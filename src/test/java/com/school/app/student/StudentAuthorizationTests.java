package com.school.app.student;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.app.academic.SectionGroup;
import com.school.app.academic.Standard;
import com.school.app.support.TestFixtures;
import com.school.app.teacher.Teacher;
import com.school.app.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentAuthorizationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private TestFixtures fixtures;
    @Autowired private ObjectMapper objectMapper;

    private Standard standard;
    private SectionGroup sectionA;
    private SectionGroup sectionB;
    private Teacher classTeacherOfA;

    @BeforeEach
    void setUp() {
        standard = fixtures.createStandard("10-auth-test");
        sectionA = fixtures.createSectionGroup(standard, "A-auth-test");
        sectionB = fixtures.createSectionGroup(standard, "B-auth-test");
        classTeacherOfA = fixtures.createTeacher("ct_a_" + System.nanoTime(), Role.CLASS_TEACHER, "Teacher A");
        fixtures.assignClassTeacher(classTeacherOfA, sectionA);
    }

    private StudentRequest buildRequest(Long sectionGroupId) {
        StudentRequest req = new StudentRequest();
        req.setAdmissionNumber("ADM-" + System.nanoTime());
        req.setName("Test Student");
        req.setDob(LocalDate.of(2009, 5, 1));
        req.setGender(Gender.MALE);
        req.setStandardId(standard.getId());
        req.setSectionGroupId(sectionGroupId);
        return req;
    }

    @Test
    void classTeacherCanCreateStudentInOwnSection() throws Exception {
        String token = fixtures.tokenFor(classTeacherOfA.getUser());
        mockMvc.perform(post("/api/students")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(buildRequest(sectionA.getId()))))
                .andExpect(status().isCreated());
    }

    @Test
    void classTeacherCannotCreateStudentInOtherSection() throws Exception {
        String token = fixtures.tokenFor(classTeacherOfA.getUser());
        mockMvc.perform(post("/api/students")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(buildRequest(sectionB.getId()))))
                .andExpect(status().isForbidden());
    }
}
