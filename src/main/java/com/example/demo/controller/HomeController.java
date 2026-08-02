package com.example.demo.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

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
      try {
         String basePath = "src/main/resources/static/";
         File file = new File(basePath + filename);
         if (file.exists()) {
             return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
         } else {
             File systemFile = new File(filename);
             if (systemFile.exists()) {
                 return new String(Files.readAllBytes(systemFile.toPath()), StandardCharsets.UTF_8);
             }
         }
         return "File not found: " + filename;
      } catch (Exception e) {
         return "Error: " + e.getMessage();
      }
   }
}
