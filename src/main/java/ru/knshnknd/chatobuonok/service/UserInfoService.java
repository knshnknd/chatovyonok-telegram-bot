package ru.knshnknd.chatobuonok.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.knshnknd.chatobuonok.dao.UserInfoEntity;
import ru.knshnknd.chatobuonok.dao.UserInfoRepository;

import java.util.List;
import java.util.Optional;

@Component
public class UserInfoService {
    @Autowired
    UserInfoRepository userInfoRepository;

    // Добавить нового пользователя в БД с настройками: счётчик мудрости 0, лимит мудрости 0
    public void addUserInfo(User user) {
        Optional<UserInfoEntity> userInfoEntity = userInfoRepository.findUserInfoEntityByUserId(user.getId());
        if (userInfoEntity.isEmpty()) {
            UserInfoEntity userInfo = new UserInfoEntity(user.getId(), user.getUserName(), 0L, 0L);
            userInfoRepository.save(userInfo);
        }
    }

    // Обнулить счётчик лимита мудрости на 0 для всех
    public void resetAllWiseLimitCount() {
        List<UserInfoEntity> userInfoEntityList = userInfoRepository.findAll();
        for (UserInfoEntity userInfoEntity : userInfoEntityList) {
            userInfoEntity.setWiseLimitCount(0L);
            userInfoRepository.save(userInfoEntity);
        }
    }
}
