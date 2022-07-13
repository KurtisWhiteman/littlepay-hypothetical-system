package com.hypothetical.littlepay.service;

import com.hypothetical.littlepay.LittlePayApplicationTests;
import com.hypothetical.littlepay.model.TapRaw;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = LittlePayApplicationTests.class)
@ActiveProfiles("test")
class TapServiceTest {

    @Autowired
    private TapService tapService;

    private List<CSVRecord> getTapRecords(String filename) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream(filename);
        assertNotNull(resourceAsStream);

        return CSVFormat
                .newFormat(',')
                .parse(
                        new InputStreamReader(resourceAsStream)
                ).getRecords();
    }

    @Test
    void ShouldParseRecords_WhenProvidedSmallCSVRecords() throws IOException {
        List<CSVRecord> records = getTapRecords("tapsExample.csv");

        List<TapRaw> tapRawRecords = tapService.parseTaps(records);
        assertEquals(4, tapRawRecords.size(),"Expected number of tapRawRecords does not match");
    }

    @Test
    void ShouldGenerateListOfTapRaw_WhenProvidedSmallCSVRecords() throws IOException {
        String filePath = "src/test/resources/tapsExample.csv";

        List<TapRaw> tapRawRecords = tapService.importTapsFile(filePath);
        assertEquals(4, tapRawRecords.size(),"Expected number of tapRawRecords does not match");
    }

    @Test
    void ShouldThrowError_WhenProvidedEmptyCSVFile()  {
        String filePath = "src/test/resources/tapsFileDoesntExist.csv";

        assertThrows(NoSuchFileException.class, () -> this.tapService.importTapsFile(filePath));
    }
}
