package com.example.SmartSeatBackend.utility;

import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.example.SmartSeatBackend.entity.SeatAllocation2;

public class SeatingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                branchConflict(factory),
                studentConflict(factory)
        };
    }

    // Constraint 1: A student cannot be in two seats at once (Hard Constraint)
    private Constraint studentConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(SeatAllocation2.class,
                        Joiners.equal(SeatAllocation2::getStudent))
                .penalize(HardSoftScore.ofHard(100))
                .asConstraint("Student assigned to multiple seats");
    }

    // Constraint 2: No same branch neighbors (Hard Constraint)
    private Constraint branchConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(SeatAllocation2.class,
                        Joiners.equal(s -> s.getRoom().getId()), // Same room
                        Joiners.equal(s -> s.getStudent().getBranch())) // Same branch
                .filter((s1, s2) ->
                        Math.abs(s1.getRowNo() - s2.getRowNo()) + Math.abs(s1.getColNo() - s2.getColNo()) == 1)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Branch adjacency conflict");
    }
}
