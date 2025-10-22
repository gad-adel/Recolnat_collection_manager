package org.recolnat.collection.manager.common.util;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {

    static DateTimeFormatter patternDays = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static DateTimeFormatter patternMonths = DateTimeFormatter.ofPattern("yyyy-MM");
    static DateTimeFormatter patternYears = DateTimeFormatter.ofPattern("yyyy");

    public static LocalDate getLocaleDate(String date) {
        try {
            if (date.length() == 4) {
                return Year.parse(date, patternYears).atDay(1).withMonth(1);
            }
            if (date.length() == 7) {
                return YearMonth.parse(date, patternMonths).atDay(1);
            }
            return LocalDate.parse(date, patternDays);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String getDate(LocalDate dateIdentified) {
        try {
            if (dateIdentified != null) {
                return dateIdentified.format(patternDays);
            }
        } catch (DateTimeException e) {
            return null;
        }
        return null;
    }

    private static String getMonth(LocalDate dateIdentified) {
        try {
            if (dateIdentified != null) {
                return dateIdentified.format(patternMonths);
            }
        } catch (DateTimeException e) {
            return null;
        }
        return null;
    }

    private static String getYear(LocalDate dateIdentified) {
        try {
            if (dateIdentified != null) {
                return dateIdentified.format(patternYears);
            }
        } catch (DateTimeException e) {
            return null;
        }
        return null;
    }

    public static String getDateByType(String type, LocalDate date) {
        switch (type) {
            case "d" -> {
                return getDate(date);
            }
            case "m" -> {
                return getMonth(date);
            }
            case "y" -> {
                return getYear(date);
            }
            default -> {
                return null;
            }
        }
    }
}
