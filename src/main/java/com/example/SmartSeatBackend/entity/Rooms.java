package com.example.SmartSeatBackend.entity;

import jakarta.persistence.*;




import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rooms")
public class Rooms {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "roomNumber")
    private Integer roomNumber;

    @Column(name="block")
    private String block;

   @Column(name="capacity")
    private Integer capacity;

    @ManyToOne
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

}