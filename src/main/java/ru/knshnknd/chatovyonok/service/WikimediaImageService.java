package ru.knshnknd.chatovyonok.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.knshnknd.chatovyonok.bot.ChatovyonokBot;
import ru.knshnknd.chatovyonok.bot.WikimediaCategories;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;

@Service
public class WikimediaImageService {

    @Autowired
    private JSONParser jsonParser;

    private final String[] categories = WikimediaCategories.categories;

    private final String urlWikimediaSearchByCategory = "https://commons.wikimedia.org/w/api.php?action=query&list=categorymembers&cmtype=file&cmlimit=500&format=json&cmtitle=Category:";
    private final String urlWikimediaGetDescriptionOfImageByPageId = "https://commons.wikimedia.org/w/api.php?action=query&prop=imageinfo&iiprop=extmetadata&iiextmetadatafilter=ImageDescription&format=json&pageids=";
    private final String urlWikimediaGetSourceOfImageByPageId = "https://commons.wikimedia.org/w/api.php?action=query&prop=pageimages&piprop=original&format=json&pageids=";

    public void getRandomImageFromWikimedia(ChatovyonokBot bot, Update update) {
        String randomPage = null;
        try {
            randomPage = getRandomPageIdFromCategory();
            JSONObject jsonObject = getJSONObjectFromPageId(randomPage);

            bot.sendImageWithMessage(update.getMessage().getChatId().toString(), getImageFromPageAsFile(jsonObject),
                    getDescriptionOfImageFromPageId(randomPage) + "\n\n<a href=\"" + getURLOfImageByPageID(randomPage) + "\">Источник: Wikimedia Commons</a>");
        } catch (ParseException | IOException e) {
            bot.sendMessage(update.getMessage().getChatId().toString(), "Ой.. Ошибка!");
        }
    }

    private String getURLOfImageByPageID(String pageId) throws ParseException, IOException {
        String urlPage = urlWikimediaGetDescriptionOfImageByPageId + pageId;

        Document doc  = Jsoup.connect(urlPage).ignoreContentType(true).ignoreHttpErrors(true).get();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(doc.text());

        JSONObject jsonObject1 = (JSONObject) jsonObject.get("query");
        JSONObject jsonObject2 = (JSONObject) jsonObject1.get("pages");
        JSONObject jsonObject3 = (JSONObject) jsonObject2.get(pageId);
        return "https://commons.wikimedia.org/wiki/" + jsonObject3.get("title").toString().replace(" ", "_");
    }

    private String getDescriptionOfImageFromPageId(String pageId) throws ParseException, IOException {
        String urlPage = urlWikimediaGetDescriptionOfImageByPageId + pageId;

        Document doc  = Jsoup.connect(urlPage).ignoreContentType(true).ignoreHttpErrors(true).get();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(doc.text());

        JSONObject jsonObject1 = (JSONObject) jsonObject.get("query");
        JSONObject jsonObject2 = (JSONObject) jsonObject1.get("pages");
        JSONObject jsonObject3 = (JSONObject) jsonObject2.get(pageId);
        JSONArray jsonObject4 = (JSONArray) jsonObject3.get("imageinfo");

        String description = "";

        try {

            JSONObject jsonObject5 = (JSONObject) jsonObject4.get(0);
            JSONObject jsonObject6 = (JSONObject) jsonObject5.get("extmetadata");
            JSONObject jsonObject7 = (JSONObject) jsonObject6.get("ImageDescription");
            description = "\n" + jsonObject7.get("value").toString();

        } catch (Exception ignored) {}

        String title = jsonObject3.get("title").toString();

        description = description.equals(title) || (description + ".").equals(title) ? "" : description;

        title = title.replace("jpg", "");
        title = title.replace("JPG", "");
        title = title.replace("png", "");
        title = title.replace("PNG", "");
        title = title.replace("File:", "");

        return "<b>" + title + "</b>" + description;
    }

    private InputStream getImageFromPageAsFile(JSONObject jsonObject) throws IOException {
        String imageUrl = getURLOfImage(jsonObject);

        URL url = new URL(imageUrl);
        return url.openStream();
    }

    private JSONObject getJSONObjectFromPageId(String pageId) throws ParseException, IOException {
        String urlPage = urlWikimediaGetSourceOfImageByPageId + pageId;

        Document doc  = Jsoup.connect(urlPage).ignoreContentType(true).ignoreHttpErrors(true).get();

        JSONObject jsonObject = (JSONObject) jsonParser.parse(doc.text());
        JSONObject jsonObject1 = (JSONObject) jsonObject.get("query");
        JSONObject jsonObject2 = (JSONObject) jsonObject1.get("pages");
        JSONObject jsonObject3 = (JSONObject) jsonObject2.get(pageId);

        return (JSONObject) jsonObject3.get("original");
    }

    private String getURLOfImage(JSONObject jsonObject) {
        return jsonObject.get("source").toString();
    }

    private String getRandomPageIdFromCategory() throws ParseException, IOException {
        int randomCategory = new Random().nextInt(categories.length);
        String urlCategory = urlWikimediaSearchByCategory + categories[randomCategory];

        Document doc  = Jsoup.connect(urlCategory).ignoreContentType(true).get();
        String getJson = doc.text();

        JSONObject jsonObject = (JSONObject) jsonParser.parse(getJson);
        JSONObject jsonObject1 = (JSONObject) jsonObject.get("query");
        JSONArray jsonArray = (JSONArray) jsonObject1.get("categorymembers");

        int randomPage = new Random().nextInt(jsonArray.size());
        JSONObject jsonObject2 = (JSONObject) jsonArray.get(randomPage);

        return jsonObject2.get("pageid").toString();
    }

}
