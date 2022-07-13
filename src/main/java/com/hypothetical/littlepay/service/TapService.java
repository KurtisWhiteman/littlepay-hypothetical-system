package com.hypothetical.littlepay.service;

import com.hypothetical.littlepay.helper.CSVHelper;
import com.hypothetical.littlepay.model.TapRaw;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class TapService {

    /**
     * Extracts Taps from file into a List
     *
     * @return List of Tap object(s)
     */
    public List<TapRaw> importTapsFile(String filePath) throws IOException {
        // Get tap records file from input folder
        Path path = Paths.get(filePath);
        try {
            byte[] tapsFileData = Files.readAllBytes(path);
            List<CSVRecord> tapRecords = CSVHelper.recordsFromBytes(tapsFileData);
            System.out.println("Converting CSV content to Taps");
            return parseTaps(tapRecords);
        } catch (NoSuchFileException e) {
            throw new NoSuchFileException("File does not exist");
        }
    }

    /**
     * Extracts Taps from file into a List
     *
     * @return List of Tap object(s)
     */
    public List<TapRaw> parseTaps(List<CSVRecord> tapRecords) {
        // Loop through tapRecords to build TapRaw object from file data
        return tapRecords
                .subList(1, tapRecords.size())
                .stream()
                .flatMap(tapRecord -> {

                    List<TapRaw> taps = new ArrayList<>();

                    taps.add(
                            TapRaw.builder()
                                    .id(Long.parseLong(tapRecord.get(0)))
                                    .createdAt(tapRecord.get(1))
                                    .tapType(tapRecord.get(2))
                                    .stopId(tapRecord.get(3))
                                    .companyId(tapRecord.get(4))
                                    .busId(tapRecord.get(5))
                                    .pan(Long.parseLong(tapRecord.get(6)))
                                    .build()
                    );

                    return taps.stream();
                }).toList();
    }
}
