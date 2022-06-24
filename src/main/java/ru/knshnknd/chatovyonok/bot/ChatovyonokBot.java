package ru.knshnknd.chatovyonok.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.knshnknd.chatovyonok.service.*;

import java.util.Random;

@Component
public class ChatovyonokBot extends TelegramLongPollingBot {

    @Autowired
    UpdateService updateService;
    @Autowired
    WiseService wiseService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    WeatherService weatherService;
    @Autowired
    YoutubeService youtubeService;
    @Autowired
    RecipeService recipeService;

    @Scheduled(cron = "0 0 2 * * *")
    public void timeForForecast() {
        updateService.sendForecastToAll(this);
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void timeForWise() {
        userInfoService.resetAllWiseLimitCount();
        updateService.sendForecastToAll(this);
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {
            // Получаем сообщение, пользователя и ID чата, из которого пришло сообщение
            Message message = update.getMessage();
            User user = message.getFrom();
            String currentChatId = message.getChatId().toString();

            // Записываем счётчик вызова бота для статистики в БД
            updateService.addUpdate(currentChatId);

            // Шанс 3%, что бот скажет коронную фразу в ответ на вызов через 2 минуты
            if (new Random().nextInt(100) < 3) {
                Thread randomPhraseThread = new Thread(new RandomPhraseThread(this, update));
                randomPhraseThread.start();
            }

            if (message.hasText()) {
                // Разбиваем полученное сообщение на две части: команду и текст после команды
                String[] textFromMessage = message.getText().split(" ", 2);

                switch (textFromMessage[0].toLowerCase()) {
                    // команда /start и добавление информации о новом пользователе
                    case "/start@chatovyonokbot", "/start" -> {
                        sendMessage(currentChatId, BotConfig.START_TEXT);
                        userInfoService.addUserInfo(user);
                    }

                    // команда /help
                    case "/help@chatovyonokbot", "/help" -> {
                        sendMessage(currentChatId, BotConfig.HELP_TEXT);
                    }

                    // Случайная мудрость
                    case "/wise@chatovyonokbot", "/wise", "/wi" -> {
                        sendMessage(currentChatId, wiseService.getRandomWiseForUser(user));
                    }

                    // Узнать счётчик мудростей у пользователя
                    case "/wise_count@chatovyonokbot", "/wise_count" -> {
                        sendMessage(currentChatId, wiseService.getWiseCountForUser(user));
                    }

                    // Согласиться получать в чат одну мудрость каждый день
                    case "/wise_subscribe@chatovyonokbot", "/wise_subscribe", "/wise_sub" -> {
                        updateService.agreeToGetWise(currentChatId);
                        sendMessage(currentChatId, "Лады! Отныне я буду присылать в сей чат одну случайную мудрость ежедённо!\n\n"
                            + "Чтобы отказать от этой затеи, наберите команду /wise_unsubscribe.");
                    }

                    // Согласиться получать в чат одну мудрость каждый день
                    case "/wise_unsubscribe@chatovyonokbot", "/wise_unsubscribe", "wise_unsub" -> {
                        updateService.refuseToGetWise(currentChatId);
                        sendMessage(currentChatId, "Ну, и ладно! Больше не буду присылать мудрости в этот чат.");
                    }

                    // Запрос прогноза погоды по городу
                    case "/weather@chatovyonokbot", "/weather", "/we" -> {
                        String response;
                        try {
                            response = weatherService.getWeather(textFromMessage[1].trim());
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            response = "Ой-ой! Пустой запрос! Напишите после команды название города. " +
                                    "\n\nНапример: /weather Иркутск";
                        }

                        sendMessage(currentChatId, response);
                    }

                    // Узнать общее количество мудростей
                    case "/wise_number@chatovyonokbot", "/wise_number" -> {
                        String messageToSend =
                                "Всего я знаю пословиц, прибауток, поговорок, речений, присловий, чистоговорок и поверий: "
                                        + wiseService.getProverbCount()
                                        + " шт.\n\n" +
                                        "Взял их из книги Владимира Ивановича Даля «Пословицы русского народа» 1862 года.";
                        sendMessage(currentChatId, messageToSend);
                    }

                    // Согласие на получения прогноза погоды определённого города каждый день в определённый чат
                    case "/weather_subscribe@chatovyonokbot", "/weather_subscribe", "/weather_sub" -> {
                        String response;
                        try {
                            updateService.addCityAndAgreeToGetForecast(currentChatId, textFromMessage[1].trim());
                            response = "Ура! Теперь рано-рано утром я буду присылать в сей чат прогноз погоды для города " +
                                    textFromMessage[1].trim() + ".\n\n" +
                                    "Чтобы отказать от этой затеи, наберите команду /weather_unsubscribe.";
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            response = "М-да... Пустой запрос! Напишите после команды название города, " +
                                    "чтобы я присылал прогноз для него каждое утро. \n\n" +
                                    "Например, /weather_subscribe Иркутск";
                        }

                        sendMessage(currentChatId, response);
                    }

                    // Отказ на прогноз погоды каждый день
                    case "/weather_unsubscribe@chatovyonokbot", "/weather_unsubscribe" -> {
                        updateService.refuseToGetForecast(currentChatId);
                        sendMessage(currentChatId, "Ладно! Поднесь не буду присылать сюда прогноз погоды ежедённо.");
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
                        String response;
                        try {
                            response = youtubeService.getYoutubeVideo(textFromMessage[1]);
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            response = "Ба! Пустой запрос! Напишите ключевые слова после команды для поиска видео!" +
                                    "\n\nНапример: /youtube как сделать белый квас";
                        }
                        sendMessage(currentChatId, response);
                    }

                    // Узнать от бота случаный ответ на вопрос типа да-нет
                    case "/answer@chatovyonokbot", "/answer" -> {
                        int randomAnswerNumber = new Random().nextInt(BotConfig.RANDOM_ANSWERS.length);
                        sendMessage(currentChatId, "Отвечаю: " + BotConfig.RANDOM_ANSWERS[randomAnswerNumber]);
                    }

                    // Кинуть кубик от 1 до 6 (или другое число)
                    case "/dice@chatovyonokbot", "/dice" -> {
                        if (textFromMessage.length >= 2) {
                            try {
                                int range = Integer.parseInt(textFromMessage[1].trim());
                                if (range > 0) {
                                    Thread diceThread = new Thread(new DiceThread(this, update, range));
                                    diceThread.start();
                                } else {
                                    sendMessage(currentChatId, "@" + user.getUserName() +
                                            ", вы балда? Число должно быть больше нуля!");
                                }
                            } catch (NumberFormatException e) {
                                sendMessage(currentChatId, "Батюшки! Написано не число! или слишком большое число! " +
                                        "Напишите после /dice целое число меньше двух миллиардов.");
                            }
                        } else {
                            Thread diceThread = new Thread(new DiceThread(this, update, 6));
                            diceThread.start();
                        }
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
        } catch (TelegramApiException e) {}
    }

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }
}
