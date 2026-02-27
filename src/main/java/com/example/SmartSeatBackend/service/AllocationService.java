package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.DTO.GetSeatingPlan;
import com.example.SmartSeatBackend.entity.Rooms;
import com.example.SmartSeatBackend.entity.SeatAllocation;
import com.example.SmartSeatBackend.entity.Students;
import com.example.SmartSeatBackend.repository.RoomsRepository;
import com.example.SmartSeatBackend.repository.SeatAllocationRepo;
import com.example.SmartSeatBackend.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AllocationService {

    private final StudentRepository studentRepo;
    private final RoomsRepository roomRepo;
    private final SeatAllocationRepo seatRepo;

    @Transactional
    public String allocateByCollege(Long collegeId,String subjectCode) {

        List<Students> students = studentRepo.findStudentsWithoutBacklog(collegeId,subjectCode);
        List<Rooms> rooms = roomRepo.findByCollegeCollegeId(collegeId);

        if (students.isEmpty() || rooms.isEmpty()) {
            return "No students or rooms found for allocation.";
        }

        // 🔹 Check total seat capacity
        int totalSeats = rooms.stream()
                .mapToInt(Rooms::getCapacity)
                .sum();

        if (students.size() > totalSeats) {
            return "Not enough seats available for all students.";
        }

        // 🔹 Delete previous allocation
        seatRepo.deleteByCollegeId(collegeId);

        // 🔹 Branch grouping
        Map<String, Queue<Students>> branchMap = new LinkedHashMap<>();
        for (Students s : students) {
            branchMap
                    .computeIfAbsent(s.getBranch(), k -> new LinkedList<>())
                    .add(s);
        }

        boolean singleBranchWarning = branchMap.size() < 2;

        List<SeatAllocation> allocations = new ArrayList<>();

        for (Rooms room : rooms) {

            int capacity = room.getCapacity();

            int rows = (int) Math.sqrt(capacity);
            int cols = (int) Math.ceil((double) capacity / rows);

            Students[][] grid = new Students[rows][cols];

            List<String> branches = new ArrayList<>(branchMap.keySet());
            int branchIndex = 0;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {

                    if (branchMap.isEmpty())
                        break;

                    Students allocatedStudent = null;

                    int attempts = 0;

                    while (attempts < branches.size()) {

                        if (branches.isEmpty())
                            break;

                        String branch = branches.get(branchIndex);
                        branchIndex = (branchIndex + 1) % branches.size();

                        Queue<Students> queue = branchMap.get(branch);

                        if (queue == null || queue.isEmpty()) {
                            branchMap.remove(branch);
                            branches.remove(branch);
                            continue;
                        }

                        Students student = queue.peek();

                        if (isSafe(grid, r, c, student)) {
                            allocatedStudent = queue.poll();
                            break;
                        }

                        attempts++;
                    }

                    // 🔹 Fallback allocation
                    if (allocatedStudent == null) {
                        for (String branch : new ArrayList<>(branches)) {

                            Queue<Students> queue = branchMap.get(branch);

                            if (queue != null && !queue.isEmpty()) {
                                allocatedStudent = queue.poll();
                                break;
                            } else {
                                branchMap.remove(branch);
                                branches.remove(branch);
                            }
                        }
                    }

                    if (allocatedStudent != null) {

                        grid[r][c] = allocatedStudent;

                        SeatAllocation seat = new SeatAllocation();
                        seat.setRoom(room);
                        seat.setStudent(allocatedStudent);
                        seat.setRowNo(r);
                        seat.setColNo(c);
                        seat.setCollegeId(collegeId);

                        allocations.add(seat);
                    }
                }
            }
        }

        // 🔹 Bulk Save (Performance Optimized)
        seatRepo.saveAll(allocations);

        if (singleBranchWarning) {
            return "Allocation completed with warning: Only one branch present. Proper mixing not possible.";
        }

        return "Seat allocation completed successfully.";
    }

    // 🔹 Full adjacency safety check
    private boolean isSafe(Students[][] grid, int r, int c, Students s) {

        String branch = s.getBranch();

        // Left
        if (c - 1 >= 0 &&
                grid[r][c - 1] != null &&
                grid[r][c - 1].getBranch().equals(branch))
            return false;

        // Top
        if (r - 1 >= 0 &&
                grid[r - 1][c] != null &&
                grid[r - 1][c].getBranch().equals(branch))
            return false;

        // Right
        if (c + 1 < grid[0].length &&
                grid[r][c + 1] != null &&
                grid[r][c + 1].getBranch().equals(branch))
            return false;

        // Bottom
        if (r + 1 < grid.length &&
                grid[r + 1][c] != null &&
                grid[r + 1][c].getBranch().equals(branch))
            return false;

        return true;
    }



    public ResponseEntity<List<GetSeatingPlan>> getSeatingPlan(Long collegeId) {

        List<SeatAllocation> seats = seatRepo.findBycollegeId(collegeId);

        if (seats.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<GetSeatingPlan> response = seats.stream()
                .map(seat -> GetSeatingPlan.builder()
                        .enrollmentNo(seat.getStudent().getEnrollmentNo())
                        .name(seat.getStudent().getName())
                        .branch(seat.getStudent().getBranch())
                        .semester(seat.getStudent().getSemester())
                        .row(seat.getRowNo())
                        .column(seat.getColNo())
                        .room_id(seat.getRoom().getRoomNumber())
                        .build())
                .toList();

        return ResponseEntity.ok(response);
    }
}