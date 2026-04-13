package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.entity.*;
import com.example.SmartSeatBackend.repository.*;
import org.springframework.transaction.annotation.Propagation; // Import this!
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import org.springframework.transaction.annotation.Transactional; // Use Spring's version
import org.springframework.transaction.annotation.Propagation;// This is what was red

@Service
@RequiredArgsConstructor
public class AllocationService {

    private final StudentRepository studentRepo;
    private final RoomsRepository roomRepo;
    private final CollegeRepository collegeRepo;
    private final SeatAllocationRepo seatRepo;
    private final TimetableRepo timetableRepo;
    private final NotificationRepository notificationRepo;

    /**
     * Propagation.REQUIRES_NEW ensures that each branch allocation is committed
     * to the DB immediately. This allows the next branch to see the occupied seats.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String allocateByGroupedMap(Map<String, List<String>> collegeToEnrMap,
                                       Long timetableId, String universityId) {

        Timetable timetable = timetableRepo.findById(timetableId)
                .orElseThrow(() -> new RuntimeException("Timetable not found"));

        if (collegeToEnrMap == null || collegeToEnrMap.isEmpty()) {
            return timetable.getSubjectName() + " (Sem " + timetable.getSemester() + "): No students.";
        }

        List<Notification> notifications = new ArrayList<>();
        List<SeatAllocation> allAllocations = new ArrayList<>();
        LocalDateTime istLocal = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime();
        SecureRandom sr = new SecureRandom();

        for (Map.Entry<String, List<String>> entry : collegeToEnrMap.entrySet()) {
            Long collegeId = Long.parseLong(entry.getKey());
            List<String> enrList = entry.getValue();

            List<Students> students = studentRepo.findAllByEnrollmentNoIn(enrList);
            List<Rooms> rooms = roomRepo.findByCollegeCollegeId(collegeId);

            if (students.isEmpty() || rooms.isEmpty()) continue;

            Collections.shuffle(students, sr);
            Collections.shuffle(rooms, sr);

            Map<String, Queue<Students>> branchMap = new LinkedHashMap<>();
            for (Students s : students) {
                branchMap.computeIfAbsent(s.getBranch(), k -> new LinkedList<>()).add(s);
            }

            // The grid logic now fetches "Fresh" data because of REQUIRES_NEW
            List<SeatAllocation> collegeResults = runGridLogic(rooms, branchMap, collegeId, timetable, sr);
            allAllocations.addAll(collegeResults);

            notifications.add(createNotification(collegeRepo.findUserIdByCollegeId(collegeId),
                    "college", "ALLOCATION_DONE", "Allocation done for " + timetable.getSubjectName(), istLocal));

            for (SeatAllocation sa : collegeResults) {
                notifications.add(createNotification(sa.getStudent().getEnrollmentNo(),
                        "student", "ALLOCATION_DONE", "Seat: " + sa.getRoom().getBlock() + " " + sa.getRoom().getRoomNumber(), istLocal));
            }
        }

        seatRepo.saveAll(allAllocations);
        notificationRepo.saveAll(notifications);

        return timetable.getSubjectName() + " allocation committed.";
    }

    private List<SeatAllocation> runGridLogic(List<Rooms> rooms,
                                              Map<String, Queue<Students>> branchMap,
                                              Long collegeId,
                                              Timetable timetable,
                                              SecureRandom sr) {

        College college = collegeRepo.findById(collegeId).orElseThrow();
        List<SeatAllocation> newAllocations = new ArrayList<>();
        List<String> branches = new ArrayList<>(branchMap.keySet());
        int branchIndex = 0;

        for (Rooms room : rooms) {
            int capacity = room.getCapacity();
            int rows = (int) Math.sqrt(capacity);
            int cols = (int) Math.ceil((double) capacity / rows);

            Students[][] grid = new Students[rows][cols];

            // Fetch students from PREVIOUSLY COMMITTED branches (e.g., Computer)
            List<SeatAllocation> existing = seatRepo.findExistingInRoom(
                    room.getId(), timetable.getExamDate(), timetable.getStartTime()
            );

            //System.out.println("Room " + room.getRoomNumber() + " - Existing students found: " + existing.size());

            for (SeatAllocation sa : existing) {
                if (sa.getRowNo() < rows && sa.getColNo() < cols) {
                    grid[sa.getRowNo()][sa.getColNo()] = sa.getStudent();
                }
            }

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (grid[r][c] != null) continue; // Slot taken by previous branch
                    if (branchMap.isEmpty()) break;

                    Students allocatedStudent = null;
                    int attempts = 0;

                    while (attempts < branches.size()) {
                        if (branches.isEmpty()) break;
                        if (branchIndex >= branches.size()) branchIndex = 0;

                        String branchName = branches.get(branchIndex);
                        Queue<Students> queue = branchMap.get(branchName);

                        if (queue == null || queue.isEmpty()) {
                            branchMap.remove(branchName);
                            branches.remove(branchIndex);
                            continue;
                        }

                        if (isSafe(grid, r, c, queue.peek())) {
                            allocatedStudent = queue.poll();
                            branchIndex = (branchIndex + 1) % (branches.isEmpty() ? 1 : branches.size());
                            break;
                        }

                        branchIndex = (branchIndex + 1) % (branches.isEmpty() ? 1 : branches.size());
                        attempts++;
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
                        newAllocations.add(seat);
                    }
                }
            }
        }
        return newAllocations;
    }

    private boolean isSafe(Students[][] grid, int r, int c, Students s) {
        String branch = s.getBranch();
        if (c - 1 >= 0 && grid[r][c - 1] != null && grid[r][c - 1].getBranch().equals(branch)) return false;
        if (r - 1 >= 0 && grid[r - 1][c] != null && grid[r - 1][c].getBranch().equals(branch)) return false;
        return true;
    }

    private Notification createNotification(String userId, String role, String type, String msg, LocalDateTime time) {
        return Notification.builder()
                .userId(userId).role(role).type(type).msg(msg)
                .isRead(false).createdAt(time).build();
    }
}