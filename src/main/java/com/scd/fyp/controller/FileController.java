package com.scd.fyp.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller for serving uploaded files
 * Handles file downloads with proper content types and URL encoding
 */
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {
    
    /**
     * Serve a file by its path
     * This endpoint properly handles URL-encoded paths
     * Usage: /api/files?path=uploads/submissions/filename.jpg
     */
    @GetMapping
    public ResponseEntity<Resource> serveFile(@RequestParam String path) {
        try {
            // Decode the path parameter
            String decodedPath = java.net.URLDecoder.decode(path, "UTF-8");
            
            // Remove leading slash if present
            if (decodedPath.startsWith("/")) {
                decodedPath = decodedPath.substring(1);
            }
            
            // Ensure path is within uploads directory (security check)
            if (!decodedPath.startsWith("uploads/")) {
                return ResponseEntity.badRequest().build();
            }
            
            // Get the file path
            Path filePath = Paths.get(decodedPath).normalize();
            
            // Additional security: ensure path doesn't contain ".."
            if (filePath.toString().contains("..")) {
                return ResponseEntity.badRequest().build();
            }
            
            // Check if file exists
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            // Load file as resource
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            
            // Determine content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                // Try to determine from file extension
                String fileName = filePath.getFileName().toString().toLowerCase();
                if (fileName.endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (fileName.endsWith(".png")) {
                    contentType = "image/png";
                } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                    contentType = "application/msword";
                } else {
                    contentType = "application/octet-stream";
                }
            }
            
            // Get filename for content disposition
            String filename = filePath.getFileName().toString();
            
            // Return file with proper headers
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "inline; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .body(resource);
                    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}

