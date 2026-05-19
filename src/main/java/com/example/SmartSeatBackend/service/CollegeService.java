package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.DTO.ExamSlotProjection;
import com.example.SmartSeatBackend.DTO.GetSeatByCollege;
import com.example.SmartSeatBackend.DTO.RoomsDTO;
import com.example.SmartSeatBackend.DTO.StudentsDTO;
import com.example.SmartSeatBackend.entity.*;
import com.example.SmartSeatBackend.repository.*;

import com.example.SmartSeatBackend.utility.HelperMethods;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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
    private final MessageService msgService;
    private final HelperMethods helper;

    @Cacheable(value = "studentsByCollege", key = "#collegeId")
    public List<Students> getStudents(Long collegeId){
        return studentRepo.findByCollegeId(collegeId);
    }

    //fetch college details for collegeid
    public Optional<College> getCollege(Long collegeId) {
        return collegeRepo.findByCollegeId(collegeId);
    }

    @CacheEvict(value = "studentsByCollege", key = "#collegeID")
    public String addStudent(@NotNull StudentsDTO dto, Long collegeID) {
        // 1. Validation checks (Throwing exceptions)
        if (studentRepo.existsById(dto.getEnrollmentNo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Enrollment number " + dto.getEnrollmentNo() + " already exists!");
        }

        if (studentRepo.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email " + dto.getEmail() + " is already registered!");
        }

        if (dto.getSemester() != null && dto.getSemester() == 1 && dto.isHasBacklog()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Semester 1 students cannot have backlogs.");
        }

        // 2. Mapping DTO to Entity
        Students student = new Students();
        BeanUtils.copyProperties(dto, student);
        student.setBranch(dto.getBranch().toUpperCase());
        student.setCollegeId(collegeID);

        List<SubjectStudent> subjectEntities = dto.getSubjects().stream()
                .map(code -> {
                    SubjectStudent back = new SubjectStudent();
                    back.setEnrollmentNo(dto.getEnrollmentNo());
                    back.setSubjectCode(code);
                    return back;
                }).toList();

        student.setSubjects(subjectEntities);
        // 3. Handle Backlog Mapping
        if (student.isHasBacklog()) {
            if (dto.getBacklogSubjects() == null || dto.getBacklogSubjects().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Backlog subjects list is required when hasBacklog is true.");
            }

            List<BacklogStudent> backlogEntities = dto.getBacklogSubjects().stream()
                    .map(code -> {
                        BacklogStudent back = new BacklogStudent();
                        back.setEnrollmentNo(dto.getEnrollmentNo());
                        back.setSubjectCode(code);
                        return back;
                    }).toList();

            student.setBacklogSubjects(backlogEntities);
        } else {
            student.setBacklogSubjects(null);
        }

        // 4. Security & Metadata
        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        student.setPassword(passwordEncoder.encode(rawPassword));

        // 5. Save
        studentRepo.save(student);


        msgService.sendRegistrationEvent(dto.getEmail(),rawPassword,dto.getName(), String.valueOf(collegeID));
        return student.getEnrollmentNo(); // Return only the key or a success string
    }


    @Transactional // Ensures atomicity: if one fails, nothing is saved
    @CacheEvict(value = "studentsByCollege", key = "#collegeId")
    public List<String> saveStudentsFromCSV(MultipartFile file, Long collegeId) throws IOException {
        List<String> logs = new ArrayList<>();
        List<Students> studentsToSave = new ArrayList<>();
        List<UniversityService.RegistrationDetail> registrationDetails = new ArrayList<>();
        List<String> requiredHeaders = Arrays.asList(
                "enrollmentNo", "name", "email", "mobileNumber",
                "branch", "specialization", "semester",
                "subjects", "hasBacklog", "backlogSubjects"
        );

        try (
                Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withIgnoreHeaderCase()
                        .withTrim())
        ) {

            //csv column name validation
            Map<String, Integer> headerMap = csvParser.getHeaderMap();

            if (headerMap == null || headerMap.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CSV file is empty.");
            }

            for (String header : requiredHeaders) {
                if (!headerMap.containsKey(header)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid CSV format. Missing required column: " + header);
                }
            }

            for (CSVRecord record : csvParser) {
                String enrollment = record.get("enrollmentNo");

                //Map CSV Row to DTO
                StudentsDTO dto = new StudentsDTO();
                dto.setEnrollmentNo(enrollment);
                dto.setName(record.get("name"));
                dto.setEmail(record.get("email"));
                dto.setMobileNumber(record.get("mobileNumber"));
                dto.setBranch(record.get("branch"));
                dto.setSpecialization(record.get("specialization")); // Added this line
                try {
                    dto.setSemester(Integer.parseInt(record.get("semester")));
                } catch (NumberFormatException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid semester format for enrollment: " + enrollment);
                }

                //Map Regular Subjects
                String subjectsRaw = record.get("subjects");
                dto.setSubjects((subjectsRaw != null && !subjectsRaw.isEmpty())
                        ? Arrays.asList(subjectsRaw.split("\\|"))
                        : new ArrayList<>());

                // Map Backlog Subjects
                boolean hasBacklog = Boolean.parseBoolean(record.get("hasBacklog"));
                dto.setHasBacklog(hasBacklog);
                if (hasBacklog) {
                    String backlogRaw = record.get("backlogSubjects");
                    if (backlogRaw == null || backlogRaw.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Backlog subjects required for enrollment: " + enrollment);
                    }
                    dto.setBacklogSubjects(Arrays.asList(backlogRaw.split("\\|")));
                }

                //Run Business Validations
                // If this fails (duplicate email/enrollment), it throws a ResponseStatusException
                validateStudentBusinessRules(dto);

                // 5. Transform DTO to Entity
                Students student = prepareFullStudentEntity(dto, collegeId);
                String rawPassword = UUID.randomUUID().toString().substring(0, 8);
                student.setPassword(passwordEncoder.encode(rawPassword));
                studentsToSave.add(student);

                //make list ot temp. data to send email after insrtion done
                registrationDetails.add(new UniversityService.RegistrationDetail(
                        dto.getEmail(),
                        rawPassword,
                        dto.getName(),
                        String.valueOf(collegeId)
                ));
                //logs.add("Validated: " + enrollment);
            }

            // 6. Bulk Save
            // This only runs if the loop finished without any exceptions
            studentRepo.saveAll(studentsToSave);
            //send studnets data to email service
            helper.sendRegistrationBatch(registrationDetails);
            logs.add("Bulk upload successful. Total saved: " + studentsToSave.size());

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read CSV file.");
        }

        return logs;
    }


    public RoomsDTO addRooms(RoomsDTO dto) throws Exception {

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

    public List<String> saveRoomsFromCSV(MultipartFile file) throws Exception {
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

    private void validateStudentBusinessRules(StudentsDTO dto) {
        if (studentRepo.existsById(dto.getEnrollmentNo())) {
            throw new RuntimeException("Enrollment already exists");
        }
        if (studentRepo.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (dto.getSemester() != null && dto.getSemester() == 1 && dto.isHasBacklog()) {
            throw new RuntimeException("Semester 1 students cannot have backlogs");
        }
    }

    private Students prepareFullStudentEntity(StudentsDTO dto, Long collegeId) {
        Students student = new Students();
        BeanUtils.copyProperties(dto, student);

        student.setCollegeId(collegeId);
        student.setBranch(dto.getBranch().toUpperCase());
        student.setSpecialization(dto.getSpecialization());
        // Security: Password Generation


        // A. Map Regular Subjects (SubjectStudent)
        if (dto.getSubjects() != null) {
            List<SubjectStudent> regularSubjects = dto.getSubjects().stream()
                    .map(code -> {
                        SubjectStudent sub = new SubjectStudent();
                        sub.setEnrollmentNo(dto.getEnrollmentNo());
                        sub.setSubjectCode(code);
                        return sub;
                    }).toList();
            student.setSubjects(regularSubjects);
        }

        // B. Map Backlog Subjects (BacklogStudent)
        if (dto.isHasBacklog() && dto.getBacklogSubjects() != null) {
            List<BacklogStudent> backlogs = dto.getBacklogSubjects().stream()
                    .map(code -> {
                        BacklogStudent bs = new BacklogStudent();
                        bs.setEnrollmentNo(dto.getEnrollmentNo());
                        bs.setSubjectCode(code);
                        return bs;
                    }).toList();
            student.setBacklogSubjects(backlogs);
        } else {
            student.setBacklogSubjects(null);
        }

        return student;
    }

    /**
     * Fetches only the physical room locations for a specific time slot
     */
    public List<Map<String, Object>> getRoomsByTimeSlot(String dateStr, String timeStr) throws Exception {
        Long collegeId = helper.getCollegeIdByUserId();

        // Parsing the strings from request into Java 8 Date/Time objects
        LocalDate date = LocalDate.parse(dateStr);
        LocalTime time = LocalTime.parse(timeStr);

        List<Map<String, Object>> rooms  = roomsRepo.findOccupiedRoomsBySlot(date, time, collegeId);

        return rooms != null ? rooms : Collections.emptyList();
    }

    public List<Map<String, Object>> getGroupedExamDetails() throws Exception {
        Long collegeId = helper.getCollegeIdByUserId();

        ZoneId istZone = ZoneId.of("Asia/Kolkata");
        LocalDate today = LocalDate.now(istZone);
        //LocalDate tenDaysHence = today.plusDays(10);

        // 1. Fetch data using the Projection from Repo
        List<ExamSlotProjection> result = timetableRepo.findUpcomingExams(
                collegeId,
                false
        );

        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Group by Date + Time string
        Map<String, List<ExamSlotProjection>> groupedBySlot = result.stream()
                .collect(Collectors.groupingBy(res ->
                        res.getExamDate().toString() + " " + res.getStartTime().toString()
                ));

        // 3. Transform into the final List of Slots
        return groupedBySlot.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> slot = new LinkedHashMap<>();
                    List<ExamSlotProjection> exams = entry.getValue();

                    slot.put("examDate", exams.get(0).getExamDate());
                    slot.put("startTime", exams.get(0).getStartTime());

                    // Map projection list to a list of simple maps for the "exams" field
                    List<Map<String, Object>> examList = exams.stream().map(e -> {
                        Map<String, Object> info = new HashMap<>();
                        info.put("id", e.getId());
                        info.put("examName", e.getExamName());
                        return info;
                    }).collect(Collectors.toList());

                    slot.put("exams", examList);
                    return slot;
                })
                .sorted(Comparator.comparing(m -> m.get("examDate").toString()))
                .collect(Collectors.toList());
    }
}