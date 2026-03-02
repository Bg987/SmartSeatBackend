package com.example.SmartSeatBackend.utility;

import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.example.SmartSeatBackend.entity.SeatAllocation2;
import com.example.SmartSeatBackend.entity.Students;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@PlanningSolution
@Getter
@Setter
@NoArgsConstructor
public class SeatingPlanSolution {

    @ValueRangeProvider(id = "studentRange")
    private List<Students> studentList;

    @PlanningEntityCollectionProperty
    private List<SeatAllocation2> seatList;

    @PlanningScore
    private HardSoftScore score;

    public SeatingPlanSolution(List<Students> students, List<SeatAllocation2> seats) {
        this.studentList = students;
        this.seatList = seats;
    }
}
