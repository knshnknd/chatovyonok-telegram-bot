package ru.knshnknd.chatobuonok.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import ru.knshnknd.chatobuonok.bot.BotConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class YoutubeService {
    private final int VIDEO_COUNT = 1;

    // Получить видео из Youtube по ключевым словам
    public String getYoutubeVideo(String keyword) {
        try {
            // Формируем url-запрос для получения json с первым видео по поиску
            keyword = keyword.replace(" ", "+");
            String url =
                "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults="
                + VIDEO_COUNT
                + "&order=relevance&q="
                + keyword
                + "&regionCode=RU&key="
                + BotConfig.YOUTUBE_API_KEY;

            Document doc;
            try {
                doc = Jsoup.connect(url).ignoreContentType(true).get();
            } catch (HttpStatusException e) {
                return "Увы! Закончилась квота на запрос 100 видео в день. " +
                        "К сожалению, автор бота пока что использует бесплатные сервисы Google.";
            }

            String getJson = doc.text();
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(getJson);
            JSONArray videoItems = (JSONArray) jsonObject.get("items");

            List<JSONObject> items = new ArrayList<>();

            for (Object o : videoItems) {
                JSONObject jsonObject1 = (JSONObject) o;
                items.add((JSONObject) jsonObject1.get("id"));
            }

            JSONObject firstVideo = items.get(0);
            String videoID = (String) firstVideo.get("videoId");

            return "https://www.youtube.com/watch?v=" + videoID;

        } catch (IOException e) {
            return "Ой-ой! Ошибка подключения.";
        } catch (ParseException e) {
            return "Ой-ой! Ошибка.";
        }
    }
}
