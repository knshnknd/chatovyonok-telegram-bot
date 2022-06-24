package ru.knshnknd.chatovyonok.service;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.knshnknd.chatovyonok.bot.BotConfig;
import ru.knshnknd.chatovyonok.bot.ChatovyonokBot;

import java.util.Random;

public class RandomPhraseThread implements Runnable {
    Update update;
    ChatovyonokBot bot;

    public RandomPhraseThread(ChatovyonokBot bot, Update update) {
        this.update = update;
        this.bot = bot;
    }

    @Override
    public void run() {
        String chatId = String.valueOf(update.getMessage().getChatId());

        int randomNumber = new Random().nextInt(BotConfig.RANDOM_PHRASES.length);
        try {
            Thread.sleep(120000);
        } catch (InterruptedException e) {}

        bot.sendMessage(chatId, BotConfig.RANDOM_PHRASES[randomNumber]);
    }
}
