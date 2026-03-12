package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.DTO.*;
import com.example.SmartSeatBackend.entity.*;
import com.example.SmartSeatBackend.repository.*;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UniversityService {

    private final CollegeRepository collegeRepo;
    private final UserRepository userRepo;
    private final SubjectRepository subRepo;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final MessageService msgService;
    private final TimetableRepo timetableRepo;
    private final StudentRepository studentRepo;
    private final RoomsRepository roomRepo;
    private final AllocationService allocationService;
    private final NotificationService notificationService;
    private final SubjectStudentRepository subjectRepo;
    private final SeatAllocationRepo seatAllocationRepo;


    //  Get All Subjects
    @Cacheable(value = "subjects")
    public List<Subject> getAllSubjects() {
        return subRepo.findAll();
    }

    // Add Subject
    @CacheEvict(value = "subjects", allEntries = true)
    public ResponseEntity<String> addSubject(SubjectDTO subjectdto) {

        Subject subject = new Subject();
        subject.setSubjectName(subjectdto.getSubjectName());
        subject.setSubjectId(subjectdto.getSubjectId());
        subject.setDepartment(subjectdto.getDepartment());
        subject.setBranch(subjectdto.getBranch());
        subject.setSemester(subjectdto.getSemester());

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
                sub.setDepartment(record.get("department"));
                sub.setBranch(record.get("branch"));
                sub.setSemester(Integer.parseInt(record.get("semester").toString()));


                ResponseEntity<String> response = addSubject(sub);
                responses.add("SUCCESS: " + response.getBody());
            }
        }

        return responses;
    }


    //  Get All Colleges
    @Cacheable(value = "colleges")
    public ResponseEntity<List<User>> getAllColleges() {
        System.out.println("call");
        List<User> colleges = userRepo.findByRole(User.Role.college);
        return ResponseEntity.ok(colleges);
    }


    // Add College
    @CacheEvict(value = "colleges", allEntries = true)
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
        college.setDepartment(collegeData.getDepartment());
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
                tempCollege.setDepartment(record.get("department"));

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

    public College getCollegeByUser(Long userId) {

        return collegeRepo.findByUser_userId(userId)
                .orElseThrow(() ->
                        new RuntimeException("College not found with userId: " + userId)
                );
    }

    @Transactional // Ensures atomicity: all subjects save, or none do
    public void saveAllExams(List<TimetableDTO> dtos) {
        LocalDate minAllowedDate = LocalDate.now().plusDays(25);
        List<Timetable> entitiesToSave = new ArrayList<>();


        // 1. Validate and Map
        for (TimetableDTO dto : dtos) {
            LocalDate examDate = LocalDate.parse(dto.getExamDate());

            //check whether same subject incomplete exam already exist or not
            if (timetableRepo.existsBySubjectIdAndCompletedFalse(dto.getSubjectId())) {
                throw new IllegalArgumentException("Subject " + dto.getSubjectId() + " is already scheduled in the system.");
            }

            // Check: Date must be >=25 days from now
            if (examDate.isBefore(minAllowedDate)) {
                throw new IllegalArgumentException(
                        "Validation failed: Subject " + dto.getSubjectId() +
                                " is scheduled for " + examDate +
                                ". Exams must be scheduled at least 25 days in advance (Min: " + minAllowedDate + ")"
                );
            }

            //check whether same branch semester have exam on same day and same time or not
            List<Timetable> conflicts = timetableRepo.findGroupConflicts(
                    dto.getBranch(), dto.getSemester(), examDate, LocalTime.parse(dto.getStartTime())
            );

            if (!conflicts.isEmpty()) {
                throw new IllegalArgumentException("Conflict: " +" Branch "+ dto.getBranch() + " Semester " + dto.getSemester() +
                        " already has an exam on " + examDate + " at " + dto.getStartTime());
            }

            // Map DTO to Entity
            Timetable entity = new Timetable();
            entity.setSubjectId(dto.getSubjectId());
            entity.setSubjectName(dto.getSubjectName());
            entity.setBranch(dto.getBranch());
            entity.setSemester(dto.getSemester());
            entity.setExamDate(examDate);
            entity.setStartTime(LocalTime.parse(dto.getStartTime()));
            entity.setDurationMinutes(dto.getDuration());
            // Default flags
            entity.setAllocated(false);
            entity.setCompleted(false);
            entity.setQuestionGenrated(false);

            entitiesToSave.add(entity);
        }
        // 2. Batch Insert
        timetableRepo.saveAll(entitiesToSave);
    }


    public List<Timetable> getTimetable(String batchId) {
        return timetableRepo.findByBatchId(batchId);
    }

    public Long getCountOfStudents(Long collegeId)
    {
        return studentRepo.countByCollegeId(collegeId);
    }

    public Long getCountofRooms(Long collegeId)
    {
        return roomRepo.countByCollege_CollegeId(collegeId);
    }

    public List<Subject>getSubjectsDepartmentBranchSemester(String department,String branch,Integer semester)
    {
        return subRepo.findByDepartmentAndBranchAndSemester(department,branch,semester);
    }


    public void mainWork(Long examId){


        //fetch semester and subject of exam
        String subjectCode = timetableRepo.findsubjectIdById(examId);
        Integer semester = timetableRepo.findSemesterById(examId);

        //fetch reguler and backlog stunets for exam
        List<StudentEnrollmentDTO> students =
                studentRepo.findStudentsForExam(subjectCode, semester);

        //group enr numbers which map to collegeID
        Map<String, List<String>> collegeToEnrMap = students.stream()
                .collect(Collectors.groupingBy(
                        StudentEnrollmentDTO::getCollegeId,
                        Collectors.mapping(
                                StudentEnrollmentDTO::getEnrollmentNo,
                                Collectors.toList()
                        )
                ));

        // allocation
        String finalStatus =
                allocationService.allocateByGroupedMap(collegeToEnrMap, examId);


        System.out.println(finalStatus);

        //to send real time notification to client
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //fetch userid from auth context
        String userId = auth.getPrincipal().toString();
        //IOT - Sem 4 - CS301 - 2026-03-28 - payload example
        String payload= timetableRepo.getExamNameByTimetable(examId);
        notificationService.sendNotification(userId,finalStatus);
        //to prevent multiple times allocation for particuler college
    }

    //find collegeId's whose students appear for particuler exam
    public List<College> getCollegeDetailsForExam(Long TimetableId){
        return seatAllocationRepo.findCollegesByTimetableId(TimetableId);
    }



    public List<Timetable> getIncompleteExams(){
        return timetableRepo.findByAllocatedFalse();
    }

    public List<Timetable> getCompleteExams(){
        return timetableRepo.findByAllocatedTrue();
    }

    public Boolean checkAllocationStatus(Long ExamID){
        return timetableRepo.findAllocationStatusById(ExamID);
    }


    public void processWithQuickDelay(String userId) {
        CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS).execute(() -> {
            System.out.println("call");
            notificationService.sendNotification(userId,"data = "+userId);
            processWithQuickDelay(userId);
        });
    }
}
