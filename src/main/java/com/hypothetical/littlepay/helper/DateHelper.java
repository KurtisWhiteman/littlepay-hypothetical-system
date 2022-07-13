package com.hypothetical.littlepay.helper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateHelper {

    private DateHelper() {
    }

    public static final String DATE_TIME_FORMAT1 = "dd-MM-yyyy HH:mm:ss";
    public static final DateTimeFormatter DATE_TIME_FORMATTER1 = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT1);

    /**
     * Helper function to convert String to LocalDateTime with format
     *
     * @param dateTimeFormatter date time formatter
     * @param dateStringValue   string value of dateTime
     *
     * @return LocalDateTime object
     */
    public static LocalDateTime convertStringToSpecificDateFormat(DateTimeFormatter dateTimeFormatter, String dateStringValue) {
        try {
            return LocalDateTime.parse(dateStringValue, dateTimeFormatter);
        } catch (Exception e) {
            throw new DateTimeParseException("Error while converting date value.", dateStringValue, 0);
        }
    }
}
