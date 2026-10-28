package com.school.app.teacher;

import com.school.app.exception.ResourceNotFoundException;
import com.school.app.security.CurrentUser;
import org.springframework.stereotype.Service;

@Service
public class TeacherContextService {

    private final TeacherRepository teacherRepository;

    public TeacherContextService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    /** Resolves the Teacher record for the currently authenticated user. */
    public Teacher currentTeacher() {
        Long userId = CurrentUser.userId();
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No teacher profile linked to current user"));
    }

    public Long currentTeacherId() {
        return currentTeacher().getId();
    }
}
