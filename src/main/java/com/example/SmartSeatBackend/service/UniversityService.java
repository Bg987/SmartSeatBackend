package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.DTO.SubjectDTO;
import com.example.SmartSeatBackend.DTO.TempCollegeDTO;
import com.example.SmartSeatBackend.configurations.passwordConfiguration;
import com.example.SmartSeatBackend.entity.Subject;
import com.example.SmartSeatBackend.entity.User;
import com.example.SmartSeatBackend.repository.CollegeRepository;
import com.example.SmartSeatBackend.repository.SubjectRepository;
import com.example.SmartSeatBackend.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.SmartSeatBackend.entity.College;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UniversityService {

    private final CollegeRepository collegeRepo;
    private final UserRepository userRepo;
    private final SubjectRepository subRepo;
    private final passwordConfiguration passwordUtil;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
//    private final MessageService msgService;

    // ================= ADD COLLEGE =================
    public ResponseEntity<String> addCollege(TempCollegeDTO collegeData) {

        User userCollege = new User();
        userCollege.setName("Admin of " + collegeData.getCollegeName());
        userCollege.setMail(collegeData.getEmail());
        userCollege.setMobileNumber(collegeData.getContactNumber());
        userCollege.setRole(User.Role.college);

        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        String encodedPassword = passwordEncoder.encode(rawPassword);
        userCollege.setPassword(encodedPassword);

        User savedUser = userRepo.save(userCollege);

        College college = new College();
        college.setName(collegeData.getCollegeName());
        college.setAddress(collegeData.getAddress());
        college.setUser(savedUser);

        collegeRepo.save(college);

//        msgService.sendCollegeRegistrationEvent(
//                collegeData.getEmail(),
//                rawPassword,
//                collegeData.getCollegeName()
//        );

        return ResponseEntity.ok("College added successfully");
    }

    // ================= ADD SUBJECT =================
    public ResponseEntity<String> addSubject(@NotNull SubjectDTO subjectdto) {

        // Duplicate Check
        if (subRepo.existsById(subjectdto.getSubjectId())) {
            return ResponseEntity
                    .badRequest()
                    .body("Subject ID already exists!");
        }

        // Length Validation
        if (subjectdto.getSubjectId().length() < 4) {
            return ResponseEntity
                    .badRequest()
                    .body("Subject ID length must be 4 or more");
        }

        if (subjectdto.getSubjectName().length() < 4) {
            return ResponseEntity
                    .badRequest()
                    .body("Subject name must be at least 4 characters");
        }

        // Save Subject
        Subject subject = new Subject();
        subject.setSubjectId(subjectdto.getSubjectId());
        subject.setSubjectName(subjectdto.getSubjectName());

        subRepo.save(subject);

        return ResponseEntity.ok("Subject added successfully");
    }

    // ================= GET ALL COLLEGES =================
    public ResponseEntity<List<User>> getAllColleges() {

        List<User> colleges = userRepo.findByRole(User.Role.college);
        return ResponseEntity.ok(colleges);
    }

    // ================= SAVE COLLEGES FROM CSV =================
    public List<String> saveCollegesFromCSV(MultipartFile file) throws IOException {

        List<String> responses = new ArrayList<>();

        try (
                Reader reader = new BufferedReader(
                        new InputStreamReader(file.getInputStream()));
                CSVParser csvParser = new CSVParser(reader,
                        CSVFormat.DEFAULT
                                .withFirstRecordAsHeader()
                                .withIgnoreHeaderCase()
                                .withTrim())
        ) {

            for (CSVRecord record : csvParser) {

                TempCollegeDTO tempCollege = new TempCollegeDTO();

                tempCollege.setCollegeName(record.get("name"));
                tempCollege.setAddress(record.get("address"));
                tempCollege.setEmail(record.get("mail"));
                tempCollege.setContactNumber(record.get("contactNumber"));

                Set<ConstraintViolation<TempCollegeDTO>> violations =
                        validator.validate(tempCollege);

                if (!violations.isEmpty()) {
                    throw new ConstraintViolationException(violations);
                }

                ResponseEntity<String> response = addCollege(tempCollege);
                responses.add(response.getBody());
            }
        }

        return responses;
    }

    // ================= GET ALL SUBJECTS =================
    public List<Subject> getAllSubjects() {
        return subRepo.findAll();
    }

    // ================= SAVE SUBJECTS FROM CSV =================
    public List<String> saveSubjectsFromCSV(MultipartFile file) throws IOException {

        List<String> responses = new ArrayList<>();

        try (
                Reader reader = new BufferedReader(
                        new InputStreamReader(file.getInputStream()));
                CSVParser csvParser = new CSVParser(reader,
                        CSVFormat.DEFAULT
                                .withFirstRecordAsHeader()
                                .withIgnoreHeaderCase()
                                .withTrim())
        ) {

            for (CSVRecord record : csvParser) {

                SubjectDTO sub = new SubjectDTO();

                sub.setSubjectId(record.get("subjectId"));
                sub.setSubjectName(record.get("subjectName"));

                ResponseEntity<String> response = addSubject(sub);

                responses.add("SUCCESS: " + response.getBody());
            }
        }

        return responses;
    }
}
