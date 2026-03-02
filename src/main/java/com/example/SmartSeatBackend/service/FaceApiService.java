package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.repository.StudentEmbeddingRepository;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class FaceApiService {

    private  final StudentEmbeddingRepository embeddingRepository; // Inject the new repository
    // ngrok url for python api call
    private final String COLAB_URL = "https://nonswimming-nonseriously-lester.ngrok-free.dev/get-embedding";

    public float[] getEmbeddingFromColabAndStore(MultipartFile file,Long studentID) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        // 1. Prepare Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 2. Prepare Body (Multipart Request)
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // We wrap the MultipartFile bytes in a Resource so RestTemplate can send it
        ByteArrayResource contentsAsResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        body.add("file", contentsAsResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        //POST Request
        ResponseEntity<Map> response = restTemplate.postForEntity(COLAB_URL, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();

            if ("success".equals(responseBody.get("status"))) {
                List<Double> embeddingList = (List<Double>) responseBody.get("embedding");

                // Convert List<Double> to float[] to store in database
                float[] result = new float[embeddingList.size()];
                for (int i = 0; i < embeddingList.size(); i++) {
                    result[i] = embeddingList.get(i).floatValue();
                }

                String vectorString = java.util.Arrays.toString(result);
                //System.out.println(vectorString);
                // Use the native upsert  to store embeddings
                embeddingRepository.upsertEmbedding(studentID, vectorString);
                return result;
            }
        }
        throw new RuntimeException("Failed to get embedding from AI server");
    }
}
