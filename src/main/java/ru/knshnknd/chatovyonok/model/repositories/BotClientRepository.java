package ru.knshnknd.chatovyonok.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.knshnknd.chatovyonok.model.enitites.BotClient;

import java.util.Optional;

@Repository
public interface BotClientRepository extends JpaRepository<BotClient, Long> {
    Optional<BotClient> findBotClientByUserId(Long userId);
    Optional<BotClient> findBotClientByUsername(String username);
}
