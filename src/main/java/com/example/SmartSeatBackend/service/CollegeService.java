package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.DTO.CollegeDTO;
import com.example.SmartSeatBackend.DTO.RoomsDTO;
import com.example.SmartSeatBackend.DTO.StudentsDTO;
import com.example.SmartSeatBackend.entity.College;
import com.example.SmartSeatBackend.entity.Rooms;
import com.example.SmartSeatBackend.entity.Students;
import com.example.SmartSeatBackend.entity.Subject;

import com.example.SmartSeatBackend.repository.CollegeRepository;
import com.example.SmartSeatBackend.repository.RoomsRepository;
import org.springframework.beans.BeanUtils;
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
    CollegeRepository collegeRepo;

    @Autowired
    RoomsRepository roomsRepo;
    private final StudentRepository studentRepo;
    private final MessageService msgService;



    public String addStudent(StudentsDTO dto) {


        if (studentRepo.existsById(dto.getEnrollmentNo())) {
            return "Error: Enrollment number " + dto.getEnrollmentNo() + " already exists!";
        }

        // 2. Map DTO to Entity
        Students student = new Students();
//        student.setEnrollmentNo(dto.getEnrollmentNo());
//        student.setName(dto.getName());
//        student.setMobileNumber(dto.getMobileNumber());
//        student.setEmail(dto.getEmail());
//        student.setBranch(dto.getBranch());
//        student.setSpecialization(dto.getSpecialization());
//        student.setSemester(dto.getSemester());
//        student.setSubjects(dto.getSubjects());
//        student.setHasBacklog(dto.isHasBacklog());
//        student.setImgUrl(dto.getImgUrl());
//        student.setCollegeId(dto.getCollegeId());

        // 3. Password Generation Logic
        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        String encodedPassword = passwordEncoder.encode(rawPassword);
        student.setPassword(encodedPassword);
        BeanUtils.copyProperties(dto, student);

        // 4. Save to Database
        studentRepo.save(student);
        //email service
//        msgService.sendRegistrationEvent(
//                dto.getEmail(),
//                rawPassword,
//                dto.getName(),String.valueOf(dto.getCollegeId()));

        return "Student saved successfully with enrollment: " + student.getEnrollmentNo() +
                " | Temporary Password: " + rawPassword;
    }


    public RoomsDTO addRooms(RoomsDTO dto) {
        // 1. Fetch College entity by ID
        College college = collegeRepo.findById(dto.getCollege_id())
                .orElseThrow(() -> new RuntimeException("College not found"));

        // 2. Check if roomNumber already exists for this college
        boolean exists = roomsRepo.existsByRoomNumberAndCollege(dto.getRoomNumber(), college);
        if (exists) {
            throw new RuntimeException("Room number " + dto.getRoomNumber() + " already exists for this college");
        }

        // 3. Map DTO to Entity
        Rooms room = new Rooms();
        room.setRoomNumber(dto.getRoomNumber());
        room.setCapacity(dto.getCapacity());
        if (dto.getBlock() == null || dto.getBlock().isEmpty()) {
            room.setBlock("A");
        } else {
            room.setBlock(dto.getBlock());
        }
        room.setCollege(college);

        // 3. Save
        Rooms saved = roomsRepo.save(room);

        // 4. Return DTO
        RoomsDTO response = new RoomsDTO();
        response.setRoomNumber(saved.getRoomNumber());
        response.setBlock(saved.getBlock());
        response.setCapacity(saved.getCapacity());
        response.setCollege_id(saved.getCollege().getCollegeId());

        return response;
    }




}