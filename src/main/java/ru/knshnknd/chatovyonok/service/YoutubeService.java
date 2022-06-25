package ru.knshnknd.chatovyonok.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.knshnknd.chatovyonok.bot.BotKeysConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class YoutubeService {
    @Autowired
    private JSONParser jsonParser;

    private final String IO_EXCEPTION_MESSAGE = "Ой-ой! Ошибка подключения.";
    private final String PARSE_EXCEPTION_MESSAGE = "Ой-ой! Ошибка...";
    private final String HTTP_STATUS_EXCEPTION = "Увы! Сейчас запросы в YouTube недоступны.";

    private final int VIDEO_COUNT = 1;
    public String getYoutubeVideo(String keyword) {
        try {
            keyword = keyword.replace(" ", "+");

            String url =
                "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults="
                + VIDEO_COUNT
                + "&order=relevance&q="
                + keyword
                + "&regionCode=RU&key="
                + BotKeysConfig.YOUTUBE_API_KEY;

            Document doc;
            try {
                doc = Jsoup.connect(url).ignoreContentType(true).get();
            } catch (HttpStatusException e) {
                return HTTP_STATUS_EXCEPTION;
            }

            String getJson = doc.text();

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
            return IO_EXCEPTION_MESSAGE;
        } catch (ParseException e) {
            return PARSE_EXCEPTION_MESSAGE;
        }
    }
}
