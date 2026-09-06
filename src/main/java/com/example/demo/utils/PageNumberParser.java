package com.example.demo.utils;

public final class PageNumberParser {

    private static final int FIRST_PAGE = 1;

    private PageNumberParser() {
    }

    public static int parsePositivePage(String pageValue) {
        if (pageValue == null) {
            return FIRST_PAGE;
        }
        try {
            return Math.max(Integer.parseInt(pageValue.trim()), FIRST_PAGE);
        } catch (NumberFormatException exception) {
            return FIRST_PAGE;
        }
    }
}
