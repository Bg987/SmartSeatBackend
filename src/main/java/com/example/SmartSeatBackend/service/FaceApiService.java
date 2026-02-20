package com.example.SmartSeatBackend.service;

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
public class FaceApiService {

    // Replace this with the Ngrok URL printed in your Colab console
    private final String COLAB_URL = "";

    public float[] getEmbeddingFromColab(MultipartFile file) throws Exception {
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

        // 3. Send POST Request
        ResponseEntity<Map> response = restTemplate.postForEntity(COLAB_URL, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();

            if ("success".equals(responseBody.get("status"))) {
                List<Double> embeddingList = (List<Double>) responseBody.get("embedding");

                // Convert List<Double> to float[]
                float[] result = new float[embeddingList.size()];
                for (int i = 0; i < embeddingList.size(); i++) {
                    result[i] = embeddingList.get(i).floatValue();
                }
                return result;
            }
        }

        throw new RuntimeException("Failed to get embedding from AI server");
    }
}
