package ru.knshnknd.chatovyonok.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.knshnknd.chatovyonok.bot.ChatovyonokBot;
import ru.knshnknd.chatovyonok.bot.WikimediaCategories;
import ru.knshnknd.chatovyonok.jpa.enitites.ArtSubscription;
import ru.knshnknd.chatovyonok.jpa.repositories.ArtSubscriptionRepository;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Transactional
@Service
public class WikimediaImageService {

    private static final Logger log = Logger.getLogger(WikimediaImageService.class);

    @Autowired
    private ArtSubscriptionRepository artSubscriptionRepository;

    @Autowired
    private JSONParser jsonParser;

    private final String[] categories = WikimediaCategories.categories;

    private final String urlWikimediaSearchByCategory = "https://commons.wikimedia.org/w/api.php?action=query&list=categorymembers&cmtype=file&cmlimit=500&format=json&cmtitle=Category:";
    private final String urlWikimediaGetDescriptionOfImageByPageId = "https://commons.wikimedia.org/w/api.php?action=query&prop=imageinfo&iiprop=extmetadata&iiextmetadatafilter=ImageDescription&format=json&pageids=";
    private final String urlWikimediaGetSourceOfImageByPageId = "https://commons.wikimedia.org/w/api.php?action=query&prop=pageimages&piprop=original&format=json&pageids=";

    public void getRandomImageFromWikimedia(ChatovyonokBot bot, String chatId) {
        String randomPage = null;
        try {
            randomPage = getRandomPageIdFromCategory();
            JSONObject jsonObject = getJSONObjectFromPageId(randomPage);

            bot.sendImageWithMessage(chatId, getImageFromPageAsFile(jsonObject),
                    getDescriptionOfImageFromPageId(randomPage) + "\n\n<a href=\"" + getURLOfImageByPageID(randomPage) + "\">Источник: Wikimedia Commons</a>");
        } catch (ParseException | IOException e) {
            bot.sendMessage(chatId, "Ой.. Ошибка!");
        }
    }

    public void addNewArtSubscriptionIfNotExist(String chatId) {
        Optional<ArtSubscription> artSubscriptionOptional = artSubscriptionRepository.findArtSubscriptionByChatId(chatId);
        if (artSubscriptionOptional.isEmpty()) {
            ArtSubscription artSubscription = new ArtSubscription(chatId, Boolean.FALSE);
            artSubscriptionRepository.save(artSubscription);
        }
    }

    public void subscribeToArt(ChatovyonokBot bot, String chatId) {
        Optional<ArtSubscription> artSubscriptionOptional = artSubscriptionRepository.findArtSubscriptionByChatId(chatId);
        if (artSubscriptionOptional.isPresent()) {
            ArtSubscription artSubscription = artSubscriptionOptional.get();
            artSubscription.setActive(Boolean.TRUE);
            artSubscriptionRepository.save(artSubscription);
            bot.sendMessage(chatId, "Теперь я буду каждый день присылать в этот чат одно случайное произведение искусства, связанное с Россией. Отменить: /art_unsub");
        } else {
            addNewArtSubscriptionIfNotExist(chatId);
            subscribeToArt(bot, chatId);
        }
    }

    public void unsubscribeFromArt(String chatId) {
        Optional<ArtSubscription> artSubscriptionOptional = artSubscriptionRepository.findArtSubscriptionByChatId(chatId);
        if (artSubscriptionOptional.isPresent()) {
            ArtSubscription artSubscription = artSubscriptionOptional.get();
            artSubscription.setActive(Boolean.FALSE);
            artSubscriptionRepository.save(artSubscription);
        } else {
            addNewArtSubscriptionIfNotExist(chatId);
            unsubscribeFromArt(chatId);
        }
    }

    public void sendArtToAllSubscribed(ChatovyonokBot bot) {
        List<ArtSubscription> artSubscriptionsList = artSubscriptionRepository.findArtSubscriptionByIsActive(Boolean.TRUE);
        for (ArtSubscription artSubscription : artSubscriptionsList) {
            log.info("Отправка икусства для " + artSubscription.getChatId() + " начинается");
            getRandomImageFromWikimedia(bot, artSubscription.getChatId());
            log.info("Отправка икусства для " + artSubscription.getChatId() + " прошла успешно");

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

        title = title.replace(".jpg", "");
        title = title.replace(".jpeg", "");
        title = title.replace(".JPEG", "");
        title = title.replace(".JPG", "");
        title = title.replace(".png", "");
        title = title.replace(".PNG", "");
        title = title.replace("File:", "");

        if ((title.length() + description.length()) >= 200) {
            description = "";
            if (title.length() >= 200) {
                title = title.substring(0, 199);
            }
        }

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
        String category = categories[randomCategory];
        String urlCategory = urlWikimediaSearchByCategory + category;

        Document doc  = Jsoup.connect(urlCategory).ignoreContentType(true).get();
        String getJson = doc.text();

        JSONObject jsonObject = (JSONObject) jsonParser.parse(getJson);
        JSONObject jsonObject1 = (JSONObject) jsonObject.get("query");
        JSONArray jsonArray = (JSONArray) jsonObject1.get("categorymembers");

        log.info("JSON массив, размер: " + jsonArray.size() + ". Категория: " + category);

        int randomPage = 0;
        if (jsonArray.size() > 0) {
            randomPage = new Random().nextInt(jsonArray.size());
        }

        JSONObject jsonObject2 = (JSONObject) jsonArray.get(randomPage);

        return jsonObject2.get("pageid").toString();
    }

}
