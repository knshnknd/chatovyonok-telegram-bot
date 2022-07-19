package ru.knshnknd.chatovyonok.service;

import org.apache.log4j.Logger;
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
    private static final Logger log = Logger.getLogger(WikimediaImageService.class);

    @Autowired
    private UpdateRepository updateRepository;

    public void sendMessageToCertainChat(ChatovyonokBot bot, String chatId, String message) {
        bot.sendMessage(chatId, message);
        log.info("В чат с ID " + chatId + " отправлено сообщение: " + message);
    }

    public void sendMessageToAllChatsAsBot(ChatovyonokBot bot, String message) {
        List<Update> updateList = updateRepository.findAll();
        for (Update update : updateList) {
            bot.sendMessage(update.getChatId(), message);
            log.info("В чат с ID " + update.getChatId() + " отправлено сообщение: " + message);
        }
    }

}
