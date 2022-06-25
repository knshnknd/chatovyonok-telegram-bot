package ru.knshnknd.chatovyonok.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.knshnknd.chatovyonok.bot.BotMessages;
import ru.knshnknd.chatovyonok.bot.ChatovyonokBot;
import ru.knshnknd.chatovyonok.dao.enitites.UserOfBot;
import ru.knshnknd.chatovyonok.dao.enitites.WiseSubscription;
import ru.knshnknd.chatovyonok.dao.repositories.UserOfBotRepository;
import ru.knshnknd.chatovyonok.dao.repositories.WiseSubscriptionRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

// Пока что пользователи завязаны только на счётчике мудрости, поэтому один сервис использует две таблицы... В будущем нужно сделать декомпозицию

@Service
public class UserService {

    @Autowired
    private UserOfBotRepository userOfBotRepository;

    @Autowired
    private WiseSubscriptionRepository wiseSubscriptionRepository;

    private final int APPROXIMATE_PROVERB_COUNT = 29_500;

    // Коллекция поговорок и их количество
    private final List<String> allProverbs;
    private final int proverbCount;

    // Чтение файла и запись поговорок в коллекцию в конструкторе
    public UserService() {
        List<String> allProverbs = new ArrayList<>(APPROXIMATE_PROVERB_COUNT);
        Resource resource = new ClassPathResource("proverbs.txt");

        try (BufferedReader bufferedReader =
                     new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String readLine;

            while ((readLine = bufferedReader.readLine()) != null) {
                allProverbs.add(readLine);
            }

        } catch (IOException e) {
            System.out.println(e);
        }

        // Инициализируем переменные: список поговорок и их число
        this.allProverbs = allProverbs;
        proverbCount = allProverbs.size();
    }

    // Случайная мудрость для пользователя
    @Transactional
    public String getRandomWiseForUser(User user) {
        Optional<UserOfBot> userOfBotOptional = userOfBotRepository.findUserInfoByUserId(user.getId());

        // Если пользователя нет в БД, то сперва добавляем
        if (userOfBotOptional.isEmpty()) {
            addNewUserIfNotExist(user);
            userOfBotOptional = userOfBotRepository.findUserInfoByUserId(user.getId());
        }

        UserOfBot userOfBotWise = userOfBotOptional.get();

        // Проверяем счётчик лимита мудрости
        if (isWiseLimitFull(userOfBotWise)) {
            return BotMessages.WISE_LIMIT_MESSAGE;
        } else {

            // Обновляем счётчик мудрости и счётчик лимита мудрости на +1
            Long previousWiseCount = userOfBotWise.getWiseCount();
            userOfBotWise.setWiseCount(previousWiseCount + 1L);
            Long previousWiseLimitCount = userOfBotWise.getWiseLimitCount();
            userOfBotWise.setWiseLimitCount(previousWiseLimitCount + 1L);
            userOfBotRepository.save(userOfBotWise);

            return getRandomWise();
        }
    }

    // Проверить, достигнут ли лимит мудростей в день
    public boolean isWiseLimitFull(UserOfBot userOfBot) {
        return userOfBot.getWiseLimitCount() >= 3;
    }

    // Получить случайную поговорку из коллекции
    public String getRandomWise() {
        Random random = new Random();
        int randomId = random.nextInt(proverbCount);
        return allProverbs.get(randomId) +
                "\n\n" +
                "– Мудрость №" +
                randomId + ".";
    }

    // Получить счётчик мудрости у пользователя
    @Transactional
    public String getWiseCountForUser(org.telegram.telegrambots.meta.api.objects.User user) {
        Optional<UserOfBot> userOfBotOptional = userOfBotRepository.findUserInfoByUserId(user.getId());

        // Если пользователя нет в БД, то сперва добавляем
        if (userOfBotOptional.isEmpty()) {
            addNewUserIfNotExist(user);
            userOfBotOptional = userOfBotRepository.findUserInfoByUserId(user.getId());
        }

        Long wiseCount = userOfBotOptional.get().getWiseCount();
        return "Счётчик мудрости у @" + user.getUserName() + " равен " + wiseCount + ".";
    }

    // Обнулить счётчик лимита мудрости на 0 для всех
    @Transactional
    public void resetAllWiseLimitCount() {
        List<UserOfBot> userOfBotList = userOfBotRepository.findAll();
        for (UserOfBot userOfBot : userOfBotList) {
            userOfBot.setWiseLimitCount(0L);
            userOfBotRepository.save(userOfBot);
        }
    }

    // Настройки мудростей
    @Transactional
    public void subscribeToWise(String chatId) {
        Optional<WiseSubscription> wiseSubscriptionOptional = wiseSubscriptionRepository.findWiseSubscriptionByChatId(chatId);
        if (wiseSubscriptionOptional.isPresent()) {
            WiseSubscription wiseSubscription = wiseSubscriptionOptional.get();
            wiseSubscription.setIsActive(Boolean.TRUE);
            wiseSubscriptionRepository.save(wiseSubscription);
        } else {
            addNewWiseSubscriptionIfNotExist(chatId);
            subscribeToWise(chatId);
        }
    }

    @Transactional
    public void unsubscribeFromWise(String chatId) {
        Optional<WiseSubscription> wiseSubscriptionOptional = wiseSubscriptionRepository.findWiseSubscriptionByChatId(chatId);
        if (wiseSubscriptionOptional.isPresent()) {
            WiseSubscription wiseSubscription = wiseSubscriptionOptional.get();
            wiseSubscription.setIsActive(Boolean.FALSE);
            wiseSubscriptionRepository.save(wiseSubscription);
        } else {
            addNewWiseSubscriptionIfNotExist(chatId);
            unsubscribeFromWise(chatId);
        }
    }

    @Transactional
    public void addNewWiseSubscriptionIfNotExist(String chatId) {
        Optional<WiseSubscription> wiseSubscriptionOptional = wiseSubscriptionRepository.findWiseSubscriptionByChatId(chatId);
        if (wiseSubscriptionOptional.isEmpty()) {
            WiseSubscription wiseSubscription = new WiseSubscription(chatId, Boolean.FALSE);
            wiseSubscriptionRepository.save(wiseSubscription);
        }
    }

    @Transactional
    public void addNewUserIfNotExist(User user) {
        Optional<UserOfBot> userOfBotOptional = userOfBotRepository.findUserInfoByUserId(user.getId());
        if (userOfBotOptional.isEmpty()) {
            UserOfBot userOfBot = new UserOfBot(user.getId(), user.getUserName(), 0L, 0L);
            userOfBotRepository.save(userOfBot);
        }
    }

    @Transactional
    public void sendWiseToAllSubscribed(ChatovyonokBot bot) {
        List<WiseSubscription> wiseSubscriptionList = wiseSubscriptionRepository.findWiseSubscriptionByIsActive(Boolean.TRUE);
        for (WiseSubscription wiseSubscription : wiseSubscriptionList) {
            bot.sendMessage(wiseSubscription.getChatId(), getRandomWise());
        }
    }

    public String howManyWisesDoesBotKnow() {
        return "Всего я знаю пословиц, прибауток, поговорок, речений, присловий, чистоговорок и поверий: "
                + getProverbCount()
                + " шт.\n\n" +
                "Взял их из книги Владимира Ивановича Даля «Пословицы русского народа» 1862 года.";
    }

    private int getProverbCount() {
        return proverbCount;
    }
}

