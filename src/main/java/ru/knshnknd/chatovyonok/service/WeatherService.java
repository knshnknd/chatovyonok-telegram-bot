package ru.knshnknd.chatovyonok.service;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.enums.Language;
import com.github.prominence.openweathermap.api.enums.UnitSystem;
import com.github.prominence.openweathermap.api.exception.NoDataFoundException;
import com.github.prominence.openweathermap.api.model.forecast.*;
import com.github.prominence.openweathermap.api.model.forecast.Location;
import com.github.prominence.openweathermap.api.model.weather.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.knshnknd.chatovyonok.bot.BotMessages;
import ru.knshnknd.chatovyonok.bot.ChatovyonokBot;
import ru.knshnknd.chatovyonok.model.enitites.WeatherSubscription;
import ru.knshnknd.chatovyonok.model.repositories.WeatherSubscriptionRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
public class WeatherService {

    @Autowired
    private OpenWeatherMapClient openWeatherClient;
    @Autowired
    private WeatherSubscriptionRepository weatherSubscriptionRepository;

    private final int HOURS_FOR_FORECAST_X_3 = 3;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public String getFullWeatherForecast(String cityName) {
        return getWeatherForecastForNow(cityName) + "\n\n" + getWeatherForecastFor9Hours(cityName);
    }

    public void addNewUserWeatherIfNotExist(String chatId) {
        Optional<WeatherSubscription> userWeatherOptional = weatherSubscriptionRepository.findUserWeatherByChatId(chatId);
        if (userWeatherOptional.isEmpty()) {
            WeatherSubscription weatherSubscription = new WeatherSubscription(chatId, Boolean.FALSE, "");
            weatherSubscriptionRepository.save(weatherSubscription);
        }
    }

    public void subscribeToWeather(ChatovyonokBot bot, String chatId, String cityName) {
        Optional<WeatherSubscription> userWeatherOptional = weatherSubscriptionRepository.findUserWeatherByChatId(chatId);
        if (userWeatherOptional.isPresent()) {
            WeatherSubscription weatherSubscription = userWeatherOptional.get();
            weatherSubscription.setActive(Boolean.TRUE);
            weatherSubscription.setWeatherCity(cityName.trim());
            weatherSubscriptionRepository.save(weatherSubscription);
            bot.sendMessage(chatId, "Ура! Теперь рано-рано утром я буду присылать в сей чат прогноз погоды для города " +
                    cityName + ". К сожалению, пока что один прогноз на один чат. Новая подписка перезапишет старую, но я скоро это исправлю.\n\n" +
                    "Чтобы отказать от этой затеи, наберите команду /weather_unsubscribe.");
        } else {
            addNewUserWeatherIfNotExist(chatId);
            subscribeToWeather(bot, chatId, cityName);
        }
    }

    public void unsubscribeFromWeather(String chatId) {
        Optional<WeatherSubscription> userWeatherOptional = weatherSubscriptionRepository.findUserWeatherByChatId(chatId);
        if (userWeatherOptional.isPresent()) {
            WeatherSubscription weatherSubscription = userWeatherOptional.get();
            weatherSubscription.setActive(Boolean.FALSE);
            weatherSubscriptionRepository.save(weatherSubscription);
        } else {
            addNewUserWeatherIfNotExist(chatId);
            unsubscribeFromWeather(chatId);
        }
    }

    public void sendForecastToAllSubscribed(ChatovyonokBot bot) {
        List<WeatherSubscription> weatherSubscriptionList = weatherSubscriptionRepository.findUserWeatherByIsActive(Boolean.TRUE);
        for (WeatherSubscription weatherSubscription : weatherSubscriptionList) {
            bot.sendMessage(weatherSubscription.getChatId(), getFullWeatherForecast(weatherSubscription.getWeatherCity()));
        }
    }

    private String getWeatherForecastForNow(String cityName) {
        try {
            final Weather weather =
                    openWeatherClient
                            .currentWeather()
                            .single()
                            .byCityName(cityName)
                            .language(Language.RUSSIAN)
                            .unitSystem(UnitSystem.METRIC)
                            .retrieve()
                            .asJava();

            // Пишем погоду на данный момент
            return "Погода в городе "
                            + weather.getLocation().getName()
                            + " на "
                            + timeFormatter.format(getCorrectTemporalAccessorForWeather(weather))
                            + ": температура "
                            + weather.getTemperature().getValue()
                            + "°C, влажность "
                            + weather.getHumidity().getValue()
                            + "%, скорость ветра "
                            + weather.getWind().getSpeed()
                            + " м/c, "
                            + weather.getWeatherState().getDescription() + ".";

        } catch (NoDataFoundException e) {
            return BotMessages.WEATHER_CITY_ERROR;
        }
    }

    private String getWeatherForecastFor9Hours(String cityName) {
        StringBuilder stringBuilder = new StringBuilder("Прогноз на ближайшие " + HOURS_FOR_FORECAST_X_3 * 3 + " часов:\n");

        final Forecast forecast =
                openWeatherClient
                        .forecast5Day3HourStep()
                        .byCityName(cityName)
                        .language(Language.RUSSIAN)
                        .unitSystem(UnitSystem.METRIC)
                        .count(HOURS_FOR_FORECAST_X_3)
                        .retrieve()
                        .asJava();

        List<WeatherForecast> weathers = forecast.getWeatherForecasts();

        for (WeatherForecast weatherForecast : weathers) {
            stringBuilder.append("· В ")
                    .append(timeFormatter.format(getCorrectTemporalAccessorForForecast(weatherForecast.getForecastTime(), forecast.getLocation())))
                    .append(": ")
                    .append(weatherForecast.getWeatherState().getDescription())
                    .append(", ")
                    .append(weatherForecast.getTemperature().getValue())
                    .append("°C.\n");
        }
        return stringBuilder.toString();
    }

    private TemporalAccessor getCorrectTemporalAccessorForWeather(Weather weather) {
        return weather.getCalculationTime().plusSeconds(weather.getLocation().getZoneOffset().getTotalSeconds());
    }

    private TemporalAccessor getCorrectTemporalAccessorForForecast(LocalDateTime localDateTime, Location location) {
        return localDateTime.plusSeconds(location.getZoneOffset().getTotalSeconds());
    }
}
