package ru.knshnknd.chatovyonok.dao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.knshnknd.chatovyonok.dao.enitites.UserOfBot;

import java.util.Optional;

@Repository
public interface UserOfBotRepository extends JpaRepository<UserOfBot, Long> {
    Optional<UserOfBot> findUserInfoByUserId(Long userId);
}
