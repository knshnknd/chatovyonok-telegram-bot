package ru.knshnknd.chatovyonok.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UpdateRepository extends JpaRepository<UpdateEntity, String> {
    Optional<UpdateEntity> findUpdateEntityByChatId(String chatId);
    List<UpdateEntity> findUpdateEntitiesByWantsWeather(Integer wantsWeather);
    List<UpdateEntity> findUpdateEntitiesByWantsWise(Integer wantsWise);
}
