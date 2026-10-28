package com.school.app.support;

import com.school.app.academic.*;
import com.school.app.security.JwtUtil;
import com.school.app.subject.Subject;
import com.school.app.subject.SubjectRepository;
import com.school.app.subject.SubjectSectionGroup;
import com.school.app.subject.SubjectSectionGroupRepository;
import com.school.app.teacher.Teacher;
import com.school.app.teacher.TeacherRepository;
import com.school.app.user.Role;
import com.school.app.user.User;
import com.school.app.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Shared helper for building test data + auth tokens without going through the full HTTP flow. */
@Component
public class TestFixtures {

    @Autowired private UserRepository userRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private StandardRepository standardRepository;
    @Autowired private SectionGroupRepository sectionGroupRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private SubjectSectionGroupRepository subjectSectionGroupRepository;
    @Autowired private ClassTeacherAssignmentRepository classTeacherAssignmentRepository;
    @Autowired private SubjectTeacherAssignmentRepository subjectTeacherAssignmentRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    public User createUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode("Test@1234"));
        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user);
    }

    public String tokenFor(User user) {
        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
    }

    public Teacher createTeacher(String username, Role role, String fullName) {
        User user = createUser(username, role);
        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setFullName(fullName);
        teacher.setActive(true);
        return teacherRepository.save(teacher);
    }

    public Standard createStandard(String name) {
        return standardRepository.findByName(name).orElseGet(() -> {
            Standard s = new Standard();
            s.setName(name);
            return standardRepository.save(s);
        });
    }

    public SectionGroup createSectionGroup(Standard standard, String name) {
        SectionGroup sg = new SectionGroup();
        sg.setStandard(standard);
        sg.setName(name);
        sg.setActive(true);
        return sectionGroupRepository.save(sg);
    }

    public Subject createSubject(Standard standard, String name) {
        Subject subject = new Subject();
        subject.setStandard(standard);
        subject.setName(name);
        return subjectRepository.save(subject);
    }

    /** Creates the subject (if needed) and maps it to the given section/group in one step. */
    public Subject createSubject(Standard standard, SectionGroup sectionGroup, String name) {
        Subject subject = createSubject(standard, name);
        mapSubjectToGroup(subject, sectionGroup);
        return subject;
    }

    public SubjectSectionGroup mapSubjectToGroup(Subject subject, SectionGroup sectionGroup) {
        SubjectSectionGroup mapping = new SubjectSectionGroup();
        mapping.setSubject(subject);
        mapping.setSectionGroup(sectionGroup);
        mapping.setActive(true);
        return subjectSectionGroupRepository.save(mapping);
    }

    public ClassTeacherAssignment assignClassTeacher(Teacher teacher, SectionGroup sectionGroup) {
        ClassTeacherAssignment assignment = new ClassTeacherAssignment();
        assignment.setTeacher(teacher);
        assignment.setSectionGroup(sectionGroup);
        assignment.setActive(true);
        return classTeacherAssignmentRepository.save(assignment);
    }

    public SubjectTeacherAssignment assignSubjectTeacher(Teacher teacher, SectionGroup sectionGroup, Subject subject) {
        SubjectTeacherAssignment assignment = new SubjectTeacherAssignment();
        assignment.setTeacher(teacher);
        assignment.setSectionGroup(sectionGroup);
        assignment.setSubject(subject);
        assignment.setActive(true);
        return subjectTeacherAssignmentRepository.save(assignment);
    }
}
