package ru.knshnknd.chatovyonok.service;

import org.springframework.stereotype.Service;
import ru.knshnknd.chatovyonok.bot.BotMessages;
import ru.knshnknd.chatovyonok.bot.ChatovyonokBot;

import java.util.Random;

@Service
public class RandomPhraseService {
    // Случайная фраза с шансом chance
    public void sayRandomPhrase(ChatovyonokBot bot, org.telegram.telegrambots.meta.api.objects.Update update, int randomChance) {
        if (new Random().nextInt(100) < randomChance) {
            Runnable runnable = () -> {
                String chatId = String.valueOf(update.getMessage().getChatId());

                int randomNumber = new Random().nextInt(BotMessages.RANDOM_PHRASES.length);
                try {
                    Thread.sleep(120000);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }

                bot.sendMessage(chatId, BotMessages.RANDOM_PHRASES[randomNumber]);
            };

            new Thread(runnable).start();
        }
    }

}
