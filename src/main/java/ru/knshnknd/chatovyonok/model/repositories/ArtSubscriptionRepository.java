package ru.knshnknd.chatovyonok.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.knshnknd.chatovyonok.model.enitites.ArtSubscription;
import ru.knshnknd.chatovyonok.model.enitites.WiseSubscription;

import java.util.List;
import java.util.Optional;

public interface ArtSubscriptionRepository extends JpaRepository<ArtSubscription, Long> {
    Optional<ArtSubscription> findArtSubscriptionByChatId(String chatId);
    List<ArtSubscription> findArtSubscriptionByIsActive(Boolean isActive);
}
