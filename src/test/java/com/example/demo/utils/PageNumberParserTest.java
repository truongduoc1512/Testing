package com.example.demo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PageNumberParserTest {

    @Test
    void parsePositivePage_usesFirstPageForInvalidOrNonPositiveInput() {
        assertEquals(1, PageNumberParser.parsePositivePage(null));
        assertEquals(1, PageNumberParser.parsePositivePage(""));
        assertEquals(1, PageNumberParser.parsePositivePage("invalid"));
        assertEquals(1, PageNumberParser.parsePositivePage("0"));
        assertEquals(1, PageNumberParser.parsePositivePage("-5"));
    }

    @Test
    void parsePositivePage_preservesValidPositiveInput() {
        assertEquals(3, PageNumberParser.parsePositivePage("3"));
    }
}
