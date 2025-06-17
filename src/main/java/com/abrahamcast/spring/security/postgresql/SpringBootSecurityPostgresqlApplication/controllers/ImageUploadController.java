//package com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.controllers;

//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//mport org.springframework.web.multipart.MultipartFile;

//import java.io.IOException;
//import java.nio.file.*;
//import java.util.Map;
//import java.util.UUID;

//@CrossOrigin(origins = "http://localhost:4200")
//@RestController
//@RequestMapping("/api/images")
//public class ImageUploadController {

  //  @PostMapping("/upload")
    //public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
      //  try {
        //    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
          //  Path imagePath = Paths.get("src/main/resources/static/uploads/" + fileName);

            //Files.createDirectories(imagePath.getParent());
            //iles.copy(file.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);

            // URL relativa para usar desde Angular
            //String imageUrl = "/uploads/" + fileName;

            //return ResponseEntity.ok(Map.of("url", imageUrl));
        //} catch (IOException e) {
          //  return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
           //         .body("Error uploading image: " + e.getMessage());
       // }
    //}
//}
