package ru.knshnknd.chatovyonok.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.knshnknd.chatovyonok.jpa.enitites.ArtSubscription;

import java.util.List;
import java.util.Optional;

public interface ArtSubscriptionRepository extends JpaRepository<ArtSubscription, Long> {
    Optional<ArtSubscription> findArtSubscriptionByChatId(String chatId);
    List<ArtSubscription> findArtSubscriptionByIsActive(Boolean isActive);
}
