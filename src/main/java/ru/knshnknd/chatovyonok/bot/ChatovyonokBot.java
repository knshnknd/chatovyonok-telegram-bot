package ru.knshnknd.chatovyonok.bot;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.io.InputStream;
import java.util.Random;

@Component
public class ChatovyonokBot extends TelegramLongPollingBot {

    private static final Logger log = Logger.getLogger(ChatovyonokBot.class);

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
    @Autowired
    private ChronicleService chronicleService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private WikiSourceService wikiSourceService;

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

    @Scheduled(cron = "0 0 9 * * *")
    public void timeForEverydayArt() {
        wikimediaImageService.sendArtToAllSubscribed(this);
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {
            Message message = update.getMessage();
            User user = message.getFrom();
            String currentChatId = message.getChatId().toString();

            // ???????????????????? ?????????????? ???????????? ???????? ?????? ???????????????????? ?? ????
            updateService.addUpdate(currentChatId);

            // ???????? 3%, ?????? ?????? ???????????? ???????????????? ?????????? ?? ?????????? ???? ?????????? ?????????? 2 ????????????
            randomPhraseService.sayRandomPhrase(this, update, 3);

            if (message.hasText()) {
                log.info(user.getUserName() + " ?? " + currentChatId + ": " + message.getText());

                // ?????????????????? ???????????????????? ?????????????????? ???? ?????? ??????????: ?????????????? ?? ?????????? ?????????? ??????????????
                String[] messageText = message.getText().split(" ", 2);

                switch (messageText[0].toLowerCase()) {
                    case "/start@chatovyonokbot", "/start" -> {
                        sendMessage(currentChatId, BotMessages.START_TEXT);
                        botClientService.addBotClientIfNotExist(user);
                    }

                    case "/help@chatovyonokbot", "/help" -> {
                        sendEditedHTMLMessage(currentChatId, BotMessages.HELP_TEXT);
                    }

                    // ?????????????????? ????????????????
                    case "/wise@chatovyonokbot", "/wise", "/wi" -> {
                        sendMessage(currentChatId, botClientService.getRandomWiseForUser(user));
                    }

                    // ???????????? ?????????????? ?????????????????? ?? ????????????????????????
                    case "/wise_count@chatovyonokbot", "/wise_count", "/wc" -> {
                        sendMessage(currentChatId, botClientService.getWiseCountForUser(user));
                    }

                    // ?????????????????????? ???????????????? ?? ?????? ???????? ???????????????? ???????????? ????????
                    case "/wise_subscribe@chatovyonokbot", "/wise_sub@chatovyonokbot", "/wise_subscribe", "/wise_sub", "/ws" -> {
                        botClientService.subscribeToWise(currentChatId);
                        sendMessage(currentChatId, BotMessages.WISE_SUBSCRIPTION_MESSAGE);
                    }

                    // ?????????????????????? ???????????????? ?? ?????? ???????? ???????????????? ???????????? ????????
                    case "/wise_unsubscribe@chatovyonokbot", "/wise_unsub@chatovyonokbot", "/wise_unsubscribe", "wise_unsub", "/wu" -> {
                        botClientService.unsubscribeFromWise(currentChatId);
                        sendMessage(currentChatId, BotMessages.WISE_UNSUBSCRIPTION_MESSAGE);
                    }

                    // ???????????? ???????????????? ???????????? ???? ????????????
                    case "/weather@chatovyonokbot", "/weather", "/we" -> {
                        if (BotUtils.isTextMessageHasAnyWordsMore(messageText)) {
                            sendMessage(currentChatId, weatherService.getFullWeatherForecast(messageText[1].trim()));
                        } else {
                            sendMessage(currentChatId, BotMessages.WEATHER_EMPTY_REQUEST_MESSAGE);
                        }
                    }

                    // ???????????? ?????????? ???????????????????? ??????????????????
                    case "/wise_number@chatovyonokbot", "/wise_number" -> {
                        sendMessage(currentChatId, botClientService.howManyWisesDoesBotKnow());
                    }

                    // ???????????? ?????????? ???????????????????? ??????????????????
                    case "/wise_attack@chatovyonokbot", "/wise_attack", "/wa" -> {
                        if (BotUtils.isTextMessageHasAnyWordsMore(messageText)) {
                            botClientService.wiseAttack(this, update, user, messageText[1]);
                        } else {
                            sendMessage(currentChatId, BotMessages.WISE_ATTACK_EMPTY_REQUEST);
                        }
                    }

                    // ???????????????? ???? ?????????????????? ???????????????? ???????????? ?????????????????????????? ???????????? ???????????? ???????? ?? ???????????????????????? ??????
                    case "/weather_subscribe@chatovyonokbot", "/weather_sub@chatovyonokbot", "/weather_subscribe", "/weather_sub", "/wes" -> {
                        if (BotUtils.isTextMessageHasAnyWordsMore(messageText)) {
                            weatherService.subscribeToWeather(this, currentChatId, messageText[1].trim());
                        } else {
                            sendMessage(currentChatId, BotMessages.WEATHER_SUBSCRIPTION_EMPTY_REQUEST_MESSAGE);
                        }
                    }

                    // ?????????? ???? ?????????????? ???????????? ???????????? ????????
                    case "/weather_unsubscribe@chatovyonokbot", "/weather_unsub@chatovyonokbot", "/weather_unsubscribe", "/weather_unsub", "/weu" -> {
                        weatherService.unsubscribeFromWeather(currentChatId);
                        sendMessage(currentChatId, BotMessages.WEATHER_UNSUBSCRIPTION_MESSAGE);
                    }

                    // ?????????????????? ????????????
                    case "/recipe@chatovyonokbot", "/recipe","/rec" -> {
                        sendMessage(currentChatId, recipeService.getRandomRecipe());
                    }

                    // ???????????? ???????????????????? ????????????????
                    case "/recipes_number@chatovyonokbot", "/recipes_number" -> {
                        sendMessage(currentChatId, recipeService.getRecipesNumberMessage());
                    }

                    // ?????????? ?????????? ???? Youtube ???? ???????????????? ????????????
                    case "/youtube@chatovyonokbot", "/youtube" -> {
                        if (BotUtils.isTextMessageHasAnyWordsMore(messageText)) {
                            sendMessage(currentChatId, youtubeService.getYoutubeVideo(messageText[1]));
                        } else {
                            sendMessage(currentChatId, BotMessages.YOUTUBE_EMPTY_REQUEST_MESSAGE);
                        }
                    }

                    // ???????????? ???? ???????? ?????????????????? ?????????? ???? ???????????? ???????? ????-??????
                    case "/answer@chatovyonokbot", "/answer" -> {
                        int randomAnswerNumber = new Random().nextInt(BotMessages.RANDOM_ANSWERS.length);
                        sendMessage(currentChatId, "??????????????: " + BotMessages.RANDOM_ANSWERS[randomAnswerNumber]);
                    }

                    // ???????????? ?????????? ???? 1 ???? 6 (?????? ???????????? ??????????)
                    case "/dice@chatovyonokbot", "/dice" -> {
                        if (BotUtils.isTextMessageHasAnyWordsMore(messageText)) {
                            if (BotUtils.isTextInteger(messageText[1])) {
                                diceService.dice(this, update, Integer.parseInt(messageText[1]));
                            } else {
                                sendMessage(currentChatId, BotMessages.DICE_ERROR_MESSAGE);
                            }
                        } else {
                            diceService.dice(this, update, 6);
                        }
                    }

                    // ?????????????????? ?????????????? ?????????????????? ???? Wikimedia Commons
                    case "/art@chatovyonokbot", "/art" -> {
                        wikimediaImageService.getRandomImageFromWikimedia(this, currentChatId);
                    }

                    // ???????????????? ???? ??????????????????
                    case "/art_subscribe@chatovyonokbot", "/art_sub@chatovyonokbot", "/art_subscribe", "/art_sub", "/arts" -> {
                        wikimediaImageService.subscribeToArt(this, currentChatId);
                    }

                    // ?????????????? ???? ??????????????????
                    case "/art_unsubscribe@chatovyonokbot", "/art_unsub@chatovyonokbot", "/art_unsubscribe", "/art_unsub", "/artu" -> {
                        wikimediaImageService.unsubscribeFromArt(currentChatId);
                        sendMessage(currentChatId, BotMessages.ART_UNSUBSCRIPTION_MESSAGE);
                    }

                    // ?????????????????? ???????????? ???? ????????????????
                    case "/chronicle@chatovyonokbot", "/chronicle" -> {
                        sendMessage(currentChatId, chronicleService.getRandomChronicleLine());
                    }

                    // ?????????????????? ???????? ??????????????????
                    case "/chat_id@chatovyonokbot", "/chat_id" -> {
                        sendMessage(currentChatId, currentChatId);
                    }

                    // ??????????????
                    case "/vocabulary", "/vocab", "/vocabulary@chatovyonokbot" -> {
                        sendEditedHTMLMessage(currentChatId, wikiSourceService.getRandomDictionaryArticle());
                    }

                    // ?????????????? ?????????????? ?????????????? ?????? ???????????????? ?????????????????? ?????????? ???????? + AdminService

                    case "/test" -> {

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
            sendErrorMessageAndLog(chatId, e);
        }
    }

    public void sendEditedHTMLMessage(String chatId, String message) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(message)
                    .parseMode("HTML")
                    .build());
        } catch (TelegramApiException e) {
            sendErrorMessageAndLog(chatId, e);
        }
    }

    public void sendEditedMarkdownV2Message(String chatId, String message) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(message)
                    .parseMode("MarkdownV2")
                    .build());
        } catch (TelegramApiException e) {
            sendErrorMessageAndLog(chatId, e);
        }
    }

    public void sendImageWithMessage(String chatId, InputStream file, String message) {
        try {
            execute(SendPhoto.builder()
                    .chatId(chatId)
                    .photo(new InputFile(file, file.toString()))
                    .caption(message)
                    .parseMode("HTML")
                    .build());
        } catch (TelegramApiException e) {
            sendErrorMessageAndLog(chatId, e);
        }
    }

    public void sendErrorMessageAndLog(String chatId, Exception e) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text("???????????? ?? ?????????????????????? ??????????????! ???????????????????? ???????????????????? ??????????????????????????." +
                            "\n\n???????????????????? ?????? ??????.")
                    .build());
            log.error("ID ????????:" + chatId + ". ???????????? ?? ????????: " + e);
        } catch (TelegramApiException ignored) {}
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
