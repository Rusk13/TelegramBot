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
    String exist;

    String lastStateRegistration = "first";

    private String fullName;
    private String kidName;
    private String username;
    private int id;
    public void onUpdateReceived(Update update){
 // Create a message object

        if(update.hasMessage() && update.getMessage().hasText()) {
            String receivedMessage = update.getMessage().getText();
            SendMessage sendMessage = null;

            switch (receivedMessage) {
                case "/start":
                    sendMessage = handleStart(update);
                    break;
                case "Вход":
                    sendMessage = handleEnter(update);
                    break;
                case "Регистрация":
                    sendMessage = regUser(update);

                    break;
                default:
                    sendMessage = checkStatus(update);
                    break;
            }

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

//        if (update.getMessage().getText().equals("/start")) {
//            // Set variables
//            String message_text = update.getMessage().getText();
//            long chat_id = update.getMessage().getChatId();
//
//
//            message.setChatId(chat_id);
//            message.setText(message_text);
//
//            String user_first_name = update.getMessage().getChat().getFirstName();
//            String user_last_name = update.getMessage().getChat().getLastName();
//            String user_username = update.getMessage().getChat().getUserName();
//            long user_id = update.getMessage().getChat().getId();
//
//            setButtons(message,update);
//
//            try {
//                exist = mongoDB.check(user_first_name, user_last_name, toIntExact(user_id), user_username);
//                message.setText(mongoDB.getLastState(toIntExact(user_id)));
//                execute(message);
//
//            } catch (TelegramApiException e) {
//                e.printStackTrace();
//            }
//
//        }
//        else if(update.getMessage().getText().equals("Регистрация")) {
//            try {
//                message.setChatId(update.getMessage().getChatId());
//                message.setText("Давайте знакомиться!");
//                execute(message);
//                message.setText("Введите ваше имя и фамилию");
//                execute(message);
//                long user_id = update.getMessage().getChat().getId();
//                mongoDB.setLastState(toIntExact(user_id), "registration");
//
//
//
//                String name = update.getMessage().getText();
//                message.setText("Вы ввели " + name);
//                execute(message);
//            } catch (TelegramApiException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private SendMessage checkStatus(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        String lastState = mongoDB.getLastState(toIntExact(update.getMessage().getChat().getId()));
//        if(lastState.equals("null")){
//            return handleStart(update);
//        }

        if(lastState.equals("null")){
            switch (lastStateRegistration){
                case "first":
                    id = toIntExact(update.getMessage().getChat().getId());
                    fullName = update.getMessage().getText();
                    username = update.getMessage().getChat().getUserName();
                    lastStateRegistration = "second";
                    sendMessage.setText("Введите имя вашего ребенка:");
                    break;
                case "second":
                    kidName = update.getMessage().getText();
                    lastStateRegistration = "third";

                    mongoDB.addToDatabase(id,fullName,kidName,username);
                    mongoDB.setLastState(toIntExact(update.getMessage().getChat().getId()),"registered");
                    sendMessage.setText("Здравствуйте, " + fullName + "! Вы успешно зарегистрированы.");
                    break;

            }
        }

        return sendMessage;
    }

    private SendMessage regUser(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        exist = mongoDB.check(toIntExact(update.getMessage().getChat().getId()));
        if(exist.equals("exists")){
            sendMessage.setText("Вы уже зарегестрированы");
            return sendMessage;
        }

        mongoDB.setLastState(toIntExact(update.getMessage().getChat().getId()), "registration");

        sendMessage.setText("Введите ваше имя и фамилию:");
        return sendMessage;
    }

    private SendMessage handleStart(Update update){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        String user_first_name = update.getMessage().getChat().getFirstName();
        String user_last_name = update.getMessage().getChat().getLastName();
        String user_username = update.getMessage().getChat().getUserName();
        long user_id = update.getMessage().getChat().getId();

        sendMessage.setText("Приветствую! Я бот для управления вашим расписанием!");
        exist = mongoDB.check(toIntExact(user_id));
        setButtons(sendMessage,update);
        //sendMessage.setText(mongoDB.getLastState(toIntExact(user_id)));

        return sendMessage;
    }

    private SendMessage handleEnter(Update update){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        String userFullName;
        String userKidName;
        String username;
        long user_id = update.getMessage().getChat().getId();

        exist = mongoDB.check(toIntExact(user_id));

        if(exist.equals("exists")) {
            userFullName = mongoDB.getParentName(toIntExact(user_id));
            sendMessage.setText("Приветствую, " + userFullName);
        } else {
            sendMessage.setText("Похоже вас нет в нашей базе, попробуйте зарегистрироваться.");
        }

        //sendMessage.setText(mongoDB.getLastState(toIntExact(user_id)));

        return sendMessage;
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

        //sendMessage.setText("Hi! " + update.getMessage().getFrom().getFirstName());

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
