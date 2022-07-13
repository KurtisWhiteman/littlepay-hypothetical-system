package com.hypothetical.littlepay.helper;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CSVHelper {

    private CSVHelper() {
        throw new IllegalStateException("Helper class shouldn't be constructed");
    }

    public static List<CSVRecord> recordsFromBytes(byte[] bytes) throws IOException {
        return CSVHelper.recordsFromBytes(bytes, ',');
    }

    public static List<CSVRecord> recordsFromBytes(byte[] bytes, char delimiter) throws IOException {
        return CSVFormat
                .newFormat(delimiter)
                .withQuote('"')     // This is to handle commas in the fields
                .withEscape('\\')
                .parse(
                        new InputStreamReader(
                                new ByteArrayInputStream(bytes),
                                StandardCharsets.UTF_8
                        )
                )
                .getRecords();
    }
}
