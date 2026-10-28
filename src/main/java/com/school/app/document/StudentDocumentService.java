package com.school.app.document;

import com.school.app.exception.ResourceNotFoundException;
import com.school.app.security.CurrentUser;
import com.school.app.student.Student;
import com.school.app.student.StudentService;
import com.school.app.user.User;
import com.school.app.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentDocumentService {

    private final StudentDocumentRepository documentRepository;
    private final StudentService studentService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    public StudentDocumentService(StudentDocumentRepository documentRepository, StudentService studentService,
                                   FileStorageService fileStorageService, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.studentService = studentService;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    @Transactional
    public StudentDocument upload(Long studentId, DocumentType type, MultipartFile file) {
        // getById enforces that the caller (class teacher/subject teacher) is authorized for this student
        Student student = studentService.getById(studentId);
        String storedFilename = fileStorageService.store(file);

        User uploader = userRepository.findById(CurrentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        StudentDocument doc = new StudentDocument();
        doc.setStudent(student);
        doc.setDocumentType(type);
        doc.setOriginalFilename(file.getOriginalFilename());
        doc.setStoredFilename(storedFilename);
        doc.setMimeType(file.getContentType());
        doc.setFileSize(file.getSize());
        doc.setUploadedBy(uploader);
        doc.setUploadedDate(LocalDateTime.now());
        return documentRepository.save(doc);
    }

    public List<StudentDocument> listForStudent(Long studentId) {
        studentService.getById(studentId); // enforces access
        return documentRepository.findByStudentId(studentId);
    }

    public StudentDocument getById(Long documentId) {
        StudentDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
        studentService.getById(doc.getStudent().getId()); // enforces access to the owning student
        return doc;
    }

    public InputStream loadFile(StudentDocument doc) {
        return fileStorageService.loadAsStream(doc.getStoredFilename());
    }

    @Transactional
    public void delete(Long documentId) {
        StudentDocument doc = getById(documentId); // enforces access
        fileStorageService.delete(doc.getStoredFilename());
        documentRepository.delete(doc);
    }
}
