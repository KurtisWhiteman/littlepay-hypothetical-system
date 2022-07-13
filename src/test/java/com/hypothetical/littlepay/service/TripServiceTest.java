package com.hypothetical.littlepay.service;

import com.hypothetical.littlepay.LittlePayApplicationTests;
import com.hypothetical.littlepay.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = LittlePayApplicationTests.class)
@ActiveProfiles("test")
public class TripServiceTest {

    @Autowired
    private TripService tripService;

    private List<TapRaw> taps = new ArrayList<>();

    @BeforeEach
    void init() {
        taps.add(
                TapRaw.builder()
                        .id(1)
                        .createdAt("22-01-2018 13:00:44")
                        .tapType("ON")
                        .stopId("Stop1")
                        .companyId("Company1")
                        .busId("Bus37")
                        .pan(5500005555555559L)
                        .build()
        );

        taps.add(
                TapRaw.builder()
                        .id(2)
                        .createdAt("22-01-2018 13:05:37")
                        .tapType("OFF")
                        .stopId("Stop2")
                        .companyId("Company1")
                        .busId("Bus37")
                        .pan(5500005555555559L)
                        .build()
        );
    }

    @Test
    void ShouldHaveExpectedValuesInTrip_WhenGeneratingTripFromTapList() {
        List<Trip> trips = tripService.tapsToTrips(taps);
        assertEquals(1, trips.size(), "Expected 1 trip. Does not match");
        assertEquals("22-01-2018 13:00:44", trips.get(0).getStarted());
        assertEquals("22-01-2018 13:05:37", trips.get(0).getFinished());
        assertEquals(293, trips.get(0).getDuration());
        assertEquals("Stop1", trips.get(0).getFromStopId());
        assertEquals("Stop2", trips.get(0).getToStopId());
        assertEquals("$3.25", trips.get(0).getChargeAmount());
        assertEquals("Company1", trips.get(0).getCompanyId());
        assertEquals("Bus37", trips.get(0).getBusId());
        assertEquals(5500005555555559L, trips.get(0).getPan());
        assertEquals(TripStatus.COMPLETED, trips.get(0).getStatus());
    }

    @Test
    void ShouldHaveExpectedChargeAmountsInTrip_WhenStopIdsChange() {
        // Stop 1-2
        List<Trip> trips = tripService.tapsToTrips(taps);
        assertEquals("Stop1", trips.get(0).getFromStopId());
        assertEquals("Stop2", trips.get(0).getToStopId());
        assertEquals("$3.25", trips.get(0).getChargeAmount());

        // Stop 1-3
        taps.get(1).setStopId("Stop3");
        trips = tripService.tapsToTrips(taps);
        assertEquals("Stop1", trips.get(0).getFromStopId());
        assertEquals("Stop3", trips.get(0).getToStopId());
        assertEquals("$7.30", trips.get(0).getChargeAmount());

        // Stop 2-3
        taps.get(0).setStopId("Stop2");
        trips = tripService.tapsToTrips(taps);
        assertEquals("Stop2", trips.get(0).getFromStopId());
        assertEquals("Stop3", trips.get(0).getToStopId());
        assertEquals("$5.50", trips.get(0).getChargeAmount());

        // Stop 2-1
        taps.get(1).setStopId("Stop1");
        trips = tripService.tapsToTrips(taps);
        assertEquals("Stop2", trips.get(0).getFromStopId());
        assertEquals("Stop1", trips.get(0).getToStopId());
        assertEquals("$3.25", trips.get(0).getChargeAmount());

        // Stop 3-1
        taps.get(0).setStopId("Stop3");
        trips = tripService.tapsToTrips(taps);
        assertEquals("Stop3", trips.get(0).getFromStopId());
        assertEquals("Stop1", trips.get(0).getToStopId());
        assertEquals("$7.30", trips.get(0).getChargeAmount());
    }

    @Test
    void ShouldHaveExpectedValues_WhenPassedOneTap() {
        taps.remove(1);
        List<Trip> trips = tripService.tapsToTrips(taps);
        assertEquals(0, trips.get(0).getDuration());
        assertEquals(5500005555555559L, trips.get(0).getPan());
        assertEquals("$7.30", trips.get(0).getChargeAmount());
        assertEquals("Stop1", trips.get(0).getFromStopId());
        assertEquals("", trips.get(0).getToStopId());
        assertEquals(TripStatus.INCOMPLETE, trips.get(0).getStatus());
    }

    @Test
    void ShouldHaveExpectedValues_WhenTapsGoingToSameStopId() {
        taps.get(1).setStopId(taps.get(0).getStopId());
        List<Trip> trips = tripService.tapsToTrips(taps);
        assertEquals(5500005555555559L, trips.get(0).getPan());
        assertEquals("$0", trips.get(0).getChargeAmount());
        assertEquals(TripStatus.CANCELLED, trips.get(0).getStatus());
    }
}
