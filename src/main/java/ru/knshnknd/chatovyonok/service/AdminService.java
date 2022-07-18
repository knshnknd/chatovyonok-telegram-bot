package ru.knshnknd.chatovyonok.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.knshnknd.chatovyonok.bot.ChatovyonokBot;
import ru.knshnknd.chatovyonok.model.enitites.Update;
import ru.knshnknd.chatovyonok.model.repositories.UpdateRepository;

import java.util.List;

@Transactional
@Service
public class AdminService {

    @Autowired
    private UpdateRepository updateRepository;

    public void sendMessageToCertainChat(ChatovyonokBot bot, String chatId, String message) {
        bot.sendMessage(chatId, message);
    }

    public void sendMessageToAllChatsAsBot(ChatovyonokBot bot, String message) {
        List<Update> updateList = updateRepository.findAll();
        for (Update update : updateList) {
            bot.sendMessage(update.getChatId(), message);
        }
    }

}
