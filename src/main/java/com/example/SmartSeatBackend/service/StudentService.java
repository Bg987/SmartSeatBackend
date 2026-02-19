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
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@AllArgsConstructor
@Service
public class StudentService {

    private final Cloudinary cloudinary;

    private final StudentRepository studentRepo;
    private FaceApiService faceApiService; // The service we created earlier

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
        catch(Exception e){
            System.out.println("error in upload file or insertion of link");
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Upload failed");
        }

    }

    public String uploadProfilePic(MultipartFile file, Long studentId) throws Exception {
        String subFolderPath = "SmartSeatAi/students/";

        //file bytes once to avoid reading the stream twice
        final byte[] fileBytes = file.getBytes();



        // Task A: Get Embeddings from Python API
        CompletableFuture<float[]> embeddingFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return faceApiService.getEmbeddingFromColab(file);
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

        // Wait for both to finish and get the results
        CompletableFuture.allOf(embeddingFuture, uploadFuture).join();

        float[] embeddings = embeddingFuture.get();
        String imageUrl = uploadFuture.get();

        // 4. Log and Save (Now you have both!)
        System.out.println("Embeddings: " + Arrays.toString(embeddings));

        // studentService.saveFaceData(studentId, imageUrl, embeddings);

        return imageUrl;
    }

    public boolean belongsToCollege(Long targetStudentId,Long collegeId){
        return studentRepo.existsByStudentIdAndCollegeId(targetStudentId, collegeId);
    }
}

//# import nest_asyncio
//# from fastapi import FastAPI, File, UploadFile
//# from deepface import DeepFace
//# import uvicorn
//# from pyngrok import ngrok
//# import numpy as np
//# import cv2
//
//# # 1. Apply nest_asyncio to run FastAPI in a notebook
//# nest_asyncio.apply()
//
//# app = FastAPI()
//
//# # 2. Pre-load model on GPU
//# # DeepFace automatically detects the GPU in Colab
//# MODEL = DeepFace.build_model("Facenet")
//
//# @app.post("/verify")
//# async def verify(file1: UploadFile = File(...), file2: UploadFile = File(...)):
//        #     img1 = cv2.imdecode(np.frombuffer(await file1.read(), np.uint8), cv2.IMREAD_COLOR)
//        #     img2 = cv2.imdecode(np.frombuffer(await file2.read(), np.uint8), cv2.IMREAD_COLOR)
//
//        #     # Using 'ssd' backend for the best speed/accuracy balance in Colab
//#     res1 = DeepFace.represent(img1, model_name="Facenet", detector_backend='ssd', enforce_detection=False)
//#     res2 = DeepFace.represent(img2, model_name="Facenet", detector_backend='ssd', enforce_detection=False)
//#     emb1 = np.array(res1[0]["embedding"])
//#     emb2 = np.array(res2[0]["embedding"])
//
//#     # Calculate Euclidean Distance
//#     # Formula: sqrt(sum((a - b)^2))
//        #     distance = np.linalg.norm(emb1 - emb2)
//
//#     # Threshold for FaceNet (Euclidean) is typically between 10.0 and 12.0
//        #     # You may need to tune this based on your lighting
//#     threshold = 10.5
//        #     verified = distance < threshold
//
//#     return {
//        #         "verified": bool(verified),
//#         "distance": round(float(distance), 4),
//        #         "metric": "euclidean"
//        #     }
//
//
//        # # 3. Setup Ngrok Tunnel (Get a free token from ngrok.com)
//# # Replace 'YOUR_NGROK_AUTH_TOKEN' with your actual token
//# # 3. Setup Ngrok Tunnel
//# NGROK_TOKEN = "39YbI92cXOQU1qrCjyod0rAaDh7_3nTuDBoPFRRjHyRhXa6J4"
//        # ngrok.set_auth_token(NGROK_TOKEN)
//
//# # Clear old tunnels to avoid "Too many sessions" error
//# ngrok.kill()
//# public_url = ngrok.connect(8000)
//# print(f"\n--- COPY THIS TO YOUR ANGULAR FRONTEND ---\n{public_url.public_url}\n------------------------------------------")
//
//# # 4. Start Server using the existing loop
//# import asyncio
//
//# if __name__ == "__main__":
//        #     config = uvicorn.Config(app=app, host="0.0.0.0", port=8000, log_level="info")
//#     server = uvicorn.Server(config)
//
//#     # This is the trick for Colab:
//        #     loop = asyncio.get_event_loop()
//#     loop.create_task(server.serve())