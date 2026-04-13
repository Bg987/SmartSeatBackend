package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.DTO.*;
import com.example.SmartSeatBackend.entity.*;
import com.example.SmartSeatBackend.repository.*;
import com.example.SmartSeatBackend.utility.HelperMethods;
import jakarta.annotation.PreDestroy;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private final HelperMethods helper;
    private final ExecutorService allocationQueue = Executors.newSingleThreadExecutor();

    //  Get All Subjects
    @Cacheable(value = "subjects")
    public List<Subject> getAllSubjects() {
        System.out.println("call");
        return subRepo.findAll();
    }

    // Add Subject
    @CacheEvict(value = "subjects", allEntries = true)
    public ResponseEntity<String> addSubject(SubjectDTO subjectdto) {

        boolean exists = subRepo.existsBySubjectIdAndBranchAndSemester(
                subjectdto.getSubjectId(),
                subjectdto.getBranch(),
                subjectdto.getSemester()
        );

        if (exists) {
            throw new IllegalArgumentException("Conflict: " + subjectdto.getSubjectId() + " (" + subjectdto.getSubjectName() +
                    ") is already registered for " + subjectdto.getBranch() +
                    " Semester " + subjectdto.getSemester());
        }
        Subject subject = new Subject();
        subject.setSubjectName(subjectdto.getSubjectName());
        subject.setSubjectId(subjectdto.getSubjectId());
        subject.setDepartment(subjectdto.getDepartment().toUpperCase());
        subject.setBranch(subjectdto.getBranch().toUpperCase());
        subject.setSemester(subjectdto.getSemester());

        subRepo.save(subject);

        return ResponseEntity.ok("Subject added successfully");
    }

    //  Upload Subjects CSV
    @CacheEvict(value = "subjects", allEntries = true)
    @Transactional
    public String saveSubjectsFromCSV(MultipartFile file) throws IOException {

        List<Subject> subjectsToSave = new ArrayList<>();

        // Track duplicates inside CSV
        Set<String> internalCheck = new HashSet<>();

        // Load existing subjects from DB once
        List<Subject> existingSubjects = subRepo.findAll();

        Set<String> existingKeys = existingSubjects.stream()
                .map(s -> s.getSubjectId() + "-" + s.getBranch() + "-" + s.getSemester())
                .collect(Collectors.toSet());

        try (
                Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withIgnoreHeaderCase()
                        .withTrim())
        ) {

            //check whether csv file format based on templete or not
            // 1. Get the parsed headers
            Map<String, Integer> headerMap = csvParser.getHeaderMap();


            // 2. Define the exact fields from your image
            String[] requiredFields = {"subjectId", "subjectName", "department", "branch", "semester"};

            // 3. Validation Logic
            if (headerMap == null) {
                throw new RuntimeException("Error: File is empty or headers are missing.");
            }

            for (String field : requiredFields) {
                if (!headerMap.containsKey(field)) {
                    // This is where you trigger your "format not proper" error
                    throw new RuntimeException("Error: Format not proper. Missing column: " + field);
                }
            }

            for (CSVRecord record : csvParser) {

                String temp = "";

                String subjectId = record.get("subjectId");
                String subjectName = record.get("subjectName");
                String department = record.get("department").toUpperCase();
                String branch = record.get("branch").toUpperCase();
                Integer semester = Integer.parseInt(record.get("semester"));

                // Manual validations
                if (subjectId.length() > 8 || subjectId.length() < 1) {
                    temp += "subjectCode must be between 1 and 8 characters, ";
                }

                if (subjectName.length() > 100 || subjectName.length() < 3) {
                    temp += "subjectName must be between 3 and 100 characters, ";
                }

                if (semester > 8 || semester < 1) {
                    temp += "semester must be between 1 and 8";
                }

                if (!temp.isEmpty()) {
                    temp += " -> Row: " + subjectId + " " + subjectName;
                    throw new IllegalArgumentException(temp);
                }

                // Create subject
                Subject subject = new Subject();
                subject.setSubjectId(subjectId);
                subject.setSubjectName(subjectName);
                subject.setDepartment(department);
                subject.setBranch(branch);
                subject.setSemester(semester);

                // Bean validation
                Set<ConstraintViolation<Subject>> violations = validator.validate(subject);
                if (!violations.isEmpty()) {
                    throw new ConstraintViolationException(
                            "Validation error in " + subject.getSubjectId(), violations);
                }

                // Unique key
                String uniqueKey = subjectId + "-" + branch + "-" + semester;

                // Check duplicate inside CSV
                if (!internalCheck.add(uniqueKey)) {
                    throw new IllegalArgumentException(
                            "Duplicate row found in CSV for: " + uniqueKey);
                }

                // Check duplicate in DB
                if (existingKeys.contains(uniqueKey)) {
                    throw new IllegalArgumentException(
                            "Conflict: " + subjectId +
                                    " already exists for Branch " + branch +
                                    " Sem " + semester);
                }

                subjectsToSave.add(subject);
            }
        }

        // Batch insert
        subRepo.saveAll(subjectsToSave);

        return "Successfully inserted " + subjectsToSave.size() + " subjects.";
    }


    //  Get All Colleges
    @Cacheable(value = "colleges")
    public ResponseEntity<List<User>> getAllColleges() {
        List<User> colleges = userRepo.findByRole(User.Role.college);
        return ResponseEntity.ok(colleges);
    }


    // Add College
    @CacheEvict(value = "colleges", allEntries = true)
    public ResponseEntity<?> addCollege(TempCollegeDTO collegeData) {

        if(userRepo.existsByMail(collegeData.getEmail())){
            throw new IllegalArgumentException("college(Email) already exist");
        }

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
         msgService.sendRegistrationEvent(
                 collegeData.getEmail(),
                 rawPassword,
                 collegeData.getCollegeName(),
                 null
         );

        return ResponseEntity.ok(Map.of("message","College added successfully."));
    }

    @Transactional
    public String saveCollegesFromCSV(MultipartFile file) throws IOException {
        List<String> requiredHeaders = Arrays.asList("name", "address", "mail", "contactNumber");

        // Lists to hold entities for batch saving
        List<User> usersToSave = new ArrayList<>();
        List<College> collegesToSave = new ArrayList<>();
        //use to store temp data for email service
        List<RegistrationDetail> registrationDetails = new ArrayList<>();
        try (
                Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                CSVParser csvParser = new CSVParser(
                        reader,
                        CSVFormat.DEFAULT
                                .withFirstRecordAsHeader()
                                .withIgnoreHeaderCase()
                                .withTrim())
        ) {
            Map<String, Integer> headerMap = csvParser.getHeaderMap();

            if (headerMap == null) throw new RuntimeException("CSV file is empty.");
            for (String header : requiredHeaders) {
                if (!headerMap.containsKey(header)) {
                    throw new RuntimeException("Error: Format not proper. Missing column: " + header);
                }
            }

            for (CSVRecord record : csvParser) {
                TempCollegeDTO collegeData = new TempCollegeDTO();
                collegeData.setCollegeName(record.get("name"));
                collegeData.setAddress(record.get("address"));
                collegeData.setEmail(record.get("mail"));
                collegeData.setContactNumber(record.get("contactNumber"));

                // Validation logic remains the same
                if (userRepo.existsByMail(collegeData.getEmail())) {
                    throw new IllegalArgumentException("College Email already exists: " + collegeData.getEmail());
                }

                Set<ConstraintViolation<TempCollegeDTO>> violations = validator.validate(collegeData);
                if (!violations.isEmpty()) {
                    throw new ConstraintViolationException(violations);
                }

                // 1. Prepare User Entity
                User userCollege = new User();
                userCollege.setName("Admin of " + collegeData.getCollegeName());
                userCollege.setMail(collegeData.getEmail());
                userCollege.setMobileNumber(collegeData.getContactNumber());
                userCollege.setRole(User.Role.college);

                String rawPassword = UUID.randomUUID().toString().substring(0, 8);
                userCollege.setPassword(passwordEncoder.encode(rawPassword));

                // 2. Prepare College Entity
                College college = new College();
                college.setName(collegeData.getCollegeName());
                college.setAddress(collegeData.getAddress());

                // 3. Link them (Hibernate handles the ID mapping during saveAll if configured correctly)
                college.setUser(userCollege);

                // Add to lists instead of saving now
                usersToSave.add(userCollege);
                collegesToSave.add(college);
                registrationDetails.add(new RegistrationDetail(
                        collegeData.getEmail(),
                        rawPassword,
                        collegeData.getCollegeName(),
                        null
                ));
            }

            // --- BATCH INSERTION ---
            if (!usersToSave.isEmpty()) {
                userRepo.saveAll(usersToSave);
                collegeRepo.saveAll(collegesToSave);
                //email service
                helper.sendRegistrationBatch(registrationDetails);
            }
        }
        return collegesToSave.size()+" Colleges added successfully.";
    }


    public College getCollegeByUser(Long userId) {

        return collegeRepo.findByUser_userId(userId)
                .orElseThrow(() ->
                        new RuntimeException("College not found with userId: " + userId)
                );
    }

    @Transactional
    public void saveAllExams(List<TimetableDTO> dtos) {

        System.out.println("calll");
        LocalDate minAllowedDate = LocalDate.now().plusDays(25); //
        List<Timetable> entitiesToSave = new ArrayList<>();

        for (TimetableDTO dto : dtos) {


            LocalDate examDate = LocalDate.parse(dto.getExamDate());

            // FIX: Check if THIS specific subject for THIS branch is already scheduled
            if (timetableRepo.existsBySubjectIdAndBranchAndCompletedFalse(dto.getSubjectId(), dto.getBranch())) {
                throw new IllegalArgumentException("Subject " + dto.getSubjectId() +
                        " for branch " + dto.getBranch() + " is already scheduled.");
            }

            // Check: Date must be >= 25 days from now
            if (examDate.isBefore(minAllowedDate)) {
                throw new IllegalArgumentException("Exams must be scheduled at least 25 days in advance.");
            }

            // Check for group conflicts (Is this Branch-Semester busy at this time?)
            List<Timetable> conflicts = timetableRepo.findGroupConflicts(
                    dto.getBranch(), dto.getSemester(), examDate, LocalTime.parse(dto.getStartTime())
            );

            if (!conflicts.isEmpty()) {
                throw new IllegalArgumentException("Conflict: Branch " + dto.getBranch() +
                        " already has an exam at " + dto.getStartTime());
            }

            // Mapping Logic
            Timetable entity = new Timetable();
            entity.setSubjectId(dto.getSubjectId());
            entity.setSubjectName(dto.getSubjectName());
            entity.setBranch(dto.getBranch());
            entity.setSemester(dto.getSemester());
            entity.setExamDate(examDate);
            entity.setStartTime(LocalTime.parse(dto.getStartTime()));
            // Inside the loop in saveAllExams
            if (dto.getDuration() == 0) {
                entity.setDurationMinutes(180); // Set a default if AI missed it
            } else {
                entity.setDurationMinutes(dto.getDuration());
            }
            entity.setAllocated(false);
            entity.setCompleted(false);

            entitiesToSave.add(entity);
        }
        timetableRepo.saveAll(entitiesToSave); //
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

    public void processAllocationQueue(Long examId, String universityId) {
        allocationQueue.submit(() -> {
            try {
                System.out.println("Queue: Starting allocation for Exam ID " + examId);
                executeMainWork(examId, universityId);
            } catch (Exception e) {
                System.err.println("Queue: Critical Error for Exam " + examId + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * The actual logic that runs inside the queue worker.
     * Note: @Async is removed here because the Executor handles the background thread.
     */
    private void executeMainWork(Long examId, String universityId) {
        // 1. Fetch Exam Details
        Timetable exam = timetableRepo.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        // 2. Fetch students for this SPECIFIC branch/subject/semester
        List<StudentEnrollmentDTO> students =
                studentRepo.findStudentsForExam(exam.getSubjectId(), exam.getBranch(), exam.getSemester());

        System.out.println("Queue: Found " + students.size() + " students for " + exam.getBranch());

        // 3. Group enrollment numbers by collegeId
        Map<String, List<String>> collegeToEnrMap = students.stream()
                .collect(Collectors.groupingBy(
                        StudentEnrollmentDTO::getCollegeId,
                        Collectors.mapping(StudentEnrollmentDTO::getEnrollmentNo, Collectors.toList())
                ));

        // 4. Trigger the Allocation Logic
        // This method in AllocationService MUST have @Transactional(propagation = Propagation.REQUIRES_NEW)
        String finalStatus = allocationService.allocateByGroupedMap(collegeToEnrMap, examId, universityId);

        System.out.println("Queue: Finished. Status: " + finalStatus);
    }

    @PreDestroy
    public void shutdownQueue() {
        System.out.println("Shutting down allocation worker queue...");
        allocationQueue.shutdown();
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


    public List<Timetable> getExamForGrading() {
        // Explicitly get the current time in India
        LocalDateTime nowIST = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

        return timetableRepo.findByCompletedFalse().stream()
                .filter(t -> {
                    // Combine Date and Time from DB, add the duration
                    LocalDateTime examEnd = LocalDateTime.of(t.getExamDate(), t.getStartTime())
                            .plusMinutes(t.getDurationMinutes());

                    // Return true only if current time is PAST the end time
                    return nowIST.isAfter(examEnd);
                })
                .collect(Collectors.toList());
    }

    public Boolean checkAllocationStatus(Long ExamID){
        return timetableRepo.findAllocationStatusById(ExamID);
    }



    //used to store temp. data at the time of college csv insertion and the time of betch DB insertion-
    //use this to get data for email service for college
    public record RegistrationDetail(String email, String password, String name,String collegeID) {}


//    public void processWithQuickDelay(String userId) {
//        CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS).execute(() -> {
//            System.out.println("call");
//            notificationService.sendNotification(userId,"data = "+userId);
//            processWithQuickDelay(userId);
//        });
//    }


}
