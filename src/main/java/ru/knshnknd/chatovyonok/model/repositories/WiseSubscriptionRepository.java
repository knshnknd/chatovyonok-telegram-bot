package ru.knshnknd.chatovyonok.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.knshnknd.chatovyonok.model.enitites.WiseSubscription;

import java.util.List;
import java.util.Optional;

public interface WiseSubscriptionRepository extends JpaRepository<WiseSubscription, Long> {
    Optional<WiseSubscription> findWiseSubscriptionByChatId(String chatId);
    List<WiseSubscription> findWiseSubscriptionByIsActive(Boolean isActive);
}
