package com.akka.wrapper.util;

/**
 * There are different date formats our system supports. This is an enumeration of those formats.
 */
public enum SupportedDatePattern {

    YYYYMMDD ("yyyyMMdd"),
    YYYYMMDD_HH24_MM_SS ("yyyyMMdd HH:mm:ss"),
    YYYY_MM_DD ("yyyy-MM-dd"),
    DD_MMM_YY ("dd-MMM-yy"),
    DD_MM_YYYY ("dd-mm-yyyy"),
    MM_DD_YYYY ("MM/dd/yyyy"),
    MM_DD_YYYY_HH_MM_SS_AA ("MM/dd/yyyy hh:mm:ss aa"),
    MM_DD_YYYY_HH_MM_SS ("MM/dd/yyyy HH:mm:ss"),
    M_D_YYYY_H_MM_SS_A ("M/d/yyyy h:mm:ss a"),
    YYYY_MM_DD_HH24_MM_SS ("yyyy-MM-dd HH:mm:ss"),
    YYYY_MM_DD_T_HH24_MM_SS ("yyyy-MM-dd'T'HH:mm:ss"),
    YYYY_MM_DD_T_HH_MM_SS_SSSZ ("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
    E_MMM_DD_HH_MM_SS_Z_YYYY ("E MMM dd HH:mm:ss Z yyyy");

    private String dateFormat;

    private SupportedDatePattern(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public static String[] getDateFormatValues() {
        String[] dateFormatValues = new String[SupportedDatePattern.values().length];
        for (SupportedDatePattern lotDatePattern : SupportedDatePattern.values()) {
            dateFormatValues[lotDatePattern.ordinal()] = lotDatePattern.dateFormat;
        }
        return dateFormatValues;
    }

    public String getDateFormat() {
        return dateFormat;
    }
}
