package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HomeControllerTest {

    @Test
    void viewFile_doesNotReadFilesOutsideStaticDirectory() {
        HomeController controller = new HomeController();

        assertEquals("File not found: pom.xml", controller.viewFile("pom.xml"));
    }
}
