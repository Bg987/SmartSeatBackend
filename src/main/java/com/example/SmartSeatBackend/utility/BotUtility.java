package com.example.SmartSeatBackend.utility;

import com.example.SmartSeatBackend.DTO.GetSeatByCollege;
import com.example.SmartSeatBackend.entity.College;
import com.example.SmartSeatBackend.entity.Timetable;
import com.example.SmartSeatBackend.repository.CollegeRepository;
import com.example.SmartSeatBackend.repository.TimetableRepo;
import com.example.SmartSeatBackend.service.UniversityService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
@AllArgsConstructor
public class BotUtility {


    private final CollegeRepository collegeRepo;
    private final UniversityService uniService;

    public SendMessage formatTimetable(long chatId, Timetable t) {
        // 1. Build the professional UI Card using Markdown
        String text = String.format(
                "📅 *Exam Schedule: %s*\n" +
                        "━━━━━━━━━━━━━━━━━━\n" +
                        "📖 *Subject:* %s (%s)\n" +
                        "🌿 *Branch:* %s\n" +
                        "🎓 *Semester:* %d\n" +
                        "📆 *Date:* %s\n" +
                        "⏰ *Time:* %s (%d mins)\n" +
                        "━━━━━━━━━━━━━━━━━━\n" +
                        "✅ *Allocated:* %s\n" +
                        "📝 *Questions:* %s\n",
                t.getSubjectName(),
                t.getSubjectName(), t.getSubjectId(),
                t.getBranch(),
                t.getSemester(),
                t.getExamDate(),
                t.getStartTime(), t.getDurationMinutes(),
                t.isAllocated() ? "Yes 🟢" : "No 🔴",
                t.isQuestionGenrated() ? "Generated 📄" : "Pending ⏳"
        );

        // 2. Initialize the SendMessage object
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setParseMode("Markdown");

        // 3. Add the "View Colleges" button if the exam is allocated
        if (t.isAllocated()) {
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline = new ArrayList<>();

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("🏫 View Allocated Colleges");

            // This callback carries the Exam ID to the handleCallbackQuery method
            button.setCallbackData("VIEW_COLLEGES_" + t.getId());

            rowInline.add(button);
            rowsInline.add(rowInline);
            markupInline.setKeyboard(rowsInline);
            message.setReplyMarkup(markupInline);
        }

        return message;
    }

    public String fetchAndSendExamPasswords(String email) {
        try {
            // 1. Resolve College ID
            Optional<College> collegeOpt = collegeRepo.findByUserMail(email);
            if (collegeOpt.isEmpty()) {
                return "⚠️ *Account Error:* No college linked to your profile.";
            }

            Long collegeId = collegeOpt.get().getCollegeId();
            String url = "https://examportalsmartseatbackend.onrender.com/api/exam/getExamPasswordOpen/" + collegeId;

            // 2. Execute REST call
            RestTemplate restTemplate = new RestTemplate();
            // Catching as Object first to handle both List and Map responses
            Object response = restTemplate.getForObject(url, Object.class);

            // 3. Handle "EMPTY" Status (If response is a Map instead of a List)
            if (response instanceof Map) {
                Map<String, Object> respMap = (Map<String, Object>) response;
                if ("EMPTY".equals(respMap.get("status"))) {
                    return "⏳ *No Active Exams*\n";
                }
            }

            // 4. Handle List of Exams
            if (response instanceof List) {
                List<Map<String, Object>> examData = (List<Map<String, Object>>) response;

                if (examData.isEmpty()) {
                    return  "📭 No exams found for this college.";
                }

                StringBuilder sb = new StringBuilder("🔐 Authorized Exam Passwords\n");
                sb.append("━━━━━━━━━━━━━━━━━━\n");

                for (Map<String, Object> exam : examData) {
                    String rawPassword = String.valueOf(exam.get("examPassword"));
                    String displayPassword = rawPassword.equals("NOT_GENERATED")
                            ? "_Not Generated Yet_ ⏳"
                            : "`" + rawPassword + "`";

                    sb.append("📅 Exam: ").append(exam.get("examName")).append("\n");
                    sb.append("⏰ Start: ").append(exam.get("startTime")).append("\n");
                    sb.append("🔑 Pass: ").append(displayPassword).append("\n");
                    sb.append("━━━━━━━━━━━━━━━━━━\n");
                }

                return  sb.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ *SmartSeat Server Error:* Unable to fetch passwords.";
        }
        return "";
    }

}
