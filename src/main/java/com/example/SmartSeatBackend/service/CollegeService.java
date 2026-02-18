package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.DTO.CollegeDTO;
import com.example.SmartSeatBackend.DTO.RoomsDTO;
import com.example.SmartSeatBackend.DTO.StudentsDTO;
import com.example.SmartSeatBackend.DTO.TempCollegeDTO;
import com.example.SmartSeatBackend.entity.College;
import com.example.SmartSeatBackend.entity.Rooms;
import com.example.SmartSeatBackend.entity.Students;
import com.example.SmartSeatBackend.entity.Subject;
import com.example.SmartSeatBackend.repository.CollegeRepository;
import com.example.SmartSeatBackend.repository.RoomsRepository;

import com.example.SmartSeatBackend.utility.HelperMethods;
import org.springframework.beans.BeanUtils;

import com.example.SmartSeatBackend.repository.StudentRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
public class CollegeService {

    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final CollegeRepository collegeRepo;
    private final RoomsRepository roomsRepo;
    private final StudentRepository studentRepo;
    private final MessageService msgService;
    private final HelperMethods helper;

    
    public String addStudent(StudentsDTO dto) {

        if (studentRepo.existsById(dto.getEnrollmentNo())) {
            return "Error: Enrollment number " + dto.getEnrollmentNo() + " already exists!";
        }

        Students student = new Students();

        // Generate Random Password
        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        String encodedPassword = passwordEncoder.encode(rawPassword);
        student.setPassword(encodedPassword);
        student.setCollegeId(helper.getCollegeIdByUserId());
        BeanUtils.copyProperties(dto, student);
        // 4. Save to Database
        studentRepo.save(student);
        //email service
//        msgService.sendRegistrationEvent(
//                dto.getEmail(),
//                rawPassword,
//                dto.getName(),String.valueOf(dto.getCollegeId()));


        student.setCollegeId(helper.getCollegeIdByUserId());

        BeanUtils.copyProperties(dto, student);

        studentRepo.save(student);

        return "Student saved successfully with enrollment: "
                + student.getEnrollmentNo()
                + " | Temporary Password: "
                + rawPassword;
    }

    public RoomsDTO addRooms(RoomsDTO dto) {

        College college = collegeRepo.findById(helper.getCollegeIdByUserId())
                .orElseThrow(() -> new RuntimeException("College not found"));

        Rooms room = new Rooms();
        room.setRoomNumber(dto.getRoomNumber());
        room.setCapacity(dto.getCapacity());



        room.setCollege(college);

        Rooms saved = roomsRepo.save(room);

        RoomsDTO response = new RoomsDTO();
        response.setRoomNumber(saved.getRoomNumber());
        response.setBlock(saved.getBlock());
        response.setCapacity(saved.getCapacity());

        room.setBlock(dto.getBlock());
        room.setCollege(college);
        return dto;
    }

    public List<String> saveRoomsFromCSV(MultipartFile file) throws IOException
    {

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

                RoomsDTO room = new RoomsDTO();

                room.setRoomNumber(Integer.parseInt(record.get("roomNumber")));
                room.setCapacity(Integer.parseInt(record.get("capacity")));
                room.setBlock(record.get("block"));

                Set<ConstraintViolation<RoomsDTO>> violations =
                        validator.validate(room);

                if (!violations.isEmpty()) {
                    throw new ConstraintViolationException(violations);
                }

                RoomsDTO response = addRooms(room);
                responses.add("Room " + response.getRoomNumber() + " saved successfully");
            }
        }

        return responses;
    }

}

