package ru.knshnknd.chatovyonok.dao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.knshnknd.chatovyonok.dao.enitites.WeatherSubscription;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherSubscriptionRepository extends JpaRepository<WeatherSubscription, Long> {
    Optional<WeatherSubscription> findUserWeatherByChatId(String chatId);
    List<WeatherSubscription> findUserWeatherByIsActive(Boolean isActive);
}
