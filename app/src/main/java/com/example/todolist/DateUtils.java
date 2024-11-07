package com.example.todolist;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    // Date format for consistent output (yyyy-MM-dd)
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Format a Date object to string in "yyyy-MM-dd"
    public static String formatDate(Date date) {
        return dateFormat.format(date);
    }

    // Parse a string to a Date object, assuming it's in the "yyyy-MM-dd" format
    public static Date parseDate(String dateString) throws ParseException {
        return dateFormat.parse(dateString);
    }

    // Validate if a date string is in the "yyyy-MM-dd" format
    public static boolean isValidDateFormat(String dateString) {
        try {
            parseDate(dateString); // Try to parse the string, if it fails it's invalid
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
