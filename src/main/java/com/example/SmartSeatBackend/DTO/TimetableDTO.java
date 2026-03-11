package com.example.SmartSeatBackend.DTO;


import lombok.Data;

@Data
public class TimetableDTO {
    private String subjectId;
    private String subjectName;
    private String branch;
    private Integer semester;
    private String examDate;
    private String startTime;
    private Integer duration;
}