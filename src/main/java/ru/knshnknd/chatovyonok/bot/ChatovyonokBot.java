package ru.knshnknd.chatovyonok.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.knshnknd.chatovyonok.service.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

@Component
public class ChatovyonokBot extends TelegramLongPollingBot {

    @Autowired
    private UpdateService updateService;
    @Autowired
    private BotClientService botClientService;
    @Autowired
    private WeatherService weatherService;
    @Autowired
    private YoutubeService youtubeService;
    @Autowired
    private RecipeService recipeService;
    @Autowired
    private DiceService diceService;
    @Autowired
    private RandomPhraseService randomPhraseService;
    @Autowired
    private WikimediaImageService wikimediaImageService;

    @Scheduled(cron = "0 0 2 * * *")
    public void timeForEverydayForecast() {
        weatherService.sendForecastToAllSubscribed(this);
    }

    @Scheduled(cron = "0 0 6 * * *")
    public void timeForEverydayWise() {
        botClientService.resetAllWiseLimitCount();
        botClientService.resetAllAttacks();
        botClientService.sendWiseToAllSubscribed(this);
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {
            Message message = update.getMessage();
            User user = message.getFrom();
            String currentChatId = message.getChatId().toString();

            // Записываем счётчик вызова бота для статистики в БД
            updateService.addUpdate(currentChatId);

            // Шанс 3%, что бот скажет коронную фразу в ответ на вызов через 2 минуты
            randomPhraseService.sayRandomPhrase(this, update, 3);

            if (message.hasText()) {
                // Разбиваем полученное сообщение на две части: команду и текст после команды
                String[] textFromMessage = message.getText().split(" ", 2);

                switch (textFromMessage[0].toLowerCase()) {
                    case "/start@chatovyonokbot", "/start" -> {
                        sendMessage(currentChatId, BotMessages.START_TEXT);
                        botClientService.addBotClientIfNotExist(user);
                    }

                    case "/help@chatovyonokbot", "/help" -> {
                        sendMessage(currentChatId, BotMessages.HELP_TEXT);
                    }

                    // Случайная мудрость
                    case "/wise@chatovyonokbot", "/wise", "/wi" -> {
                        sendMessage(currentChatId, botClientService.getRandomWiseForUser(user));
                    }

                    // Узнать счётчик мудростей у пользователя
                    case "/wise_count@chatovyonokbot", "/wise_count", "/wc" -> {
                        sendMessage(currentChatId, botClientService.getWiseCountForUser(user));
                    }

                    // Согласиться получать в чат одну мудрость каждый день
                    case "/wise_subscribe@chatovyonokbot", "/wise_subscribe", "/wise_sub", "/ws" -> {
                        botClientService.subscribeToWise(currentChatId);
                        sendMessage(currentChatId, BotMessages.WISE_SUBSCRIPTION_MESSAGE);
                    }

                    // Согласиться получать в чат одну мудрость каждый день
                    case "/wise_unsubscribe@chatovyonokbot", "/wise_unsubscribe", "wise_unsub", "/wu" -> {
                        botClientService.unsubscribeFromWise(currentChatId);
                        sendMessage(currentChatId, BotMessages.WISE_UNSUBSCRIPTION_MESSAGE);
                    }

                    // Запрос прогноза погоды по городу
                    case "/weather@chatovyonokbot", "/weather", "/we" -> {
                        if (BotUtils.isTextMessageHasAnyWordsMore(textFromMessage)) {
                            sendMessage(currentChatId, weatherService.getFullWeatherForecast(textFromMessage[1].trim()));
                        } else {
                            sendMessage(currentChatId, BotMessages.WEATHER_EMPTY_REQUEST_MESSAGE);
                        }
                    }

                    // Узнать общее количество мудростей
                    case "/wise_number@chatovyonokbot", "/wise_number" -> {
                        sendMessage(currentChatId, botClientService.howManyWisesDoesBotKnow());
                    }

                    // Узнать общее количество мудростей
                    case "/wise_attack@chatovyonokbot", "/wise_attack", "/wa" -> {
                        if (BotUtils.isTextMessageHasAnyWordsMore(textFromMessage)) {
                            botClientService.wiseAttack(this, update, user, textFromMessage[1]);
                        } else {
                            sendMessage(currentChatId, BotMessages.WISE_ATTACK_EMPTY_REQUEST);
                        }
                    }

                    // Согласие на получения прогноза погоды определённого города каждый день в определённый чат
                    case "/weather_subscribe@chatovyonokbot", "/weather_subscribe", "/weather_sub", "/wes" -> {
                        if (BotUtils.isTextMessageHasAnyWordsMore(textFromMessage)) {
                            weatherService.subscribeToWeather(this, currentChatId, textFromMessage[1].trim());
                        } else {
                            sendMessage(currentChatId, BotMessages.WEATHER_SUBSCRIPTION_EMPTY_REQUEST_MESSAGE);
                        }
                    }

                    // Отказ на прогноз погоды каждый день
                    case "/weather_unsubscribe@chatovyonokbot", "/weather_unsubscribe", "/weu" -> {
                        weatherService.unsubscribeFromWeather(currentChatId);
                        sendMessage(currentChatId, BotMessages.WEATHER_UNSUBSCRIPTION_MESSAGE);
                    }

                    // Случайный рецепт
                    case "/recipe@chatovyonokbot", "/recipe" -> {
                        sendMessage(currentChatId, recipeService.getRandomRecipe());
                    }

                    // Узнать количество рецептов
                    case "/recipes_number@chatovyonokbot", "/recipes_number" -> {
                        sendMessage(currentChatId, recipeService.getRecipesNumberMessage());
                    }

                    // Поиск видео из Youtube по ключевым словам
                    case "/youtube@chatovyonokbot", "/youtube" -> {
                        if (BotUtils.isTextMessageHasAnyWordsMore(textFromMessage)) {
                            sendMessage(currentChatId, youtubeService.getYoutubeVideo(textFromMessage[1]));
                        } else {
                            sendMessage(currentChatId, BotMessages.YOUTUBE_EMPTY_REQUEST_MESSAGE);
                        }
                    }

                    // Узнать от бота случайный ответ на вопрос типа да-нет
                    case "/answer@chatovyonokbot", "/answer" -> {
                        int randomAnswerNumber = new Random().nextInt(BotMessages.RANDOM_ANSWERS.length);
                        sendMessage(currentChatId, "Отвечаю: " + BotMessages.RANDOM_ANSWERS[randomAnswerNumber]);
                    }

                    // Кинуть кубик от 1 до 6 (или другое число)
                    case "/dice@chatovyonokbot", "/dice" -> {
                        if (BotUtils.isTextMessageHasAnyWordsMore(textFromMessage)) {
                            if (BotUtils.isTextInteger(textFromMessage[1])) {
                                diceService.dice(this, update, Integer.parseInt(textFromMessage[1]));
                            } else {
                                sendMessage(currentChatId, BotMessages.DICE_ERROR_MESSAGE);
                            }
                        } else {
                            diceService.dice(this, update, 6);
                        }
                    }

                    case "/art@chatovyonokbot", "/art" -> {
                        wikimediaImageService.getRandomImageFromWikimedia(this, update);
                    }
                }
            }
        }
    }

    public void sendMessage(String chatId, String message) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(message)
                    .build());
        } catch (TelegramApiException e) {
            System.out.println(e);
        }
    }

    public void sendImageWithMessage(String chatId, InputStream file, String message) {
        try {
            execute(SendPhoto.builder()
                    .chatId(chatId)
                    .photo(new InputFile(file, file.toString()))
                    .caption(message)
                    .build());
        } catch (TelegramApiException e) {
            System.out.println(e);
        }
    }

    @Override
    public String getBotUsername() {
        return BotKeysConfig.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BotKeysConfig.BOT_TOKEN;
    }


}
