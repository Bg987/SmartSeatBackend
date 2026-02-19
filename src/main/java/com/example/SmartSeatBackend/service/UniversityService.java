package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.DTO.SubjectDTO;
import com.example.SmartSeatBackend.DTO.TempCollegeDTO;
import com.example.SmartSeatBackend.entity.Subject;
import com.example.SmartSeatBackend.entity.User;
import com.example.SmartSeatBackend.entity.College;
import com.example.SmartSeatBackend.repository.CollegeRepository;
import com.example.SmartSeatBackend.repository.SubjectRepository;
import com.example.SmartSeatBackend.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@RequiredArgsConstructor
@Service
public class UniversityService {

    private final CollegeRepository collegeRepo;
    private final UserRepository userRepo;
    private final SubjectRepository subRepo;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final MessageService msgService;

    //  Get All Subjects
    public List<Subject> getAllSubjects() {
        return subRepo.findAll();
    }

    // Add Subject
    public ResponseEntity<String> addSubject(SubjectDTO subjectdto) {

        Subject subject = new Subject();
        subject.setSubjectName(subjectdto.getSubjectName());
        subject.setSubjectId(subjectdto.getSubjectId());

        subRepo.save(subject);

        return ResponseEntity.ok("Subject added successfully");
    }

    //  Upload Subjects CSV
    public List<String> saveSubjectsFromCSV(MultipartFile file) throws IOException {

        List<String> responses = new ArrayList<>();

        try (
                Reader reader = new BufferedReader(
                        new InputStreamReader(file.getInputStream()));
                CSVParser csvParser = new CSVParser(
                        reader,
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


    //  Get All Colleges
    public ResponseEntity<List<User>> getAllColleges() {
        List<User> colleges = userRepo.findByRole(User.Role.college);
        return ResponseEntity.ok(colleges);
    }

    // Add College
    public ResponseEntity<String> addCollege(TempCollegeDTO collegeData) {

        User userCollege = new User();
        userCollege.setName("Admin of " + collegeData.getCollegeName());
        userCollege.setMail(collegeData.getEmail());
        userCollege.setMobileNumber(collegeData.getContactNumber());
        userCollege.setRole(User.Role.college);

        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        userCollege.setPassword(passwordEncoder.encode(rawPassword));

        User savedUser = userRepo.save(userCollege);

        College college = new College();
        college.setName(collegeData.getCollegeName());
        college.setAddress(collegeData.getAddress());
        college.setUser(savedUser);

        collegeRepo.save(college);

        //email service
        //collegeID set to null so function identify data either student or college so send data based on it to kafka
//         msgService.sendRegistrationEvent(
//                 collegeData.getEmail(),
//                 rawPassword,
//                 collegeData.getCollegeName(),
//                 null
//         );

        return ResponseEntity.ok("College added successfully. Generated Password: " + rawPassword);
    }

    //  Upload Colleges CSV

    public List<String> saveCollegesFromCSV(MultipartFile file) throws IOException {


        List<String> responses = new ArrayList<>();

        try (
                Reader reader = new BufferedReader(
                        new InputStreamReader(file.getInputStream()));
                CSVParser csvParser = new CSVParser(
                        reader,
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
}
