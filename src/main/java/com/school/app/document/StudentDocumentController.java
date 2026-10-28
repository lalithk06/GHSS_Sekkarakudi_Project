package com.school.app.document;

import com.school.app.common.ApiResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class StudentDocumentController {

    private final StudentDocumentService documentService;

    public StudentDocumentController(StudentDocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/api/students/{studentId}/documents")
    @PreAuthorize("hasAnyRole('ADMIN','CLASS_TEACHER')")
    public ApiResponse<StudentDocumentDto> upload(@PathVariable Long studentId,
                                                   @RequestParam DocumentType documentType,
                                                   @RequestParam("file") MultipartFile file) {
        StudentDocument doc = documentService.upload(studentId, documentType, file);
        return ApiResponse.ok("Document uploaded", StudentDocumentDto.fromEntity(doc));
    }

    @GetMapping("/api/students/{studentId}/documents")
    public ApiResponse<List<StudentDocumentDto>> list(@PathVariable Long studentId) {
        return ApiResponse.ok(documentService.listForStudent(studentId).stream()
                .map(StudentDocumentDto::fromEntity).toList());
    }

    @GetMapping("/api/documents/{id}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id) {
        StudentDocument doc = documentService.getById(id);
        InputStreamResource resource = new InputStreamResource(documentService.loadFile(doc));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getOriginalFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/api/documents/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CLASS_TEACHER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ApiResponse.ok("Document deleted", null);
    }
}
