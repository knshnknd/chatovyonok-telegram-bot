package ru.knshnknd.chatovyonok.service;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.knshnknd.chatovyonok.bot.ChatovyonokBot;

public class DiceThread implements Runnable {
    Update update;
    ChatovyonokBot bot;
    int range;

    public DiceThread(ChatovyonokBot bot, Update update, int range) {
        this.update = update;
        this.bot = bot;
        this.range = range;
    }

    @Override
    public void run() {
        String chatId = String.valueOf(update.getMessage().getChatId());
        String username = update.getMessage().getFrom().getUserName();

        bot.sendMessage(chatId, "Кидаю кубик от 1 до " + range + " для @" + username + "...");
        int randomNumber = (int) (Math.random() * range + 1);

        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {}

        bot.sendMessage(chatId, "У @" + username + " выпало: " + randomNumber + "!");
    }
}
