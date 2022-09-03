package com.example.mybot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class Bot extends TelegramLongPollingBot {
    private final PlayerRepository repository;
    @Value("${bot.username}")
    private String username;
    @Value("${bot.token}")
    private String token;

    @Autowired
    public Bot(PlayerRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage()!=null) {
            Message message = update.getMessage();
            System.out.println(message.getChat().getId());
            long id = message.getFrom().getId();
            String chatId = message.getChatId().toString();
            if (!repository.findById(id).isPresent()) {
                repository.save(new Player(id, "@"+message.getFrom().getUserName()));
            } else {
                repository.deleteById(id);
                Player player = new Player(id, "@"+message.getFrom().getUserName());
                repository.save(player);
            }
            if (message.hasText()) {
                if (message.getText().startsWith("@all")) {
                    Iterable<Player> players = repository.findAll();
                    int x = 0;
                    String toSend = "";
                    for (Player player : players) {
                        x++;
                        toSend+=(player.getName().equals("@null")?"@Лох":player.getName())
                                +" ";
                        if (x == 5) {
                            send(chatId, toSend);
                            toSend="";
                            x = 0;
                        }
                    }
                    send(chatId, toSend);
                } else if (message.getText().equalsIgnoreCase("анрег")) {
                    repository.deleteById(id);
                    send(chatId, String.format(
                                    "%s тағыда сообщение жазғанға дейін анрегте болады(да да, я не послал тебя(Ричи на аве == +100 к вежливости))",
                                    message.getFrom().getFirstName()),
                            message.getMessageId());
                }
            }
        }
    }
    private void send(String chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void send(String chatId, String message, int messageId) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.setReplyToMessageId(messageId);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}