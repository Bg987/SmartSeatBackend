package com.example.SmartSeatBackend.service;

import com.example.SmartSeatBackend.DTO.GetSeatByCollege;
import com.example.SmartSeatBackend.entity.*;
import com.example.SmartSeatBackend.repository.*;
import com.example.SmartSeatBackend.utility.BotUtility;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SmartSeatBot extends TelegramLongPollingBot {

    @Autowired private UserRepository userRepository;
    @Autowired private StudentRepository studentRepo;
    @Autowired private TelegramSessionRepository sessionRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UniversityService uniService;
    @Autowired private CollegeRepository collegeRepo;
    @Autowired private CollegeService colService;
    @Autowired private TimetableRepo timetableRepo;
    @Autowired private BotUtility botUtil;
    @Value("${telegram.bot.token}") private String botToken;
    @Value("${telegram.bot.name}") private String botName;

    // States
    private final Map<Long, String> userEmailState = new HashMap<>();
    private final Map<Long, Boolean> changePasswordState = new HashMap<>();

    @Override
    public String getBotUsername() { return botName; }

    @Override
    public String getBotToken() { return botToken; }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasCallbackQuery()) {
            String callData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            Optional<TelegramSession> activeSession = sessionRepo.findById(chatId);

            if(activeSession.isEmpty()){
                sendMessage(chatId, "Not allowed, first do login /start", true);
                return;
            }

            System.out.println(chatId+" "+callData);
            if (callData.startsWith("VIEW_COLLEGES_")) {
                // Extract the ID from "VIEW_COLLEGES_91"
                Long examId = Long.parseLong(callData.split("_")[2]);

                // Call your existing service method logic
                fetchAndShowColleges(chatId, examId);
            }

            else if (callData.startsWith("VIEW_SEAT_")) {
                // Data format: "VIEW_SEAT_16:91"
                String ids = callData.replace("VIEW_SEAT_", "");
                String[] parts = ids.split(":");

                Long collegeId = Long.parseLong(parts[0]);
                Long examId = Long.parseLong(parts[1]);

                // Now call your seating fetch method
                fetchAndShowSeating(chatId, collegeId, examId);
            }
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText().trim();
        int messageId = update.getMessage().getMessageId();

        // 1. Check for Active Session
        Optional<TelegramSession> activeSession = sessionRepo.findById(chatId);
        //if user login then this path
        if (activeSession.isPresent()) {

            //  Check if user is currently typing a NEW password
            if (changePasswordState.getOrDefault(chatId, false)) {
                processPasswordChange(chatId, messageText, activeSession.get(), messageId);
                return;
            }

            handleAuthenticatedAction(chatId, messageText, activeSession.get(), update,activeSession.get().getRole());
            return;
        }

        if (messageText.equals("/profile")||messageText.equals("/changePassword")||messageText.equals("/getSitting")||messageText.equals("/getExamPassword")) {
            userEmailState.remove(chatId);
            sendMessage(chatId, "Not allowed, first do login /start", true);
            return;
        }

        // entry if use first time
        if (messageText.equals("/start")) {
            userEmailState.remove(chatId);
            sendMessage(chatId, "Welcome to **SmartSeat Bot**. 👋\nPlease enter your **Registered Email**:", true);
        }
        //if user enter email for login
        else if (!userEmailState.containsKey(chatId)) {
            handleEmailInput(chatId, messageText, messageId);
        }
        //if user enter password after email
        else {
            handlePasswordInput(chatId, messageText, messageId);
        }
    }

    private void handleEmailInput(long chatId, String email, int msgId) {
        if (!email.contains("@")) {
            sendMessage(chatId, "❌ Invalid email format.", false);
            return;
        }

        //if already login in another device
        Optional<TelegramSession> existing = sessionRepo.findByIdentifier(email);
        if (existing.isPresent() && !existing.get().getChatId().equals(chatId)) {
            sendMessage(chatId, "⚠️ **Security Alert**: Already logged in elsewhere. Please /logout there first.", true);
            return;
        }

        //put main into temp. store later use in authentication
        userEmailState.put(chatId, email);
        sendMessage(chatId, "✅ Email accepted. Now enter your **Password**:", true);
    }

    private void handlePasswordInput(long chatId, String password, int msgId) {

        //fetch mail from temp. data
        String email = userEmailState.get(chatId);
        deleteMessage(chatId, msgId); // Privacy: delete raw password

        boolean success = false;
        String role = "";

        // Check Admins
        Optional<User> userOpt = userRepository.findByMail(email);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            role = userOpt.get().getRole().name();
            success = true;
        } else {
            // Check Students
            Optional<Students> studentOpt = studentRepo.findByEmail(email);
            if (studentOpt.isPresent() && passwordEncoder.matches(password, studentOpt.get().getPassword())) {
                role = "STUDENT";
                success = true;
            }
        }

        if (success) {
            completeLogin(chatId, email, role);
        } else {
            sendMessage(chatId, "❌ Incorrect password. Try /start again.", false);
            userEmailState.remove(chatId);
        }
    }

    private void completeLogin(long chatId, String email, String role) {
        TelegramSession session = new TelegramSession(chatId, email, role);

        //put data into databse
        sessionRepo.save(session);
        userEmailState.remove(chatId);

        StringBuilder welcomeMsg = new StringBuilder("🔓 **Login Successful!**\n");
        //common functionalities
        welcomeMsg.append("Welcome "+role+" Your dashboard is active.\n\n")
                .append("• Use /changePassword to update security\n")
                .append("• Use /profile to see details\n");

        //role based allowed functionalities
        if(role.equalsIgnoreCase("university")){
            welcomeMsg.append("• Use /getSitting to access seating allocation\n");
        }
        else if(role.equalsIgnoreCase("college")){
            welcomeMsg.append("• Use /getExamPassword to access exam password\n");
        }
        else if (role.equalsIgnoreCase("STUDENT")) {
            //welcomeMsg.append("Welcome, Student! Check your seat with /myseat.");
        } else {
//            welcomeMsg.append("Welcome "+role+" Your dashboard is active.\n\n")
//                    .append("• Use /changePassword to update security\n")
//                    .append("• Use /profile to see details\n")
//                    .append("• Use /logout to logout");
        }

        //last functionality
        welcomeMsg.append("• Use /logout to logout\n");
        sendMessage(chatId, welcomeMsg.toString(), true);
    }

    private void handleAuthenticatedAction(long chatId, String text, TelegramSession session, Update update,String role) {
        if (text.equalsIgnoreCase("/logout")) {
            deleteMessage(chatId, update.getMessage().getMessageId());
            sessionRepo.deleteById(chatId);
            sendMessage(chatId, "🛑 **Session Terminated.** All temporary data cleared.\n kindly delete chat to prevent unauthorize access of sensitive data.\n Use /start to login", true);
        }
        else if (text.equalsIgnoreCase("/changePassword")) {
            changePasswordState.put(chatId, true);
            sendMessage(chatId, "🔐 **Security Update**\nPlease enter your **New Password**:", true);
            deleteMessage(chatId, update.getMessage().getMessageId());
        }
        else if (text.equalsIgnoreCase("/profile")) {
            sendMessage(chatId, "👤 **Profile**\nEmail: " + session.getIdentifier() + "\nRole: " + session.getRole(), true);
        }

        else if(text.equalsIgnoreCase("/getSitting")){
            if(role.equals("university")){
                List<Timetable> exams = uniService.getCompleteExams();
                if (exams.isEmpty()) {
                    sendMessage(chatId, "📭 No allocated exam sittings found.", false);
                    return;
                }

                // 2. Send each exam as a nice "Card"
                for (Timetable exam : exams) {
                    try {
                        // Get the formatted SendMessage object (NOT a String)
                        SendMessage message = botUtil.formatTimetable(chatId,exam);

                        // Use execute() instead of your custom sendMessage string helper
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                        System.out.println("Error sending exam card: " + e.getMessage());
                    }
                }
            }
        }
        else if(text.equalsIgnoreCase("/getExamPassword")){
            if(!role.equalsIgnoreCase("college")){
                sendMessage(chatId, "❌only for college", false);
                return;
            }
            String res = botUtil.fetchAndSendExamPasswords(session.getIdentifier());
            sendMessage(chatId, res, false);
        }
        else {
            sendMessage(chatId, "Logged in as " + session.getRole() + ". Use /logout to exit.", false);
        }
    }

    private void processPasswordChange(long chatId, String newPass, TelegramSession session, int msgId) {
        deleteMessage(chatId, msgId); // Privacy delete

        try {
            if (session.getRole().equalsIgnoreCase("STUDENT")) {
                studentRepo.findByEmail(session.getIdentifier()).ifPresent(s ->
                        saveEncodedPassword(s, newPass, s::setPassword, studentRepo::save));
            } else {
                userRepository.findByMail(session.getIdentifier()).ifPresent(u ->
                        saveEncodedPassword(u, newPass, u::setPassword, userRepository::save));
            }
            sendMessage(chatId, "✅ **Success!** Your password has been updated.", true);
        } catch (Exception e) {
            sendMessage(chatId, "❌ **Error updating password.**", false);
        } finally {
            changePasswordState.remove(chatId);
        }
    }

    // Common Generic Method
    public <T> void saveEncodedPassword(T entity, String rawPassword,
                                        java.util.function.Consumer<String> passwordSetter,
                                        java.util.function.Function<T, T> saveFunction) {
        String encoded = passwordEncoder.encode(rawPassword);
        passwordSetter.accept(encoded);
        saveFunction.apply(entity);
    }

    private void deleteMessage(long chatId, Integer messageId) {
        DeleteMessage delete = new DeleteMessage();
        delete.setChatId(String.valueOf(chatId));
        delete.setMessageId(messageId);
        try { execute(delete); } catch (TelegramApiException e) { e.printStackTrace(); }
    }

    private void sendMessage(long chatId, String text, boolean isMarkdown) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        if (isMarkdown) message.setParseMode("Markdown");
        try { execute(message); } catch (TelegramApiException e) { e.printStackTrace(); }
    }

    private void fetchAndShowColleges(long chatId, Long examId) {
        // 1. Fetch data from your existing service
        List<College> colleges = uniService.getCollegeDetailsForExam(examId);

        if (colleges == null || colleges.isEmpty()) {
            sendMessage(chatId, "❌ No colleges are currently allocated for this exam sitting.", false);
            return;
        }

        // 2. Build the message header
        StringBuilder sb = new StringBuilder();
        sb.append("🏫 *Allocated Colleges* (Exam ID: ").append(timetableRepo.getExamNameByTimetable(examId)).append(")\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n\n");

        // 3. Loop through and extract only necessary fields
        // Inside your fetchAndShowColleges method
        for (int i = 0; i < colleges.size(); i++) {
            College c = colleges.get(i);

            // 1. Build the text for this specific college card
            String collegeText = String.format(
                    "🏫 *%d. %s*\n" +
                            "📍 *Address:* %s\n" +
                            "📧 *Admin:* `%s`\n" +
                            "📞 *Contact:* %s\n",
                    (i + 1), c.getName(),
                    c.getAddress(),
                    c.getUser().getMail(),
                    c.getUser().getMobileNumber()
            );

            // 2. Create the SendMessage object
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(collegeText);
            message.setParseMode("Markdown");

            // 3. Create the "View Seating" Button
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline = new ArrayList<>();

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("🪑 View Seating Allocation");

            // IMPORTANT: We pass "VIEW_SEAT_CollegeID:ExamID" so the callback knows BOTH
            button.setCallbackData("VIEW_SEAT_" + c.getCollegeId() + ":" + examId);

            rowInline.add(button);
            rowsInline.add(rowInline);
            markupInline.setKeyboard(rowsInline);
            message.setReplyMarkup(markupInline);

            // 4. Execute (Send) this individual card
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void fetchAndShowSeating(long chatId, Long collegeId, Long examId) {
        List<GetSeatByCollege> seats = colService.getSeatBYCollege(collegeId, examId);

        if (seats.isEmpty()) {
            sendMessage(chatId, "⚠️ No seating allocation found for this selection.", false);
            return;
        }

        // Grouping logic: We'll create a summary for each Room
        Map<Integer, List<GetSeatByCollege>> roomGroups = seats.stream()
                .collect(Collectors.groupingBy(GetSeatByCollege::getRoomNumber));

        sendMessage(chatId, "💺 *Detailed Seating Arrangement*\n━━━━━━━━━━━━━━━━━━", true);

        for (Map.Entry<Integer, List<GetSeatByCollege>> entry : roomGroups.entrySet()) {
            StringBuilder sb = new StringBuilder();
            List<GetSeatByCollege> roomSeats = entry.getValue();

            sb.append("🏫 *Room:* ").append(entry.getKey());
            sb.append(" | *Block:* ").append(roomSeats.get(0).getBlock()).append("\n");
            sb.append("━━━━━━━━━━━━━━━━━━\n");
            sb.append("`Enrollment    | Row | Col`\n");

            for (GetSeatByCollege s : roomSeats) {
                sb.append(String.format("`%-13s |  %-2d |  %-2d`\n",
                        s.getEnrollmentNo(), s.getRow_no(), s.getCol_no()));
            }

            sendMessage(chatId, sb.toString(), true);
        }
    }

    @PostConstruct
    public void init() { System.out.println("🤖 SmartSeat Bot Online: " + botName); }
}