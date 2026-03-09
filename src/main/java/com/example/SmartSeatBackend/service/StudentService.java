package com.example.SmartSeatBackend.service;

import com.cloudinary.utils.ObjectUtils;
import com.example.SmartSeatBackend.entity.Students;
import com.example.SmartSeatBackend.repository.StudentRepository;
import lombok.AllArgsConstructor;
import com.cloudinary.Cloudinary; // THIS is the correct one
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@AllArgsConstructor
@Service
public class StudentService {

    private final Cloudinary cloudinary;

    private final StudentRepository studentRepo;
    private FaceApiService faceApiService; // The service we created earlier


    public Students fetchStudent(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 2. Extract the Principal (which is "752" in your case)
        String userId = auth.getPrincipal().toString();
        return studentRepo.findByStudentId(Long.valueOf(userId));
    }
    //insert url into databse
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
        catch(Exception e){
            System.out.println("error in upload file or insertion of link");
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Upload failed");
        }

    }

    //insert embeddings in database and store image in cloudinary
    public String uploadProfilePic(MultipartFile file, Long studentId) throws Exception {
        String subFolderPath = "SmartSeatAi/students/";

        //file bytes once to avoid reading the stream twice
        final byte[] fileBytes = file.getBytes();



        // Task A: Get Embeddings from Python API
        CompletableFuture<float[]> embeddingFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return faceApiService.getEmbeddingFromColabAndStore(file,studentId);
            } catch (Exception e) {
                throw new RuntimeException("Embedding extraction failed", e);
            }
        });

        // Task B: Upload to Cloudinary
        CompletableFuture<String> uploadFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Map uploadResult = cloudinary.uploader().upload(fileBytes,
                        ObjectUtils.asMap(
                                "folder", subFolderPath,
                                "public_id", String.valueOf(studentId),
                                "resource_type", "image",
                                "overwrite", true,
                                "invalidate", true
                        ));
                return uploadResult.get("secure_url").toString();
            } catch (IOException e) {
                throw new RuntimeException("Cloudinary upload failed", e);
            }
        });

        //CompletableFuture.allOf(embeddingFuture, uploadFuture).join();

        float[] embeddings = embeddingFuture.get();
        String imageUrl = uploadFuture.join();

        return imageUrl;
    }

    public boolean checkIfImageExists(Long id) {
        return studentRepo.existsProfilePic(id);
    }

    public Long getVerifiedStudentId(String enrollmentNo, Long collegeId) {
        return studentRepo.findStudentIdByEnrollmentAndCollege(enrollmentNo, collegeId)
                .orElseThrow(() -> new RuntimeException("Student not found or doesn't belong to your college"));
    }
}
