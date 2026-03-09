package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.DTO.GetSeatByCollege;
import com.example.SmartSeatBackend.DTO.RoomsDTO;
import com.example.SmartSeatBackend.DTO.StudentsDTO;
import com.example.SmartSeatBackend.entity.*;
import com.example.SmartSeatBackend.repository.*;

import com.example.SmartSeatBackend.utility.HelperMethods;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
public class CollegeService {

    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final CollegeRepository collegeRepo;
    private final RoomsRepository roomsRepo;
    private final StudentRepository studentRepo;
    public final TimetableRepo timetableRepo;
    private final SeatAllocationRepo seatAllocationRepo;
//    private final MessageService msgService;
    private final HelperMethods helper;



    public Optional<College> getCollege(Long collegeId){
        return  collegeRepo.findByCollegeId(collegeId);
    }
    public String addStudent(@NotNull StudentsDTO dto) {

        if (studentRepo.existsById(dto.getEnrollmentNo())) {
            return "Error: Enrollment number " + dto.getEnrollmentNo() + " already exists!";
        }

        Students student = new Students();

        // Copy properties first
        BeanUtils.copyProperties(dto, student);

        // Backlog validation
        if (student.isHasBacklog()) {
            if (student.getBacklogSubjects() == null || student.getBacklogSubjects().isEmpty()) {
                return "Error: Backlog subjects required when hasBacklog is true!";
            }
        } else {
            student.setBacklogSubjects(null);
        }

        // Generate Random Password
        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        student.setPassword(passwordEncoder.encode(rawPassword));

        // Set college id
        student.setCollegeId(helper.getCollegeIdByUserId());

        // Save once
        studentRepo.save(student);

        // Send email
//        msgService.sendRegistrationEvent(
//                dto.getEmail(),
//                rawPassword,
//                dto.getName(),
//                String.valueOf(student.getCollegeId())
//        );

        return "Student saved successfully with enrollment: "
                + student.getEnrollmentNo()
                + " | Temporary Password: "
                + rawPassword;
    }
    public List<String> saveStudentsFromCSV(MultipartFile file) throws IOException {

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

                StudentsDTO student = new StudentsDTO();

                student.setEnrollmentNo(record.get("enrollmentNo"));
                student.setName(record.get("name"));
                student.setEmail(record.get("email"));
                student.setBranch(record.get("branch"));
                student.setSemester(Integer.parseInt(record.get("semester")));

                // Subjects split by |
                String subjectsRaw = record.get("subjects");
                List<String> subjects = List.of(subjectsRaw.split("\\|"));
                student.setSubjects(subjects);


                boolean hasBacklog = Boolean.parseBoolean(record.get("hasBacklog"));
                student.setHasBacklog(hasBacklog);

                // Backlog Subjects
                String backlogRaw = record.get("backlogSubjects");

                if (hasBacklog) {
                    if (backlogRaw == null || backlogRaw.isEmpty()) {
                        responses.add("Error for Enrollment "
                                + student.getEnrollmentNo()
                                + ": Backlog subjects required when hasBacklog is true.");
                        continue; // Skip this record
                    }
                    student.setBacklogSubjects(List.of(backlogRaw.split("\\|")));
                } else {
                    student.setBacklogSubjects(null);
                }


                // Validation
                Set<ConstraintViolation<StudentsDTO>> violations =
                        validator.validate(student);

                if (!violations.isEmpty()) {
                    throw new ConstraintViolationException(violations);
                }

                // Save student
                String res = addStudent(student);
                responses.add(res);
            }
        }
        return responses;
    }

    public RoomsDTO addRooms(RoomsDTO dto) {

        College college = collegeRepo.findById(helper.getCollegeIdByUserId())
                .orElseThrow(() -> new RuntimeException("College not found"));

        Rooms room = new Rooms();
        room.setRoomNumber(dto.getRoomNumber());
        room.setCapacity(dto.getCapacity());
        room.setCollege(college);
        room.setBlock(dto.getBlock());
        Rooms saved = roomsRepo.save(room);

        RoomsDTO response = new RoomsDTO();
        response.setRoomNumber(saved.getRoomNumber());
        response.setBlock(saved.getBlock());
        response.setCapacity(saved.getCapacity());
        return dto;
    }

    public List<String> saveRoomsFromCSV(MultipartFile file) throws IOException {
        College college = collegeRepo.findById(helper.getCollegeIdByUserId())
                .orElseThrow(() -> new RuntimeException("College not found"));

        List<Rooms> roomsToSave = new ArrayList<>(); // Use your Entity class here
        List<String> logs = new ArrayList<>();

        try (
                Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                CSVParser csvParser = new CSVParser(reader,
                        CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())
        ) {
            for (CSVRecord record : csvParser) {
                RoomsDTO dto = new RoomsDTO();
                dto.setRoomNumber(Integer.parseInt(record.get("roomNumber")));
                dto.setCapacity(Integer.parseInt(record.get("capacity")));
                dto.setBlock(record.get("block"));

                // 1. Validate the DTO
                Set<ConstraintViolation<RoomsDTO>> violations = validator.validate(dto);
                if (!violations.isEmpty()) {
                    throw new ConstraintViolationException(violations);
                }

                // 2. Map DTO to Entity (Assuming you have a mapper or manual conversion)
                Rooms roomEntity = new Rooms();
                        BeanUtils.copyProperties(dto,roomEntity);
                roomEntity.setCollege(college); // Link to the college

                roomsToSave.add(roomEntity);
                logs.add("Room " + dto.getRoomNumber() + " prepared");
            }
        }

        // 3. Single Batch Insert
        // This happens only if NO errors occurred in the loop above
        roomsRepo.saveAll(roomsToSave);

        return List.of("Successfully saved " + roomsToSave.size() + " rooms in batch.");
    }

    public List<GetSeatByCollege> getSeatBYCollege(Long college_id, Long exam_id) {
        return seatAllocationRepo.findSeatData(college_id, exam_id);
    }

    public List<Timetable> getTimetable(String branch,Integer semester)
    {
        Boolean completed=false;
        List<Timetable> timeTable= timetableRepo.findBybranchAndSemesterAndCompleted(branch,semester,completed);

        return timeTable;
    }

}