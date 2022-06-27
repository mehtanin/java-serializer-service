package com.service.javaserializerservice.controller;

import com.service.javaserializerservice.GreetingController;
import com.service.javaserializerservice.payload.UploadFileResponse;
import com.service.javaserializerservice.service.AasxSerializerService;
import com.service.javaserializerservice.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class FileController {
    @Value("${inputEnvironment.path}")
    private String inputEnvPath;
    @Value("${outputEnvironment.path}")
    private String outputEnvPath;
    @Value("${additionalFile.path}")
    private String additionalEnvPath;
    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping(path = "/uploadFileRdf", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String>  uploadFileRdf(@RequestPart("file") MultipartFile file) throws Exception {
        String fileName = fileStorageService.storeFile(file);
        String returnValue = new AasxSerializerService().convertRdfToAasx(inputEnvPath, additionalEnvPath, outputEnvPath, fileName);
        System.out.print(returnValue);
        return ResponseEntity.status(HttpStatus.OK).body("Success: " + fileName + " uploaded!");
    }

    @PostMapping(path = "/uploadFileAasx", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String>  uploadFileAasx(@RequestPart("file") MultipartFile file) throws Exception {
        String fileName = fileStorageService.storeFile(file);
        String returnValue = new AasxSerializerService().convertAasxToRdf(inputEnvPath, outputEnvPath, fileName);
        System.out.print(returnValue);
        return ResponseEntity.status(HttpStatus.OK).body("Success: " + fileName + " uploaded!");
    }

    @PostMapping("/uploadMultipleFiles")
    public ResponseEntity<String> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
//        return Arrays.asList(files)
//                .stream()
//                .map(file -> uploadFile(file))
//                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body("All files " + " uploaded successfully!");

    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            System.out.print("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
