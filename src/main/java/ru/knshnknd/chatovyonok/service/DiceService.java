package ru.knshnknd.chatovyonok.service;

import org.springframework.stereotype.Service;
import ru.knshnknd.chatovyonok.bot.ChatovyonokBot;

@Service
public class DiceService {
    public void dice(ChatovyonokBot bot, org.telegram.telegrambots.meta.api.objects.Update update, int range) {
        String chatId = String.valueOf(update.getMessage().getChatId());
        String username = update.getMessage().getFrom().getUserName();

        if (range > 0) {
            Runnable runnable = () -> {
                bot.sendMessage(chatId, "Кидаю кубик от 1 до " + range + " для @" + username + "...");
                int randomNumber = (int) (Math.random() * range + 1);

                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }

                bot.sendMessage(chatId, "У @" + username + " выпало: " + randomNumber + "!");
            };

            new Thread(runnable).start();
        } else {
            bot.sendMessage(chatId, "@" + username + ", вы балда? Число должно быть больше нуля!");
        }
    }
}
