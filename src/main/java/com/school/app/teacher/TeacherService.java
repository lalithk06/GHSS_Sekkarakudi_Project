package com.school.app.teacher;

import com.school.app.exception.DuplicateResourceException;
import com.school.app.exception.InvalidRequestException;
import com.school.app.exception.ResourceNotFoundException;
import com.school.app.user.Role;
import com.school.app.user.User;
import com.school.app.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.school.app.academic.ClassTeacherAssignmentRepository classAssignmentRepository;
    private final com.school.app.academic.SubjectTeacherAssignmentRepository subjectAssignmentRepository;
    private final com.school.app.attendance.ClassAttendanceRepository attendanceRepository;
    private final com.school.app.marks.MarkRepository markRepository;
    private final com.school.app.document.StudentDocumentRepository documentRepository;

    public TeacherService(TeacherRepository teacherRepository, UserRepository userRepository, PasswordEncoder passwordEncoder,
                          com.school.app.academic.ClassTeacherAssignmentRepository classAssignmentRepository,
                          com.school.app.academic.SubjectTeacherAssignmentRepository subjectAssignmentRepository,
                          com.school.app.attendance.ClassAttendanceRepository attendanceRepository,
                          com.school.app.marks.MarkRepository markRepository,
                          com.school.app.document.StudentDocumentRepository documentRepository) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.classAssignmentRepository = classAssignmentRepository;
        this.subjectAssignmentRepository = subjectAssignmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.markRepository = markRepository;
        this.documentRepository = documentRepository;
    }

    @Transactional
    public Teacher createTeacher(CreateTeacherRequest request) {
        // Role is optional when adding a teacher; default to the basic teacher login type.
        // Assigning them as a class teacher later upgrades the login automatically.
        if (request.getRole() == null) {
            request.setRole(Role.SUBJECT_TEACHER);
        }
        if (request.getRole() != Role.CLASS_TEACHER && request.getRole() != Role.SUBJECT_TEACHER) {
            throw new InvalidRequestException("Teacher role must be CLASS_TEACHER or SUBJECT_TEACHER");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);
        user = userRepository.save(user);

        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setFullName(request.getFullName());
        teacher.setPhone(request.getPhone());
        teacher.setEmail(request.getEmail());
        teacher.setActive(true);
        return teacherRepository.save(teacher);
    }

    public List<Teacher> getAll() {
        return teacherRepository.findAll();
    }

    public Teacher getById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + id));
    }

    @Transactional
    public Teacher update(Long id, CreateTeacherRequest request) {
        Teacher teacher = getById(id);
        teacher.setFullName(request.getFullName());
        teacher.setPhone(request.getPhone());
        teacher.setEmail(request.getEmail());
        return teacherRepository.save(teacher);
    }

    @Transactional
    public void deactivate(Long id) {
        Teacher teacher = getById(id);
        teacher.setActive(false);
        teacher.getUser().setActive(false);
        teacherRepository.save(teacher);
    }

    @Transactional
    public void delete(Long id) {
        Teacher teacher = getById(id);
        User user = teacher.getUser();
        Long userId = user.getId();
        // Attendance, marks and documents this login created keep pointing at the account,
        // so removing it would break those records. Ask the admin to deactivate instead.
        if (attendanceRepository.existsByRecordedById(userId) || markRepository.existsByRecordedById(userId)
                || documentRepository.existsByUploadedById(userId)) {
            throw new InvalidRequestException("This teacher has already recorded attendance, marks or documents, "
                    + "so the account cannot be removed. Use Deactivate instead.");
        }
        // Remove the teacher's class/subject assignments first (they reference the teacher).
        classAssignmentRepository.deleteByTeacherId(teacher.getId());
        subjectAssignmentRepository.deleteByTeacherId(teacher.getId());
        teacherRepository.delete(teacher);
        userRepository.delete(user);
    }
}
