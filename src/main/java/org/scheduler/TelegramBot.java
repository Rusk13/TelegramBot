package org.scheduler;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static java.lang.Math.toIntExact;

public class TelegramBot extends TelegramLongPollingBot {

    MongoDB mongoDB = new MongoDB();
    public void onUpdateReceived(Update update){
        SendMessage message = new SendMessage(); // Create a message object
        if (update.getMessage().getText().equals("/start")) {
            // Set variables
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();


            message.setChatId(chat_id);
            message.setText(message_text);

            String user_first_name = update.getMessage().getChat().getFirstName();
            String user_last_name = update.getMessage().getChat().getLastName();
            String user_username = update.getMessage().getChat().getUserName();
            long user_id = update.getMessage().getChat().getId();

            setButtons(message,update);

            try {
                execute(message);
                mongoDB.check(user_first_name, user_last_name, toIntExact(user_id), user_username);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if(update.getMessage().getText().equals("Регистрация")) {
            try {
                message.setChatId(update.getMessage().getChatId());
                message.setText("Давайте знакомиться!");
                execute(message);
                message.setText("Введите ваше имя и фамилию");
                execute(message);



                String name = update.getMessage().getText();
                message.setText("Вы ввели " + name);
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

//    private synchronized void sendMsg(String chatId, String s) {
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.enableMarkdown(true);
//        sendMessage.setChatId(chatId);
//        sendMessage.setText(s);
//        try {
//            setButtons(sendMessage);
//            execute(sendMessage);
//
//        } catch (TelegramApiException e){
//            e.printStackTrace();
//        }
//    }

    private synchronized void setButtons(SendMessage sendMessage, Update update){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row;

        row = new KeyboardRow();
        row.add("Вход");
        keyboardRowList.add(row);

        row = new KeyboardRow();
        row.add("Регистрация");
        keyboardRowList.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRowList);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        sendMessage.setText("Hi! " + update.getMessage().getFrom().getFirstName());

//        try {
//            sendMessage.setChatId(update.getMessage().getChatId());
//            execute(sendMessage);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    public String getBotUsername(){
        return "LessonScheduler_Rusk13_bot";
    }

    @Override
    public String getBotToken(){
        return "5645243294:AAG-o7rBNeVQR-sv_KdSvGbgP1E74SDE4gw";
    }
}
