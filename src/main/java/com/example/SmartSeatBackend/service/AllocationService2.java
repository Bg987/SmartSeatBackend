//package com.example.SmartSeatBackend.service;
//
//import ai.timefold.solver.core.api.solver.SolverJob;
//import ai.timefold.solver.core.api.solver.SolverManager;
//import com.example.SmartSeatBackend.entity.Rooms;
//import com.example.SmartSeatBackend.entity.SeatAllocation2; // New Entity
//import com.example.SmartSeatBackend.entity.Students;
//import com.example.SmartSeatBackend.repository.RoomsRepository;
//import com.example.SmartSeatBackend.repository.SeatAllocationRepo2; // Separate Repo
//import com.example.SmartSeatBackend.repository.StudentRepository;
//import com.example.SmartSeatBackend.utility.SeatingPlanSolution;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ExecutionException;
//
//@Service
//@RequiredArgsConstructor
//public class AllocationService2 {
//
//    private final StudentRepository studentRepo;
//    private final RoomsRepository roomRepo;
//    private final SeatAllocationRepo2 seatRepo2; // Repository for the new table
//    private final SolverManager<SeatingPlanSolution, Long> solverManager;
//
//    @Transactional
//    public String allocateByGroupedMap(Map<String, List<String>> collegeToEnrMap) {
//        if (collegeToEnrMap == null || collegeToEnrMap.isEmpty()) {
//            return "No data provided for allocation.";
//        }
//
//        StringBuilder report = new StringBuilder("OptaPlanner Allocation: ");
//
//        for (Map.Entry<String, List<String>> entry : collegeToEnrMap.entrySet()) {
//            Long collegeId = Long.parseLong(entry.getKey());
//            List<String> enrList = entry.getValue();
//
//            // 1. Prepare Data
//            List<Students> students = studentRepo.findAllByEnrollmentNoIn(enrList);
//            List<Rooms> rooms = roomRepo.findByCollegeCollegeId(collegeId);
//
//            if (students.isEmpty() || rooms.isEmpty()) continue;
//
//            // 2. Initialize Empty Seats (Planning Entities)
//            List<SeatAllocation2> seatList = new ArrayList<>();
//            long idCounter = 1;
//            for (Rooms room : rooms) {
//                int capacity = room.getCapacity();
//                int rows = (int) Math.sqrt(capacity);
//                int cols = (int) Math.ceil((double) capacity / rows);
//
//                for (int r = 0; r < rows; r++) {
//                    for (int c = 0; c < cols; c++) {
//                        seatList.add(new SeatAllocation2(idCounter++, room, r, c, collegeId));
//                    }
//                }
//            }
//
//            // 3. Define the Problem
//            SeatingPlanSolution problem = new SeatingPlanSolution(students, seatList);
//
//            // 4. Solve
//            try {
//                SolverJob<SeatingPlanSolution, Long> solverJob = solverManager.solve(collegeId, problem);
//                // .getFinalBestSolution() waits for the solver to finish based on time limits
//                SeatingPlanSolution solution = solverJob.getFinalBestSolution();
//
//                // 5. Save the New Layout to the separate table
//                seatRepo2.deleteByCollegeId(collegeId);
//
//                List<SeatAllocation2> finalAllocations = solution.getSeatList().stream()
//                        .filter(s -> s.getStudent() != null)
//                        .toList();
//
//                seatRepo2.saveAll(finalAllocations);
//                report.append(String.format("College %d (Score: %s) ", collegeId, solution.getScore()));
//
//            } catch (InterruptedException | ExecutionException e) {
//                report.append("College ").append(collegeId).append(" Failed: ").append(e.getMessage());
//                Thread.currentThread().interrupt();
//            }
//        }
//
//        return report.toString();
//    }
//}