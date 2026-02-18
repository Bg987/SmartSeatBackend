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
import com.example.SmartSeatBackend.repository.StudentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CollegeService {

    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepo;
    private final MessageService msgService;

    @Autowired
    private CollegeRepository collegeRepo;

    @Autowired
    private RoomsRepository roomsRepo;

    public String addStudent(StudentsDTO dto) {

        if (studentRepo.existsById(dto.getEnrollmentNo())) {
            return "Error: Enrollment number " + dto.getEnrollmentNo() + " already exists!";
        }

        Students student = new Students();

        // Generate Random Password
        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        String encodedPassword = passwordEncoder.encode(rawPassword);
        student.setPassword(encodedPassword);

        student.setCollegeId(getCollegeIdByUserId());

        BeanUtils.copyProperties(dto, student);

        studentRepo.save(student);

        return "Student saved successfully with enrollment: "
                + student.getEnrollmentNo()
                + " | Temporary Password: "
                + rawPassword;
    }

    public RoomsDTO addRooms(RoomsDTO dto) {

        College college = collegeRepo.findById(getCollegeIdByUserId())
                .orElseThrow(() -> new RuntimeException("College not found"));

        boolean exists = roomsRepo.existsByRoomNumberAndCollege(dto.getRoomNumber(), college);
        if (exists) {
            throw new RuntimeException("Room number " + dto.getRoomNumber()
                    + " already exists for this college");
        }

        Rooms room = new Rooms();
        room.setRoomNumber(dto.getRoomNumber());
        room.setCapacity(dto.getCapacity());

        if (dto.getBlock() == null || dto.getBlock().isEmpty()) {
            room.setBlock("A");
        } else {
            room.setBlock(dto.getBlock());
        }

        room.setCollege(college);

        Rooms saved = roomsRepo.save(room);

        RoomsDTO response = new RoomsDTO();
        response.setRoomNumber(saved.getRoomNumber());
        response.setBlock(saved.getBlock());
        response.setCapacity(saved.getCapacity());

        return response;
    }

    public Long getCollegeIdByUserId() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getPrincipal().toString();

        return collegeRepo.findByUser_userId(Long.parseLong(userId))
                .map(College::getCollegeId)
                .orElseThrow(() ->
                        new RuntimeException("College not found for User ID: " + userId));
    }
}
