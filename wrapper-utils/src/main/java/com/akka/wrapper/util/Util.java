package com.akka.wrapper.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;

public final class Util {

    private static final int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    private static final String CONCAT = "_";


    public static Map<String, List<String>> adapt(MultiValueMap<String, String> params) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (Entry<String, List<String>> entry : params.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static String checkNull(String field) {
        if (field == null || StringUtils.isEmpty(field)) {
            return "";
        }

        return field;
    }

    public static Integer daysPassed(Date lotDate) {
        if (null != lotDate) {
            final long lotTime = lotDate.getTime();
            final long timeSinceOnLot = System.currentTimeMillis() - lotTime;
            if (timeSinceOnLot >= 0) {
                final Long days = timeSinceOnLot / MILLIS_IN_DAY;
                return days.intValue();
            }
        }
        return null;
    }


    public static int[] adaptArray(String[] input) {
        if (null == input) {
            return null;
        }
        int years[] = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            years[i] = Integer.parseInt(input[i]);
        }
        return years;
    }

    public static boolean isGreaterThanSize(String[] input, int size) {
        if (null == input) {
            return false;
        }

        if (input.length > size) {
            return true;
        }
        return false;
    }

    public static String convertDateToString(Date date, String format) {
        if (date == null) {
            return null;
        }
        DateFormat dateFormatted = new SimpleDateFormat(format);
        String dateStr = dateFormatted.format(date);
        return dateStr;
    }

    public static Date convertStringToDate(String dateStr, String format) {
        if (StringUtils.isEmpty(dateStr)) {
            return null;
        }
        DateFormat dateFormatted = new SimpleDateFormat(format);
        try {
            Date date = dateFormatted.parse(dateStr);
            return date;
        } catch (ParseException e) {
            LOGGER.error("Error while parsing the date string " + dateStr + " in format " + format);
        }
        return null;
    }

    /**
     * Converts string into a date(String should correspond to patterns in {@link SupportedDatePattern}
     *
     * @param dateStr
     * @return date
     * @throws ParseException
     */
    public static Date convertStringToDate(String dateStr) throws ParseException {
        if (StringUtils.isEmpty(dateStr)) {
            return null;
        }
        try {
            return DateUtils.parseDateStrictly(dateStr, SupportedDatePattern.getDateFormatValues());
        } catch (ParseException ex) {
            List<DateTimeParser> parsers = new ArrayList<>();
            for (SupportedDatePattern supportedDatePattern : SupportedDatePattern.values()) {
                parsers.add(DateTimeFormat.forPattern(supportedDatePattern.getDateFormat()).getParser());
            }
            DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(null, parsers.toArray(new DateTimeParser[0])).toFormatter();
            DateTime dt = formatter.parseDateTime(dateStr);
            return dt.toDate();
        }
    }

    public static String generateUniqueId(String hashCode) {
        String uniqueId = String.valueOf(hashCode) + CONCAT + String.valueOf(System.currentTimeMillis());
        return uniqueId;
    }
}