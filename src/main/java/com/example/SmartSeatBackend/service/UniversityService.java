package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.DTO.SubjectDTO;
import com.example.SmartSeatBackend.DTO.TempCollegeDTO;
import com.example.SmartSeatBackend.DTO.TimetableDTO;
import com.example.SmartSeatBackend.entity.*;
import com.example.SmartSeatBackend.repository.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityService {

    private final CollegeRepository collegeRepo;
    private final UserRepository userRepo;
    private final SubjectRepository subRepo;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final MessageService msgService;
    private final TimetableRepo timetableRepo;

    // --- Subject Logic ---

    public List<Subject> getAllSubjects() {
        return subRepo.findAll();
    }

    public ResponseEntity<String> addSubject(SubjectDTO subjectdto) {
        Subject subject = new Subject();
        subject.setSubjectName(subjectdto.getSubjectName());
        subject.setSubjectId(subjectdto.getSubjectId());
        subRepo.save(subject);
        return ResponseEntity.ok("Subject added successfully");
    }

    @Transactional
    public List<String> saveSubjectsFromCSV(MultipartFile file) throws Exception {
        List<String> responses = new ArrayList<>();
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord record : csvParser) {
                try {
                    Subject subject = new Subject();
                    subject.setSubjectId(record.get("subjectId"));
                    subject.setSubjectName(record.get("subjectName"));
                    subRepo.save(subject);
                    responses.add("SUCCESS: Added " + subject.getSubjectName());
                } catch (Exception e) {
                    responses.add("FAILED: Row " + record.getRecordNumber() + " - " + e.getMessage());
                }
            }
        }
        return responses;
    }

    // --- College Logic ---

    public ResponseEntity<List<User>> getAllColleges() {
        return ResponseEntity.ok(userRepo.findByRole(User.Role.college));
    }

    @Transactional
    public ResponseEntity<String> addCollege(TempCollegeDTO collegeData) {
        // Create User Entity
        User userCollege = new User();
        userCollege.setName("Admin of " + collegeData.getCollegeName());
        userCollege.setMail(collegeData.getEmail());
        userCollege.setMobileNumber(collegeData.getContactNumber());
        userCollege.setRole(User.Role.college);

        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        userCollege.setPassword(passwordEncoder.encode(rawPassword));
        User savedUser = userRepo.save(userCollege);

        // Create College Entity
        College college = new College();
        college.setName(collegeData.getCollegeName());
        college.setAddress(collegeData.getAddress());
        college.setUser(savedUser);
        collegeRepo.save(college);

        // Send Kafka Event for Registration
        msgService.sendRegistrationEvent(collegeData.getEmail(), rawPassword, collegeData.getCollegeName(), null);

        return ResponseEntity.ok("College added successfully. Password sent to email.");
    }

    @Transactional
    public List<String> saveCollegesFromCSV(MultipartFile file) throws Exception {
        List<String> responses = new ArrayList<>();
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord record : csvParser) {
                TempCollegeDTO dto = new TempCollegeDTO();
                dto.setCollegeName(record.get("name"));
                dto.setAddress(record.get("address"));
                dto.setEmail(record.get("mail"));
                dto.setContactNumber(record.get("contactNumber"));

                Set<ConstraintViolation<TempCollegeDTO>> violations = validator.validate(dto);
                if (violations.isEmpty()) {
                    addCollege(dto);
                    responses.add("SUCCESS: " + dto.getCollegeName());
                } else {
                    responses.add("INVALID: " + dto.getCollegeName() + " - " + violations.iterator().next().getMessage());
                }
            }
        }
        return responses;
    }

    // --- Timetable Logic ---

    @Transactional
    public ResponseEntity<Map<String, Object>> generateTimetable(List<TimetableDTO> timetableDTOList) {
        String batchId = UUID.randomUUID().toString();

        List<Timetable> timetables = timetableDTOList.stream().map(dto -> {
            Timetable t = new Timetable();
            t.setSubjectId(dto.getSubjectId());
            t.setSubjectName(dto.getSubjectName());
            t.setExamDate(dto.getExamDate());
            t.setCompleted(false);
            t.setBatchId(batchId);
            return t;
        }).collect(Collectors.toList());

        List<Timetable> saved = timetableRepo.saveAll(timetables);

        Map<String, Object> response = new HashMap<>();
        response.put("status", true);
        response.put("batchId", batchId);
        response.put("data", saved);

        return ResponseEntity.ok(response);
    }

    public List<Timetable> getTimetable(String batchId) {
        return timetableRepo.findByBatchId(batchId);
    }
}