package com.example.demo.controller;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Home Controller", description = "Các API trang chủ, điều hướng hệ thống và tiện ích")
@Controller
@Transactional
public class HomeController {

   private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);
   private static final Path STATIC_ROOT = Paths.get("src/main/resources/static")
         .toAbsolutePath().normalize();

   @RequestMapping("/")
   public String home(Model model, @RequestParam(value = "keyword", defaultValue = "") String keyword) {
      if (keyword != null && !keyword.isEmpty()) {
          model.addAttribute("keyword", keyword);
      }
      return "index";
   }

   @RequestMapping("/403")
   public String accessDenied(Model model) {
      return "403";
   }

   // --- Minh họa truy cập đường dẫn tệp ---
   @RequestMapping(value = "/viewFile", method = RequestMethod.GET)
   @ResponseBody
   public String viewFile(@RequestParam("filename") String filename) {
      if (filename == null || filename.trim().isEmpty()) {
         return "File not found: " + filename;
      }
      try {
         Path requestedFile = STATIC_ROOT.resolve(filename).normalize();
         if (!requestedFile.startsWith(STATIC_ROOT) || !Files.isRegularFile(requestedFile)) {
            return "File not found: " + filename;
         }
         return new String(Files.readAllBytes(requestedFile), StandardCharsets.UTF_8);
      } catch (Exception e) {
         LOGGER.error("Không thể đọc static file {}", filename, e);
         return "Unable to read file: " + filename;
      }
   }
}
