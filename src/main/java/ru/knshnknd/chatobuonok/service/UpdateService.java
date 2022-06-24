package ru.knshnknd.chatobuonok.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.knshnknd.chatobuonok.bot.ChatovyonokBot;
import ru.knshnknd.chatobuonok.dao.UpdateEntity;
import ru.knshnknd.chatobuonok.dao.UpdateRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UpdateService {
    
    @Autowired
    UpdateRepository updateRepository;
    @Autowired
    WeatherService weatherService;
    @Autowired
    WiseService wiseService;

    // Добавляем +1 к update_count у чата по chat_id в БД при каждом вызове бота,
    // если такого кортежа нет, то добавялем новый
    public void addUpdate(String chatId) {
            Optional<UpdateEntity> updateEntityOptional = updateRepository.findUpdateEntityByChatId(chatId);

            if (!updateEntityOptional.isEmpty()) {
                UpdateEntity updateEntity = updateEntityOptional.get();
                Long previousUpdateCount = updateEntity.getUpdateCount();
                updateEntity.setUpdateCount(previousUpdateCount + 1L);
                updateRepository.save(updateEntity);
            } else {
                UpdateEntity newUpdate = new UpdateEntity(chatId, 1L, 0, 0, "");
                updateRepository.save(newUpdate);
            }
    }

    // Настройки мудростей
    // Согласиться получать мудрости в чат раз в день
    public void agreeToGetWise(String chatId) {
        UpdateEntity updateEntity = updateRepository.findUpdateEntityByChatId(chatId).get();
        updateEntity.setWantsWise(1);
        updateRepository.save(updateEntity);
    }

    // Отказаться получать мудрости в чат раз в день
    public void refuseToGetWise(String chatId) {
        UpdateEntity updateEntity = updateRepository.findUpdateEntityByChatId(chatId).get();
        updateEntity.setWantsWise(0);
        updateRepository.save(updateEntity);
    }

    // Отправить мудрость всем, кто согласен
    public void sendWiseToAll(ChatovyonokBot bot) {
        List<UpdateEntity> updateEntityList = updateRepository.findUpdateEntitiesByWantsWise(1);
        for (UpdateEntity updateEntity : updateEntityList) {
            bot.sendMessage(updateEntity.getChatId(), wiseService.getRandomWise());
        }
    }

    // Настройки погоды
    // Добавить город для получения прогноза погоды раз в день и согласиться на его получение
    public void addCityAndAgreeToGetForecast(String chatId, String cityName) {
        UpdateEntity updateEntity = updateRepository.findUpdateEntityByChatId(chatId).get();
        updateEntity.setWeatherCity(cityName);
        updateEntity.setWantsWeather(1);
        updateRepository.save(updateEntity);
    }

    // Отказаться получать прогноз погоды раз в день
    public void refuseToGetForecast(String chatId) {
        UpdateEntity updateEntity = updateRepository.findUpdateEntityByChatId(chatId).get();
        updateEntity.setWantsWeather(0);
        updateRepository.save(updateEntity);
    }

    // Отправить прогноз погоды всем, кто согласен
    public void sendForecastToAll(ChatovyonokBot bot) {
        List<UpdateEntity> updateEntityList = updateRepository.findUpdateEntitiesByWantsWeather(1);
        for (UpdateEntity updateEntity : updateEntityList) {
            bot.sendMessage(updateEntity.getChatId(), weatherService.getWeather(updateEntity.getWeatherCity()));
        }
    }
}
