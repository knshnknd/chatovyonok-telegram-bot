package ru.knshnknd.chatovyonok.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.knshnknd.chatovyonok.bot.BotMessages;
import ru.knshnknd.chatovyonok.bot.ChatovyonokBot;
import ru.knshnknd.chatovyonok.model.enitites.BotClient;
import ru.knshnknd.chatovyonok.model.enitites.WiseSubscription;
import ru.knshnknd.chatovyonok.model.repositories.BotClientRepository;
import ru.knshnknd.chatovyonok.model.repositories.WiseSubscriptionRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

// Пока что пользователи завязаны только на счётчике мудрости, поэтому один сервис использует две таблицы... В будущем нужно сделать декомпозицию

@Transactional
@Service
public class BotClientService {

    @Autowired
    private BotClientRepository botClientRepository;

    @Autowired
    private WiseSubscriptionRepository wiseSubscriptionRepository;

    private final int APPROXIMATE_PROVERB_COUNT = 30_000;

    // Коллекция поговорок и их количество
    private final List<String> allProverbs;
    private final int proverbCount;

    // Чтение файла и запись поговорок в коллекцию в конструкторе
    public BotClientService() {
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
    public String getRandomWiseForUser(User user) {
        Optional<BotClient> userOfBotOptional = botClientRepository.findBotClientByUserId(user.getId());

        // Если пользователя нет в БД, то сперва добавляем
        if (userOfBotOptional.isEmpty()) {
            addBotClientIfNotExist(user);
            userOfBotOptional = botClientRepository.findBotClientByUserId(user.getId());
        }

        updateUsernameOfBotClientIfNotTheSame(user);

        BotClient botClientWise = userOfBotOptional.get();

        // Проверяем счётчик лимита мудрости
        if (isWiseLimitFull(botClientWise)) {
            return BotMessages.WISE_LIMIT_MESSAGE;
        } else {

            // Обновляем счётчик мудрости и счётчик лимита мудрости на +1
            Long previousWiseCount = botClientWise.getWiseCount();
            botClientWise.setWiseCount(previousWiseCount + 1L);
            Long previousWiseLimitCount = botClientWise.getWiseLimitCount();
            botClientWise.setWiseLimitCount(previousWiseLimitCount + 1L);
            botClientRepository.save(botClientWise);

            return getRandomWise();
        }
    }

    // Проверить, достигнут ли лимит мудростей в день
    public boolean isWiseLimitFull(BotClient botClient) {
        return botClient.getWiseLimitCount() >= 3;
    }

    // Получить случайную поговорку из коллекции
    public String getRandomWise() {
        Random random = new Random();
        int randomId = random.nextInt(proverbCount);
        return allProverbs.get(randomId) +
                "\n\n" +
                "– мудрость №" +
                randomId + ".";
    }

    // Получить счётчик мудрости у пользователя
    public String getWiseCountForUser(User user) {
        Optional<BotClient> userOfBotOptional = botClientRepository.findBotClientByUserId(user.getId());

        // Если пользователя нет в БД, то сперва добавляем
        if (userOfBotOptional.isEmpty()) {
            addBotClientIfNotExist(user);
            userOfBotOptional = botClientRepository.findBotClientByUserId(user.getId());
        }

        updateUsernameOfBotClientIfNotTheSame(user);

        Long wiseCount = userOfBotOptional.get().getWiseCount();
        return "Счётчик мудрости у @" + user.getUserName() + " равен " + wiseCount + "." +
                "\n\nСтать мудрее можно, узнав русскую мудрость – /wise. " +
                "\nБольше мудрости – выше атака в бою.";
    }

    // Обнулить счётчик лимита мудрости на 0 для всех
    public void resetAllWiseLimitCount() {
        List<BotClient> botClientList = botClientRepository.findAll();
        for (BotClient botClient : botClientList) {
            botClient.setWiseLimitCount(0L);
            botClientRepository.save(botClient);
        }
    }

    // Обнулить всем флаг "может атаковать"
    public void resetAllAttacks() {
        List<BotClient> botClientList = botClientRepository.findAll();
        for (BotClient botClient : botClientList) {
            botClient.setCanAttack(true);
            botClientRepository.save(botClient);
        }
    }

    // Настройки мудростей
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

    public void addNewWiseSubscriptionIfNotExist(String chatId) {
        Optional<WiseSubscription> wiseSubscriptionOptional = wiseSubscriptionRepository.findWiseSubscriptionByChatId(chatId);
        if (wiseSubscriptionOptional.isEmpty()) {
            WiseSubscription wiseSubscription = new WiseSubscription(chatId, Boolean.FALSE);
            wiseSubscriptionRepository.save(wiseSubscription);
        }
    }

    public void addBotClientIfNotExist(User user) {
        Optional<BotClient> userOfBotOptional = botClientRepository.findBotClientByUserId(user.getId());
        if (userOfBotOptional.isEmpty()) {
            BotClient botClient = new BotClient(user.getId(), user.getUserName(), 0L, 0L);
            botClientRepository.save(botClient);
        }
    }

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

    private void updateUsernameOfBotClientIfNotTheSame(User user) {
        Optional<BotClient> userOfBotOptional = botClientRepository.findBotClientByUserId(user.getId());
        if (userOfBotOptional.isPresent()) {
            BotClient botClient = userOfBotOptional.get();
            if (!botClient.getUsername().equals(user.getUserName())) {
                botClient.setUsername(user.getUserName());
                botClientRepository.save(botClient);
            }
        }
    }

    // Борьба мудрости между персонажами
    public void wiseAttack(ChatovyonokBot bot, Update update, User attackingClient, String defendingClient) {
        Optional<BotClient> botClientOptionalAttacking = botClientRepository.findBotClientByUserId(attackingClient.getId());

        // Если пользователя нет в БД, то сперва добавляем
        if (botClientOptionalAttacking.isEmpty()) {
            addBotClientIfNotExist(attackingClient);
            botClientOptionalAttacking = botClientRepository.findBotClientByUserId(attackingClient.getId());
        }

        updateUsernameOfBotClientIfNotTheSame(attackingClient);

        BotClient botClientAttacking = botClientOptionalAttacking.get();

        if (!botClientAttacking.isCanAttack()) {
            bot.sendMessage(update.getMessage().getChatId().toString(),"@" + botClientAttacking.getUsername() +
                    ", вы уже атаковали сегодня! " +
                    "Для каждого мудреца доступна одна атака в стуки. " +
                    "Новая атака будет доступна завтра.");
            return;
        }

            Optional<BotClient> botClientOptionalDefending =
                botClientRepository.findBotClientByUsername(defendingClient.trim().substring(1));
        if (botClientOptionalDefending.isEmpty()) {
            bot.sendMessage(update.getMessage().getChatId().toString(),"Ой! Неправильно написано имя пользователя для атаки, " +
                    "или такого пользователя не существует в списках мудрецов. " +
                    "\n\nПример правильной команды для атаки мудрости: /attack @durov");
            return;
        }

        BotClient botClientDefending = botClientOptionalDefending.get();

        String attackingUsername = "@" +  botClientAttacking.getUsername();
        String defendingUsername = "@" +  botClientDefending.getUsername();

        int attackingBonus = (int) (botClientAttacking.getWiseCount() * 0.25);
        int defendingBonus = (int) (botClientDefending.getWiseCount() * 0.25);

        int maxAttacking = 100 + attackingBonus;
        int maxDefending = 100 + defendingBonus;

        int randomValueAttacking = new Random().nextInt(maxAttacking);
        int randomValueDefending = new Random().nextInt(maxDefending);

        String firstPartOfMessage = "Бой мудрости! " + attackingUsername + " против " +  defendingUsername + "!\n\n" +
                attackingUsername + ": урон: 1–" + maxAttacking + ". " + "Выпала атака на " + randomValueAttacking + " урона!\n" +
                defendingUsername + ": урон: 1–" + maxDefending + ". " + "Выпала атака на " + randomValueDefending + " урона!";

        String fightResultMessage = "";
        botClientAttacking.setCanAttack(false);

        if (randomValueAttacking == randomValueDefending) {
            fightResultMessage = firstPartOfMessage + "\n\nИтог: " + "ничья!";
        } else if (randomValueDefending > randomValueAttacking) {
            fightResultMessage = firstPartOfMessage + "\n\nИтог: " + defendingUsername + " побеждает и крадёт балл мудрости у " + attackingUsername + "!";
            botClientDefending.setWiseCount(botClientDefending.getWiseCount() + 1);
            botClientAttacking.setWiseCount(botClientAttacking.getWiseCount() - 1);

        } else {
            fightResultMessage = firstPartOfMessage + "\n\nИтог: " + attackingUsername + " побеждает и крадёт балл мудрости у " + defendingUsername + "!";
            botClientDefending.setWiseCount(botClientDefending.getWiseCount() - 1);
            botClientAttacking.setWiseCount(botClientAttacking.getWiseCount() + 1);
        }

        botClientRepository.save(botClientDefending);
        botClientRepository.save(botClientAttacking);
        bot.sendMessage(update.getMessage().getChatId().toString(), fightResultMessage);
    }

    private int getProverbCount() {
        return proverbCount;
    }
}

