package com.example.SmartSeatBackend.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetSeatByCollege {

   private String enrollmentNo;
   private Long room_id;
   private Integer roomNumber;
   private String block;
   private Integer row_no;
   private Integer col_no;
}