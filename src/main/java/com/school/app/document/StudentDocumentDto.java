package com.school.app.document;

import java.time.LocalDateTime;

public class StudentDocumentDto {
    private Long id;
    private Long studentId;
    private String documentType;
    private String originalFilename;
    private String mimeType;
    private long fileSize;
    private String uploadedByUsername;
    private LocalDateTime uploadedDate;

    public static StudentDocumentDto fromEntity(StudentDocument d) {
        StudentDocumentDto dto = new StudentDocumentDto();
        dto.setId(d.getId());
        dto.setStudentId(d.getStudent().getId());
        dto.setDocumentType(d.getDocumentType().name());
        dto.setOriginalFilename(d.getOriginalFilename());
        dto.setMimeType(d.getMimeType());
        dto.setFileSize(d.getFileSize());
        dto.setUploadedByUsername(d.getUploadedBy().getUsername());
        dto.setUploadedDate(d.getUploadedDate());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getUploadedByUsername() { return uploadedByUsername; }
    public void setUploadedByUsername(String uploadedByUsername) { this.uploadedByUsername = uploadedByUsername; }
    public LocalDateTime getUploadedDate() { return uploadedDate; }
    public void setUploadedDate(LocalDateTime uploadedDate) { this.uploadedDate = uploadedDate; }
}
