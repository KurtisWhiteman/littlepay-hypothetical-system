package com.hypothetical.littlepay.service;

import com.hypothetical.littlepay.helper.CSVHelper;
import com.hypothetical.littlepay.helper.DateHelper;
import com.hypothetical.littlepay.model.*;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.hypothetical.littlepay.helper.DateHelper.convertStringToSpecificDateFormat;
import static com.hypothetical.littlepay.model.TapType.getTapTypeFromDisplayName;

@Service
public class TripService {

    /**
     * Uses list of Taps to determine Trips
     *
     * @param taps List of Tap objects
     *
     * @return List of Trips
     */
    public List<Trip> tapsToTrips(List<TapRaw> taps) {

        List<Trip> trips = new ArrayList<>();

        // Group Taps to their own List based on PAN
        Map<Long, List<TapRaw>> mapOfListOfTaps = taps.stream()
                .sorted(Comparator.comparing(TapRaw::getCreatedAt))
                .collect(Collectors.groupingBy(TapRaw::getPan));

        // Loop through Map to make Trips from Grouped List of Taps
        mapOfListOfTaps.forEach((pan, tapsForTrip) -> {

            if (Objects.isNull(tapsForTrip)) {
                return;
            }
            TapRaw firstTap = tapsForTrip.get(0);
            TapRaw lastTap = tapsForTrip.get(tapsForTrip.size() - 1);

            TripStatus tripStatus;
            int duration;

            if (tapsForTrip.size() == 1) {
                tripStatus = TripStatus.INCOMPLETE;
                duration = 0;
            } else {
                tripStatus = determineTripStatus(tapsForTrip);
                // Convert string date to LocalDateTime to get duration in seconds between TapType ON and OFF
                LocalDateTime fromDateTime = convertStringToSpecificDateFormat(DateHelper.DATE_TIME_FORMATTER1, firstTap.getCreatedAt());
                LocalDateTime toDateTime = convertStringToSpecificDateFormat(DateHelper.DATE_TIME_FORMATTER1, lastTap.getCreatedAt());
                duration = Math.toIntExact(ChronoUnit.SECONDS.between(fromDateTime, toDateTime));
            }

            String chargeAmount = "$0";
            try {
                if (!tripStatus.equals(TripStatus.CANCELLED)) {
                    DecimalFormat priceFormat = new DecimalFormat("#.00");
                    chargeAmount = "$" + priceFormat.format(determineTripCost(tapsForTrip));
                }
            } catch (IOException e) {
                throw new RuntimeException("Error while opening file stop_fares.csv: " + e.getMessage());
            }

            trips.add(
                    Trip.builder()
                            .busId(firstTap.getBusId())
                            .companyId(firstTap.getCompanyId())
                            .fromStopId(firstTap.getStopId())
                            .toStopId(tripStatus.equals(TripStatus.INCOMPLETE) ? "" : lastTap.getStopId())
                            .started(firstTap.getCreatedAt())
                            .finished(lastTap.getCreatedAt())
                            .chargeAmount(chargeAmount)
                            .pan(pan)
                            .status(tripStatus)
                            .duration(duration)
                            .build()
            );

        });

        return trips;
    }

