package com.example.SmartSeatBackend.service;


import com.example.SmartSeatBackend.DTO.TempCollegeDTO;
import com.example.SmartSeatBackend.configurations.passwordConfiguration;
import com.example.SmartSeatBackend.entity.User;
import com.example.SmartSeatBackend.repository.CollegeRepository;
import com.example.SmartSeatBackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.SmartSeatBackend.entity.College;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UniversityService {

    @Autowired
    private CollegeRepository collegeRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private passwordConfiguration passwordUtil;

   @Autowired
    private final PasswordEncoder passwordEncoder;



    public ResponseEntity<String> addCollege(TempCollegeDTO collegeData){
        User userCollege = new User();
        userCollege.setName("Admin of "+collegeData.getCollegeName());
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
        return ResponseEntity.ok("college added succesfully"+" email = "+collegeData.getEmail()+" password "+rawPassword);
    }


    public ResponseEntity<List<User>> getAllColleges() {
        List<User> colleges = userRepo.findByRole(User.Role.college);
        return ResponseEntity.ok(colleges);
    }


    public void saveCollegesFromCSV(MultipartFile file) throws IOException {

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

                //  Direct existing method call
                addCollege(tempCollege);
            }
        }
    }


}