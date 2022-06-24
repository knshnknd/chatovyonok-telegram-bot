package ru.knshnknd.chatovyonok.service;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.enums.Language;
import com.github.prominence.openweathermap.api.enums.UnitSystem;
import com.github.prominence.openweathermap.api.exception.NoDataFoundException;
import com.github.prominence.openweathermap.api.model.forecast.*;
import com.github.prominence.openweathermap.api.model.weather.*;
import com.github.prominence.openweathermap.api.model.weather.Location;
import org.springframework.stereotype.Service;
import ru.knshnknd.chatovyonok.bot.BotConfig;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class WeatherService {
    // Формат времени
    final static String TIME_PATTERN = "HH:mm";

    // Узнать погоду и прогноз
    public String getWeather(String cityName) {
        String result = "";
        OpenWeatherMapClient openWeatherClient = new OpenWeatherMapClient(BotConfig.WEATHER_API_KEY);

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

            // Локализация и форматирование времени
            Location location = weather.getLocation();
            int seconds = location.getZoneOffset().getTotalSeconds();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN);

            // Пишем погоду на данный момент
            result =
                    "Погода в городе "
                            + weather.getLocation().getName()
                            + " на "
                            + timeFormatter.format(weather.getCalculationTime().plusSeconds(seconds))
                            + ": температура "
                            + weather.getTemperature().getValue()
                            + "°C, влажность "
                            + weather.getHumidity().getValue()
                            + "%, скорость ветра "
                            + weather.getWind().getSpeed()
                            + " м/c, "
                            + weather.getWeatherState().getDescription() + ".";

            // Рассвет и закат (опционально)
            /*
            result += "\n\nРассвет в " + timeFormatter.format(location.getSunriseTime().plusSeconds(seconds))
                    + "\nЗакат в "  + timeFormatter.format(location.getSunsetTime().plusSeconds(seconds));
             */

            // Прогноз на 9 часов (по прогнозу на каждые 3 часа)
            StringBuilder stringBuilder = new StringBuilder("\n\nПрогноз на ближайшие 9 часов:\n");
            final Forecast forecast =
                    openWeatherClient
                    .forecast5Day3HourStep()
                    .byCityName(cityName)
                    .language(Language.RUSSIAN)
                    .unitSystem(UnitSystem.METRIC)
                    .count(3)
                    .retrieve()
                    .asJava();

            List<WeatherForecast> weathers = forecast.getWeatherForecasts();

            for (WeatherForecast weatherForecast : weathers) {
                stringBuilder.append("· В ")
                        .append(timeFormatter.format(weatherForecast.getForecastTime().plusSeconds(seconds)))
                        .append(": ")
                        .append(weatherForecast.getWeatherState().getDescription())
                        .append(", ")
                        .append(weatherForecast.getTemperature().getValue())
                        .append("°C.\n");
            }

            // Добавляем прогноз к основному прогнозу
            result += stringBuilder.toString();
        } catch (NoDataFoundException e) {
            return "Ох! Неправильно написано название города для прогноза погоды.";
        }

        return result;
    }
}
