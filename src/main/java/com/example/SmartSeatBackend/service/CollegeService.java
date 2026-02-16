package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.DTO.StudentsDTO;
import com.example.SmartSeatBackend.entity.Students;
import com.example.SmartSeatBackend.entity.Subject;
import com.example.SmartSeatBackend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;



@RequiredArgsConstructor
@Service
public class CollegeService {


    private final PasswordEncoder passwordEncoder;
    @Autowired
    StudentRepository studentRepo;
    public String addStudent(StudentsDTO dto) {


        if (studentRepo.existsById(dto.getEnrollmentNo())) {
            return "Error: Enrollment number " + dto.getEnrollmentNo() + " already exists!";
        }

        // 2. Map DTO to Entity
        Students student = new Students();
        student.setEnrollmentNo(dto.getEnrollmentNo());
        student.setName(dto.getName());
        student.setMobileNumber(dto.getMobileNumber());
        student.setEmail(dto.getEmail());
        student.setBranch(dto.getBranch());
        student.setSpecialization(dto.getSpecialization());
        student.setSemester(dto.getSemester());
        student.setSubjects(dto.getSubjects());
        student.setHasBacklog(dto.isHasBacklog());
        student.setImgUrl(dto.getImgUrl());
        student.setCollegeId(dto.getCollegeId());

        // 3. Password Generation Logic
        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        String encodedPassword = passwordEncoder.encode(rawPassword);
        student.setPassword(encodedPassword);

        // 4. Save to Database
        studentRepo.save(student);

        return "Student saved successfully with enrollment: " + student.getEnrollmentNo() +
                " | Temporary Password: " + rawPassword;
    }

}