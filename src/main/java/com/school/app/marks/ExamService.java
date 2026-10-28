package com.school.app.marks;

import com.school.app.academic.AcademicStructureService;
import com.school.app.academic.Standard;
import com.school.app.exception.DuplicateResourceException;
import com.school.app.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final AcademicStructureService academicStructureService;

    public ExamService(ExamRepository examRepository, AcademicStructureService academicStructureService) {
        this.examRepository = examRepository;
        this.academicStructureService = academicStructureService;
    }

    @Transactional
    public Exam create(ExamRequest request) {
        Standard standard = academicStructureService.getStandardById(request.getStandardId());
        if (examRepository.existsByStandardIdAndName(standard.getId(), request.getName())) {
            throw new DuplicateResourceException("Exam already exists for this standard: " + request.getName());
        }
        Exam exam = new Exam();
        exam.setName(request.getName());
        exam.setStandard(standard);
        return examRepository.save(exam);
    }

    public List<Exam> getAll() { return examRepository.findAll(); }
    public List<Exam> getByStandard(Long standardId) { return examRepository.findByStandardId(standardId); }

    public Exam getById(Long id) {
        return examRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + id));
    }

    @Transactional
    public void delete(Long id) {
        examRepository.delete(getById(id));
    }
}
