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
    public String allocateByGroupedMap(Map<String, List<String>> collegeToEnrMap) {
        if (collegeToEnrMap == null || collegeToEnrMap.isEmpty()) {
            return "No data provided for allocation.";
        }

        List<SeatAllocation> allAllocations = new ArrayList<>();
        StringBuilder statusReport = new StringBuilder();

        // Iterate over each college group
        for (Map.Entry<String, List<String>> entry : collegeToEnrMap.entrySet()) {
            Long collegeId = Long.parseLong(entry.getKey());
            List<String> enrList = entry.getValue();

            // 1. Fetch Students and Rooms for this specific college
            List<Students> students = studentRepo.findAllByEnrollmentNoIn(enrList);
            List<Rooms> rooms = roomRepo.findByCollegeCollegeId(collegeId);

            if (students.isEmpty() || rooms.isEmpty()) {
                statusReport.append("College ").append(collegeId).append(": Skipped (No rooms/students). ");
                continue;
            }

            // 2. Capacity Check
            int totalSeats = rooms.stream().mapToInt(Rooms::getCapacity).sum();
            if (students.size() > totalSeats) {
                statusReport.append("College ").append(collegeId).append(": Error (Insufficient Seats). ");
                continue;
            }

            // 3. Clear previous data for this college
            seatRepo.deleteByCollegeId(collegeId);

            // 4. Group by Branch for the "isSafe" logic
            Map<String, Queue<Students>> branchMap = new LinkedHashMap<>();
            for (Students s : students) {
                branchMap.computeIfAbsent(s.getBranch(), k -> new LinkedList<>()).add(s);
            }

            // 5. Run the Grid Allocation Logic
            List<SeatAllocation> collegeResults = runGridLogic(rooms, branchMap, collegeId);
            allAllocations.addAll(collegeResults);

            statusReport.append("College ").append(collegeId).append(": Success. ");
        }

        // 6. Bulk Save for performance
        seatRepo.saveAll(allAllocations);

        return "Process Finished: " + statusReport.toString();
    }

    private List<SeatAllocation> runGridLogic(List<Rooms> rooms, Map<String, Queue<Students>> branchMap, Long collegeId) {
        List<SeatAllocation> allocations = new ArrayList<>();
        List<String> branches = new ArrayList<>(branchMap.keySet());
        int branchIndex = 0;

        for (Rooms room : rooms) {
            int capacity = room.getCapacity();
            int rows = (int) Math.sqrt(capacity);
            int cols = (int) Math.ceil((double) capacity / rows);
            Students[][] grid = new Students[rows][cols];

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (branchMap.isEmpty()) break;

                    Students allocatedStudent = null;
                    int attempts = 0;

                    while (attempts < branches.size()) {
                        if (branches.isEmpty()) break;

                        String branch = branches.get(branchIndex);
                        branchIndex = (branchIndex + 1) % branches.size();

                        Queue<Students> queue = branchMap.get(branch);
                        if (queue == null || queue.isEmpty()) {
                            branchMap.remove(branch);
                            branches.remove(branch);
                            continue;
                        }

                        if (isSafe(grid, r, c, queue.peek())) {
                            allocatedStudent = queue.poll();
                            break;
                        }
                        attempts++;
                    }

                    // Fallback: If no "safe" student found, take whoever is next
                    if (allocatedStudent == null) {
                        for (String b : new ArrayList<>(branches)) {
                            Queue<Students> q = branchMap.get(b);
                            if (q != null && !q.isEmpty()) {
                                allocatedStudent = q.poll();
                                break;
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
        return allocations;
    }

    private boolean isSafe(Students[][] grid, int r, int c, Students s) {
        String branch = s.getBranch();
        if (c - 1 >= 0 && grid[r][c - 1] != null && grid[r][c - 1].getBranch().equals(branch)) return false;
        if (r - 1 >= 0 && grid[r - 1][c] != null && grid[r - 1][c].getBranch().equals(branch)) return false;
        // Optimization: Usually checking left and top is enough for sequential filling
        return true;
    }
}