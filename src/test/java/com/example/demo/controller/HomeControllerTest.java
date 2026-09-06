package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.ui.ExtendedModelMap;

class HomeControllerTest {

    private HomeController controller;
    private ExtendedModelMap model;

    @BeforeEach
    void setUp() {
        controller = new HomeController();
        model = new ExtendedModelMap();
    }

    @Test
    void home_addsNonEmptyKeywordAndReturnsIndex() {
        assertEquals("index", controller.home(model, "shoe"));
        assertEquals("shoe", model.get("keyword"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void home_omitsMissingKeywordAndReturnsIndex(String keyword) {
        assertEquals("index", controller.home(model, keyword));

        assertFalse(model.containsAttribute("keyword"));
    }

    @Test
    void accessDenied_returnsDedicatedView() {
        assertEquals("403", controller.accessDenied(model));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "   ")
    void viewFile_rejectsMissingFilename(String filename) {
        assertEquals("File not found: " + filename, controller.viewFile(filename));
    }

    @Test
    void viewFile_rejectsPathTraversal() {
        assertEquals("File not found: ../application.properties",
                controller.viewFile("../application.properties"));
    }

    @Test
    void viewFile_readsExistingStaticFile() {
        assertFalse(controller.viewFile("style.css").isEmpty());
    }

    @Test
    void viewFile_doesNotReadFilesOutsideStaticDirectory() {
        assertEquals("File not found: pom.xml", controller.viewFile("pom.xml"));
    }

    @Test
    void viewFile_reportsInvalidPath() {
        String invalidFilename = "bad\u0000name";

        assertEquals("Unable to read file: " + invalidFilename,
                controller.viewFile(invalidFilename));
    }
}
