package com.example.SmartSeatBackend.service;

import com.cloudinary.utils.ObjectUtils;
import com.example.SmartSeatBackend.configurations.CloudinaryConfig;
import com.example.SmartSeatBackend.repository.StudentRepository;
import lombok.AllArgsConstructor;
import com.cloudinary.Cloudinary; // THIS is the correct one
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


@AllArgsConstructor
@Service
public class StudentService {

    private final Cloudinary cloudinary;

    private final StudentRepository studentRepo;


    public boolean checkIfImageExists(Long id) {
        return studentRepo.existsProfilePic(id);
    }

    public ResponseEntity<?> insertStudentImage(MultipartFile file, Long StudentId) throws  IOException{
        try{
            String url = uploadProfilePic(file,StudentId);
            int rowsUpdated = studentRepo.ProfilePic(StudentId,url);
            if(rowsUpdated>0){
                return ResponseEntity.ok().body("image added successfully");
            }
            else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("studnet not found");
            }
        }
        catch( IOException e){
            System.out.println("error in upload file or insertion of link");
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Upload failed");
        }

    }

    public String uploadProfilePic(MultipartFile file, Long studentId) throws IOException {
        String subFolderPath = "SmartSeatAi/students/";

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", subFolderPath,
                        "public_id", String.valueOf(studentId), // Sets filename to studentId
                        "resource_type", "image",
                        "overwrite", true,      // Overwrites if the student updates their pic
                        "invalidate", true     // Clears the old image from Cloudinary's CDN cache
                ));;

        return uploadResult.get("secure_url").toString();
    }

    public boolean belongsToCollege(Long targetStudentId,Long collegeId){
        return studentRepo.existsByStudentIdAndCollegeId(targetStudentId, collegeId);
    }
}
