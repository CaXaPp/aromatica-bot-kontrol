package org.example.aromatica.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
@RequiredArgsConstructor
public class LatecomerBot extends TelegramLongPollingBot {

    private final DefaultAttendanceService attendanceService;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            String text = message.getText();
            User user = message.getFrom();
            String username = user.getUserName();
            Long chatId = message.getChatId();

            if (text != null) {
                switch (text) {
                    case "/arrival@aromatica_control_bot":
                        sendMessage(chatId, "Отправь фото вместе с этой командой.");
                        break;
                        case "/отчет":
                        String report = ReportFormatter.formatReport(attendanceService.getAllEmployees(), attendanceService);
                        sendMessage(message.getChatId(), report);
                        break;

                    case "/сброс":
                        attendanceService.resetAll();
                        sendMessage(chatId, "Данные сброшены.");
                        break;
                    default:
                        if (text.startsWith("/статус")) {
                            String[] parts = text.split(" ");
                            if (parts.length == 2) {
                                String userToCheck = parts[1].replace("@", "");
                                var e = attendanceService.getEmployee(userToCheck);
                                if (e != null) {
                                    String status = e.isDisqualified() ? "❌ Выбыл" : "✅ В гонке";
                                    String late = (e.getLateCountUnder20Min() == null || e.getLateCountUnder20Min() == 0)
                                            ? "Нет опозданий"
                                            : String.valueOf(e.getLateCountUnder20Min());
                                    String fine = (e.getFineSum() == null || e.getFineSum() == 0)
                                            ? "Нет штрафов"
                                            : e.getFineSum() + " сом";

                                    sendMessage(message.getChatId(), String.format(
                                            "@%s\nСтатус: %s\nОпоздания ≤20 мин: %s\nШтраф: %s",
                                            userToCheck, status, late, fine));
                                } else {
                                    sendMessage(chatId, "Пользователь не найден.");
                                }
                            }
                        }
                        break;
                }
            }

            if (message.hasPhoto()) {
                byte[] photoBytes = new byte[]{};
                String result = attendanceService.processArrival(username, user.getFirstName(), photoBytes);
                sendMessage(chatId, result);
            }
        }
    }


    @Override
    public String getBotUsername() {
        return "aromatica_control_bot";
    }

    @Override
    public String getBotToken() {
        return "7966592191:AAGYqmTiUgSys0BCL0HzjYVNuB1FBlr2EXQ";
    }

    private void sendMessage(Long chatId, String text) {
        try {
            execute(SendMessage.builder().chatId(chatId).text(text).build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

