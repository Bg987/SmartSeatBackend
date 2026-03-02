package com.example.SmartSeatBackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "seat_allocation")
public class SeatAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer rowNo;
    private Integer colNo;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Rooms room;

    @ManyToOne
    @JoinColumn(name = "enrollment_no")
    private Students student;

    @Column(name = "college_id")
    private Long collegeId;

    @ManyToOne
    @JoinColumn(name = "timetable_id")
    private Timetable timetable;
}