package ru.knshnknd.chatovyonok.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.knshnknd.chatovyonok.dao.UserInfoEntity;
import ru.knshnknd.chatovyonok.dao.UserInfoRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class WiseService {

    @Autowired
    UserInfoService userInfoService;
    @Autowired
    UserInfoRepository userInfoRepository;

    private final int APPROXIMATE_PROVERB_COUNT = 29_500;

    // Коллекция поговорок и их количество
    private List<String> allProverbs;
    private int proverbCount;

    // Чтение файла и запись поговорок в коллекцию в конструкторе
    WiseService() {
        // Сразу создаём коллекцию с примерной известной ёмкостью, чтобы не нагружать оперативную память
        List<String> allProverbs = new ArrayList<>(APPROXIMATE_PROVERB_COUNT);
        Resource resource = new ClassPathResource("proverbs.txt");

        // Читаем файл с поговорками
        try (BufferedReader bufferedReader =
                     new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String readLine;

            while ((readLine = bufferedReader.readLine()) != null) {
                allProverbs.add(readLine);
            }

        } catch (IOException e) {}

        // Инициализируем переменные: список поговорок и их число
        this.allProverbs = allProverbs;
        proverbCount = allProverbs.size();
    }

    // Случайная мудрость для пользователя
    public String getRandomWiseForUser(User user) {
        Optional<UserInfoEntity> userInfoEntityOptional = userInfoRepository.findUserInfoEntityByUserId(user.getId());

        // Если пользователя нет в БД, то сперва добавляем
        if (userInfoEntityOptional.isEmpty()) {
            userInfoService.addUserInfo(user);
            userInfoEntityOptional = userInfoRepository.findUserInfoEntityByUserId(user.getId());
        }

        UserInfoEntity userInfoEntity = userInfoEntityOptional.get();

        // Проверяем счётчик лимита мудрости
        if (isWiseLimitFull(userInfoEntity)) {
            return "Ваш лимит на 3 мудрости в день исчерпан! Приходите за новой мудростью завтра.";
        } else {

            // Обновляем счётчик мудрости и счётчик лимита мудрости на +1
            Long previousWiseCount = userInfoEntity.getWiseCount();
            userInfoEntity.setWiseCount(previousWiseCount + 1L);
            Long previousWiseLimitCount = userInfoEntity.getWiseLimitCount();
            userInfoEntity.setWiseLimitCount(previousWiseLimitCount + 1L);
            userInfoRepository.save(userInfoEntity);

            return getRandomWise();
        }
    }

    // Проверить, достигнут ли лимит мудростей в день
    public boolean isWiseLimitFull(UserInfoEntity userInfoEntity) {
        if (userInfoEntity.getWiseLimitCount() >= 3) {
            return true;
        } else {
            return false;
        }
    }

    // Получить случайную поговорку из коллекции
    public String getRandomWise() {
        Random random = new Random();
        int randomId = random.nextInt(proverbCount);
        String messageToSend = allProverbs.get(randomId) +
                "\n\n" +
                "– Мудрость №" +
                randomId + ".";
        return messageToSend;
    }

    // Получить счётчик мудрости у пользователя
    public String getWiseCountForUser(User user) {
        Optional<UserInfoEntity> userInfoEntityOptional = userInfoRepository.findUserInfoEntityByUserId(user.getId());

        if (userInfoEntityOptional.isEmpty()) {
            userInfoService.addUserInfo(user);
            userInfoEntityOptional = userInfoRepository.findUserInfoEntityByUserId(user.getId());
        }

        Long wiseCount = userInfoEntityOptional.get().getWiseCount();
        return "Счётчик мудрости у @" + user.getUserName() + " равен " + wiseCount + ".";
    }

    // Получить количество поговорок
    public int getProverbCount() {
        return proverbCount;
    }
}

