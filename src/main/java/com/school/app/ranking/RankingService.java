package com.school.app.ranking;

import com.school.app.marks.Mark;
import com.school.app.marks.MarkRepository;
import com.school.app.student.Student;
import com.school.app.student.StudentRepository;
import com.school.app.subject.SubjectService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RankingService {

    private final MarkRepository markRepository;
    private final StudentRepository studentRepository;
    private final SubjectService subjectService;

    public RankingService(MarkRepository markRepository, StudentRepository studentRepository,
                           SubjectService subjectService) {
        this.markRepository = markRepository;
        this.studentRepository = studentRepository;
        this.subjectService = subjectService;
    }

    /**
     * Computes ranks on the fly from Mark records for all students in a section/group for a given exam.
     * Ties receive the same rank (standard competition ranking, e.g. 1,2,2,4).
     */
    public List<RankEntryDto> rankBySectionGroupAndExam(Long sectionGroupId, Long examId) {
        List<Student> students = studentRepository.findBySectionGroupId(sectionGroupId);
        return computeRanking(students, examId);
    }

    public List<RankEntryDto> rankByStandardAndExam(Long standardId, Long examId) {
        List<Student> students = studentRepository.findByStandardId(standardId);
        return computeRanking(students, examId);
    }

    private List<RankEntryDto> computeRanking(List<Student> students, Long examId) {
        record Totals(Student student, double obtained, double max) {}

        List<Totals> totals = students.stream().map(student -> {
            Long sectionGroupId = student.getSectionGroup().getId();
            List<Mark> marks = markRepository.findByStudentIdAndExamId(student.getId(), examId).stream()
                    // Defensive: only ever count a subject that is actually
                    // valid for the student's current section/group. Marks
                    // are already blocked at entry time for the wrong
                    // subject/group combo, but this keeps rankings correct
                    // even if a student later transfers to a different
                    // 12th group and old marks from the previous group
                    // still exist.
                    .filter(m -> subjectService.isValidForSectionGroup(m.getSubject().getId(), sectionGroupId))
                    .toList();
            double obtained = marks.stream().mapToDouble(Mark::getMarksObtained).sum();
            double max = marks.stream().mapToDouble(Mark::getMaxMarks).sum();
            return new Totals(student, obtained, max);
        })
        .filter(t -> t.max() > 0) // exclude students with no (valid) marks recorded for this exam
        .sorted(Comparator.comparingDouble(Totals::obtained).reversed())
        .toList();

        List<RankEntryDto> result = new ArrayList<>();
        int rank = 0;
        Double previousScore = null;
        int position = 0;
        for (Totals t : totals) {
            position++;
            if (previousScore == null || t.obtained() < previousScore) {
                rank = position;
                previousScore = t.obtained();
            }
            result.add(new RankEntryDto(rank, t.student().getId(), t.student().getName(), t.obtained(), t.max()));
        }
        return result;
    }
}
