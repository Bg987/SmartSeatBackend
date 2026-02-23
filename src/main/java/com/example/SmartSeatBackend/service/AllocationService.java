package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.entity.Rooms;
import com.example.SmartSeatBackend.entity.SeatAllocation;
import com.example.SmartSeatBackend.entity.Students;
import com.example.SmartSeatBackend.repository.RoomsRepository;
import com.example.SmartSeatBackend.repository.SeatAllocationRepo;
import com.example.SmartSeatBackend.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AllocationService {

    private final StudentRepository studentRepo;
    private final RoomsRepository roomRepo;
    private final SeatAllocationRepo seatRepo;

    @Transactional
    public String allocateByCollege(Long collegeId) {

        List<Students> students = studentRepo.findByCollegeId(collegeId);
        List<Rooms> rooms = roomRepo.findByCollegeCollegeId(collegeId);

        if (students.isEmpty() || rooms.isEmpty()) {
            return "No students or rooms found for allocation.";
        }

        // 🔹 Clear previous allocation
//        seatRepo.deleteByCollegeId(collegeId);

        // 🔹 Branch-wise grouping
        Map<String, Queue<Students>> branchMap = new LinkedHashMap<>();

        for (Students s : students) {
            branchMap
                    .computeIfAbsent(s.getBranch(), k -> new LinkedList<>())
                    .add(s);
        }

        boolean singleBranchWarning = false;

        if (branchMap.size() < 2) {
            singleBranchWarning = true;
            System.out.println("Warning: Only one branch available. Mixing not possible.");
        }

        List<String> branches = new ArrayList<>(branchMap.keySet());
        int branchIndex = 0;

        for (Rooms room : rooms) {

            int capacity = room.getCapacity();
            int rows = (int) Math.sqrt(capacity);
            int cols = capacity / rows;

            Students[][] grid = new Students[rows][cols];
            Set<String> usedBranches = new HashSet<>();

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {

                    int attempts = 0;
                    boolean allocated = false;

                    // 🔹 Try safe round-robin allocation
                    while (attempts < branches.size()) {

                        String branch = branches.get(branchIndex);
                        branchIndex = (branchIndex + 1) % branches.size();

                        Queue<Students> queue = branchMap.get(branch);

                        if (queue == null || queue.isEmpty()) {
                            attempts++;
                            continue;
                        }

                        Students student = queue.peek();

                        if (isSafe(grid, r, c, student)) {

                            grid[r][c] = queue.poll();
                            usedBranches.add(student.getBranch());

                            saveSeat(room, student, r, c, collegeId);
                            allocated = true;
                            break;
                        }

                        attempts++;
                    }

                    //  Fallback allocation (no empty seats)
                    if (!allocated) {
                        for (String branch : branches) {

                            Queue<Students> queue = branchMap.get(branch);

                            if (queue != null && !queue.isEmpty()) {

                                Students student = queue.poll();
                                grid[r][c] = student;
                                usedBranches.add(student.getBranch());

                                saveSeat(room, student, r, c, collegeId);
                                break;
                            }
                        }
                    }
                }
            }

            // 🔹 Smart mixing validation
            long remainingBranches =
                    branchMap.values()
                            .stream()
                            .filter(q -> !q.isEmpty())
                            .count();

            if (usedBranches.size() < 2 && remainingBranches >= 1) {
                System.out.println(
                        "Limited branch mixing in room "
                                + room.getRoomNumber()
                                + " (Uneven distribution)"
                );
            }
        }

        if (singleBranchWarning) {
            return "Allocation completed with warning: Only one branch present. Proper mixing not possible.";
        }

        return "Seat allocation completed successfully.";
    }

    // 🔹 Safety check (Left & Top)
    private boolean isSafe(Students[][] grid,
                           int r,
                           int c,
                           Students s) {

        // Left seat check
        if (c - 1 >= 0 &&
                grid[r][c - 1] != null &&
                grid[r][c - 1].getBranch().equals(s.getBranch()))
            return false;

        // Top seat check
        if (r - 1 >= 0 &&
                grid[r - 1][c] != null &&
                grid[r - 1][c].getBranch().equals(s.getBranch()))
            return false;

        return true;
    }

    private void saveSeat(Rooms room,
                          Students student,
                          int row,
                          int col,
                          Long collegeId) {

        SeatAllocation seat = new SeatAllocation();

        seat.setRoom(room);
        seat.setStudent(student);
        seat.setRowNo(row);
        seat.setColNo(col);
        seat.setCollegeId(collegeId);

        seatRepo.save(seat);
    }
}