    /**
     * Determine cost of trip based on stopIds and saved trip fares
     *
     * @param tapsForTrip List of Tap objects
     *
     * @return List of Trips
     */
    public double determineTripCost(List<TapRaw> tapsForTrip) throws IOException {

        List<String> stops = tapsForTrip.stream().map(TapRaw::getStopId).toList();

        // Get local file containing costs between stops
        String filePath = "src/main/resources/stop_fares.csv";
        Path path = Paths.get(filePath);
        byte[] data = Files.readAllBytes(path);
        List<CSVRecord> records = CSVHelper.recordsFromBytes(data);

        final var costList = records
                .subList(1, records.size())
                .stream()
                .flatMap(costRecord -> {

                    List<StopFaresRaw> stopFaresRawList = new ArrayList<>();

                    stopFaresRawList.add(
                            StopFaresRaw.builder()
                                    .id(Long.parseLong(costRecord.get(0)))
                                    .fromStopId(costRecord.get(1))
                                    .toStopId(costRecord.get(2))
                                    .chargeAmount(Float.parseFloat(costRecord.get(3)))
                                    .build()
                    );

                    return stopFaresRawList.stream();
                }).toList();

        if (stops.size() == 1) {
            List<StopFaresRaw> costs = costList.stream().filter(costRow -> costRow.getFromStopId().equals(stops.get(0))).toList();
            Optional<StopFaresRaw> maxIncompleteFare = costs.stream().max(Comparator.comparing(StopFaresRaw::getChargeAmount));
            if (maxIncompleteFare.isPresent()) {
                return maxIncompleteFare.get().getChargeAmount();
            }
        }

        return costList.stream().mapToDouble(costRow -> {
            if (costRow.getFromStopId().equals(stops.get(0)) && costRow.getToStopId().equals(stops.get(1))) {
                return costRow.getChargeAmount();
            }
            return 0;
        }).sum();
    }

    public TripStatus determineTripStatus(List<TapRaw> tapsForTrip) {
        TapRaw referenceTap = tapsForTrip.get(0);

        Boolean sameStop = tapsForTrip.stream()
                .allMatch(tapEntry -> tapEntry.getStopId().equals(referenceTap.getStopId()));

        Boolean tapOn = tapsForTrip.stream()
                .anyMatch(tapEntry -> getTapTypeFromDisplayName(tapEntry.getTapType()).equals(TapType.ON));

        Boolean tapOff = tapsForTrip.stream()
                .anyMatch(tapEntry -> getTapTypeFromDisplayName(tapEntry.getTapType()).equals(TapType.OFF));

        if (sameStop.equals(Boolean.TRUE) && tapOn.equals(Boolean.TRUE) && tapOff.equals(Boolean.TRUE)) {
            return TripStatus.CANCELLED;
        } else if (sameStop.equals(Boolean.FALSE) && tapOn.equals(Boolean.TRUE) && tapOff.equals(Boolean.TRUE)) {
            return TripStatus.COMPLETED;
        } else if (sameStop.equals(Boolean.TRUE) && tapOn.equals(Boolean.TRUE) && tapOff.equals(Boolean.FALSE)) {
            return TripStatus.INCOMPLETE;
        } else {
            return TripStatus.INCOMPLETE;
        }
    }

    public ByteArrayOutputStream produceTripsCSV(List<Trip> tripList) throws IOException {
        // Get/Create local file to put Trips in
        String outputFile = "src/main/resources/output/trips.csv";
        Path path = Paths.get(outputFile);
        final var csvStream = new ByteArrayOutputStream();
        CSVPrinter csvPrinter;

        try {
            BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
            // Write CSV File with Headers
            final var format = CSVFormat.DEFAULT.withHeader(
                    "Started",
                    "Finished",
                    "DurationSecs",
                    "FromStopId",
                    "ToStopId",
                    "ChargeAmount",
                    "CompanyId",
                    "BusId",
                    "PAN",
                    "Status");

            csvPrinter = new CSVPrinter(writer, format);

            for (Trip trip : tripList) {
                csvPrinter.printRecord(createCSVRow(trip));
            }

            // Flush and close the stream
            csvPrinter.flush();
            csvPrinter.close();

        } catch (IOException e) {
            throw new IOException("Failed to create CSV File: " + e.getMessage());
        }
        return csvStream;
    }

    private List<String> createCSVRow(Trip trip) {
        return List.of(
                Objects.toString(trip.getStarted(), ""),
                Objects.toString(trip.getFinished(), ""),
                Objects.toString(trip.getDuration(), ""),
                Objects.toString(trip.getFromStopId(), ""),
                Objects.toString(trip.getToStopId(), ""),
                Objects.toString(trip.getChargeAmount(), ""),
                Objects.toString(trip.getCompanyId(), ""),
                Objects.toString(trip.getBusId(), ""),
                Objects.toString(trip.getPan(), ""),
                Objects.toString(trip.getStatus(), "")
        );
    }
}
