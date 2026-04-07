package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.entity.*;
import com.example.SmartSeatBackend.repository.*;


import com.example.SmartSeatBackend.utility.HelperMethods;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AllocationService {

    private final HelperMethods helper;
    private final StudentRepository studentRepo;
    private final RoomsRepository roomRepo;
    private final CollegeRepository collegeRepo;
    private final SeatAllocationRepo seatRepo;
    private final TimetableRepo timetableRepo;
    private final NotificationRepository notificationRepo; // 1. Inject Repository

    @Transactional
    public String allocateByGroupedMap(Map<String, List<String>> collegeToEnrMap,
                                       Long timetableId,String universityId) {

        if (collegeToEnrMap == null || collegeToEnrMap.isEmpty()) {
            return timetableRepo.getExamNameByTimetable(timetableId)+" no students for this exam";
        }

        //  Fetch timetable properly from DB
        Timetable timetable = timetableRepo.findById(timetableId)
                .orElseThrow(() -> new RuntimeException("Timetable not found"));

        String examName= timetableRepo.getExamNameByTimetable(timetableId);
        List<Notification> notifications = new ArrayList<>(); // List to batch save
        List<SeatAllocation> allAllocations = new ArrayList<>();
        StringBuilder statusReport = new StringBuilder();
        ZonedDateTime istZone = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        LocalDateTime istLocal = istZone.toLocalDateTime();

        for (Map.Entry<String, List<String>> entry : collegeToEnrMap.entrySet()) {

            Long collegeId = Long.parseLong(entry.getKey());
            List<String> enrList = entry.getValue();

            List<Students> students = studentRepo.findAllByEnrollmentNoIn(enrList);
            List<Rooms> rooms = roomRepo.findByCollegeCollegeId(collegeId);
            Collections.shuffle(rooms); // Randomizes the list in place

            if (students.isEmpty() || rooms.isEmpty()) {
                statusReport.append("College ")
                        .append(collegeId)
                        .append(": Skipped (No rooms/students). ");
                continue;
            }

            int totalSeats = rooms.stream()
                    .mapToInt(Rooms::getCapacity)
                    .sum();

            if (students.size() > totalSeats) {
                statusReport.append("College ")
                        .append(collegeId)
                        .append(": Error (Insufficient Seats). ");
                continue;
            }

            Map<String, Queue<Students>> branchMap = new LinkedHashMap<>();
            for (Students s : students) {
                branchMap
                        .computeIfAbsent(s.getBranch(), k -> new LinkedList<>())
                        .add(s);
            }

            List<SeatAllocation> collegeResults =
                    runGridLogic(rooms, branchMap, collegeId, timetable);

            allAllocations.addAll(collegeResults);
            notifications.add(Notification.builder()
                    .userId(collegeRepo.findUserIdByCollegeId(collegeId))
                    .role("college")
                    .type("ALLOCATION_DONE")
                    .msg("Exam allocation completed for " +examName)
                    .isRead(false)
                    .createdAt(istLocal)
                    .build());

            for (SeatAllocation allocation : collegeResults) {
                notifications.add(Notification.builder()
                        .userId(allocation.getStudent().getEnrollmentNo())
                        .role("student")
                        .type("ALLOCATION_DONE")
                        .msg("Your seat for " + examName + " is allocated at " + allocation.getRoom().getBlock()+" "+allocation.getRoom().getRoomNumber())
                        .isRead(false)
                        .createdAt(istLocal)
                        .build());
            }

            statusReport.append("College ")
                    .append(collegeId)
                    .append(": Success. ");
        }

        seatRepo.saveAll(allAllocations);
        notifications.add(Notification.builder()
                .userId(universityId)
                .role("university")
                .type("ALLOCATION_DONE")
                .msg("Allocation process finished for " + examName)
                .isRead(false)
                .createdAt(istLocal)
                .build());

        notificationRepo.saveAll(notifications); // Batch save of notifications
        return timetableRepo.getExamNameByTimetable(timetableId)+" allocation done";
    }


    private List<SeatAllocation> runGridLogic(List<Rooms> rooms,
                                              Map<String, Queue<Students>> branchMap,
                                              Long collegeId,
                                              Timetable timetable) {
        College college = collegeRepo.findById(collegeId)
                .orElseThrow(() -> new RuntimeException("College not found"));

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

                        // Safety check: ensure index is still valid after a potential removal
                        if (branchIndex >= branches.size()) {
                            branchIndex = 0;
                        }

                        String branch = branches.get(branchIndex);
                        Queue<Students> queue = branchMap.get(branch);

                        if (queue == null || queue.isEmpty()) {
                            branchMap.remove(branch);
                            branches.remove(branchIndex); // Remove by index to stay in sync
                            // Do NOT increment branchIndex here because the next item
                            // shifted into the current index position
                            if (branches.isEmpty()) break;
                            continue;
                        }

                        // Try to allocate
                        if (isSafe(grid, r, c, queue.peek())) {
                            allocatedStudent = queue.poll();
                            // Move to next branch for the next seat
                            branchIndex = (branchIndex + 1) % branches.size();
                            break;
                        }

                        // If not safe, move to next branch and increment attempts
                        branchIndex = (branchIndex + 1) % branches.size();
                        attempts++;
                    }

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
                        seat.setCollege(college);
                  seat.setTimetable(timetable);

                        allocations.add(seat);
                    }
                }
            }
        }

        return allocations;
    }


    private boolean isSafe(Students[][] grid, int r, int c, Students s) {

        String branch = s.getBranch();

        if (c - 1 >= 0 && grid[r][c - 1] != null &&
                grid[r][c - 1].getBranch().equals(branch)) {
            return false;
        }

        if (r - 1 >= 0 && grid[r - 1][c] != null &&
                grid[r - 1][c].getBranch().equals(branch)) {
            return false;
        }

        return true;
    }
}