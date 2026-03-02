package com.example.SmartSeatBackend.entity;


import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@PlanningEntity
@Getter @Setter @NoArgsConstructor
public class SeatAllocation2 {

    private Long id; // Unique ID for the seat
    private Rooms room;
    private int rowNo;
    private int colNo;
    private Long collegeId;

        // This is the field OptaPlanner changes
        @PlanningVariable(valueRangeProviderRefs = "studentRange")
        private Students student;

        public SeatAllocation2(Long id, Rooms room, int row, int col, Long collegeId) {
            this.id = id;
            this.room = room;
            this.rowNo = row;
            this.colNo = col;
            this.collegeId = collegeId;
        }

}
