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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;

@Service
public class WikimediaImageService {

    @Autowired
    private JSONParser jsonParser;

    private final String[] categories = {
            "Paintings_by_Nicholas_Roerich",
            "Paintings_by_Albert_Żamett",
            "Paintings_by_Maxim_Zyazin",
            "Paintings_by_Maxim_Zyazin",
            "Paintings_by_Stanislav_Yulianovich_Zhukovsky",
            "Paintings_by_Stanislav_Yulianovich_Zhukovsky_in_the_National_Arts_Museum_of_the_Republic_of_Belarus",
            "Paintings_by_Stanislav_Yulianovich_Zhukovsky_in_the_Russian_Museum",
            "Paintings_by_Pyotr_Zakharovich_Zakharov-Chechenets",
            "Paintings_by_Pyotr_Zakharovich_Zakharov-Chechenets_in_the_Hermitage",
            "Portrait_paintings_by_Pyotr_Zakharovich_Zakharov-Chechenets",
            "Portraits_by_Pyotr_Zakharovich_Zakharov-Chechenets",
            "Genre_paintings_by_Pyotr_Zakharovich_Zakharov-Chechenets",
            "Paintings_by_Pyotr_Petrovich_Zabolotsky",
            "Drawings_by_Petr_Efimovich_Zabolotskiyy",
            "Paintings_by_Pyotr_Petrovich_Zabolotsky_in_the_Hermitage",
            "Portrait_paintings_by_Pyotr_Petrovich_Zabolotsky",
            "Paintings_by_Petr_Efimovich_Zabolotskiy",
            "Paintings_by_Petr_Zabolotskiy_in_the_Yekaterinburg_Museum_of_Fine_Arts",
            "Paintings_by_Petr_Zabolotskiy_in_the_Tretyakov_Gallery",
            "Paintings_in_the_Mikhail_Kroshitsky_Sevastopol_art_museum_by_Konstantin_Yuon",
            "Konstantin_Yuon",
            "Interiors_by_Albert_Żamett",
            "Drawings_by_Albert_Żamett",
            "Paintings_in_the_Mikhail_Kroshitsky_Sevastopol_Art_Museum_by_Ivan_Yendogurov",
            "Ivan_Yendogurov",
            "Paintings_by_Nikolai_Yaroshenko",
            "Genre_paintings_by_Nikolai_Yaroshenko",
            "Landscape_paintings_by_Nikolai_Yaroshenko",
            "Portraits_by_Nikolai_Yaroshenko",
            "Paintings_of_Ukraine_by_Nikolai_Yaroshenko",
            "Naval_ships_of_Russia_by_Community_of_Saint_Eugenia_%26_Vsevolozhsky_after_Prokudin-Gorsky",
            "Set_designs_by_Mikhail_Vrubel",
            "Self-portraits_by_Mikhail_Vrubel",
            "The_Tale_of_Tsar_Saltan_(Vrubel)",
            "The_Swan_Princess_by_Mikhail_Vrubel",
            "Sculptures_by_Mikhail_Vrubel_in_the_Russian_museum",
            "Sculptures_by_Vrubel_in_the_State_Tretyakov_Gallery",
            "Mayolicas_by_Mikhail_Vrubel",
            "Princess_of_Dreams_(Mikhail_Vrubel%27s_panel)",
            "Volga_and_Mikula_fireplace_(Vrubel)",
            "Posters_by_Mikhail_Vrubel",
            "Portraits_by_Mikhail_Vrubel",
            "Paintings_by_Mikhail_Vrubel",
            "Paintings_by_Mikhail_Vrubel_in_the_Russian_museum",
            "Paintings_by_Mikhail_Vrubel_in_the_Tretyakov_Gallery",
            "Bogatyr_by_Vrubel_(1898-9,_Russian_museum)",
            "Demon_Flying_(Mikhail_Vrubel)",
            "Morning_by_Vrubel",
            "Six-winged_Seraph_(Azrael)_by_Vrubel",
            "Demon_Downcast_(Mikhail_Vrubel)",
            "Demon_Seated_(Mikhail_Vrubel)",
            "Pan_by_Vrubel",
            "Daemon_and_Tamara_(Vrubel)",
            "Interiors_of_St._Cyril%27s_Monastery",
            "Icons_by_Mikhail_Vrubel",
            "Blessed_Virgin_Mary_With_Child_(Vrubel)",
            "Google_Art_Project_works_by_Mikhail_Vrubel",
            "Drawings_by_Mikhail_Vrubel",
            "Drawings_by_Mikhail_Vrubel_in_the_Russian_museum",
            "Drawings_by_Mikhail_Vrubel_in_the_Tretyakov_Gallery",
            "Konvoets",
            "Paintings_by_Anatoly_Vorona",
            "Portraits_by_Anatoly_Vorona",
            "Graphics_by_Anatoly_Vorona",
            "Drawings_by_Anatoly_Vorona",
            "Endpieces_(book_illustration)_by_Anatoly_Vorona",
            "Headpieces_(book_illustration)_by_Anatoly_Vorona",
            "Vorona%27s_Toys",
            "Paintings_by_Yefim_Volkov",
            "Landscape_paintings_by_Yefim_Volkov",
            "Paintings_by_Vladimir_Kosov"
    };

    private final String urlWikimediaSearchByCategory = "https://commons.wikimedia.org/w/api.php?action=query&list=categorymembers&cmtype=file&cmlimit=500&format=json&cmtitle=Category:";
    private final String urlWikimediaGetDescriptionOfImageByPageId = "https://commons.wikimedia.org/w/api.php?action=query&prop=imageinfo&iiprop=extmetadata&iiextmetadatafilter=ImageDescription&format=json&pageids=";
    private final String urlWikimediaGetSourceOfImageByPageId = "https://commons.wikimedia.org/w/api.php?action=query&prop=pageimages&piprop=original&format=json&pageids=";

    public void getRandomImageFromWikimedia(ChatovyonokBot bot, Update update) {
        String randomPage = null;
        try {
            randomPage = getRandomPageIdFromCategory();
            bot.sendImageWithMessage(update.getMessage().getChatId().toString(), getImageFromPageAsFile(randomPage), getDescriptionOfImageFromPageId(randomPage));
        } catch (ParseException | IOException e) {
            System.out.println(e);
            bot.sendMessage(update.getMessage().getChatId().toString(), "Ой.. Ошибка!");
        }
    }

    private String getDescriptionOfImageFromPageId(String pageId) throws ParseException, IOException {
        String urlPage = urlWikimediaGetDescriptionOfImageByPageId + pageId;

        Document doc  = Jsoup.connect(urlPage).ignoreContentType(true).ignoreHttpErrors(true).get();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(doc.text());

        JSONObject jsonObject1 = (JSONObject) jsonObject.get("query");
        JSONObject jsonObject2 = (JSONObject) jsonObject1.get("pages");
        JSONObject jsonObject3 = (JSONObject) jsonObject2.get(pageId);
        JSONArray jsonObject4 = (JSONArray) jsonObject3.get("imageinfo");
        System.out.println("CATEGORY MEMBERS SIZE IS " + jsonObject4.size() + " OF: " + pageId);
//
//        try {
//
//            JSONObject jsonObject5 = (JSONObject) jsonObject4.get(0);
//            JSONObject jsonObject6 = (JSONObject) jsonObject5.get("extmetadata");
//            JSONObject jsonObject7 = (JSONObject) jsonObject6.get("ImageDescription");
//            String imageUrl = jsonObject7.get("value").toString();
//            return imageUrl;
//
//        } catch (ClassCastException e) {

            String imageUrl = jsonObject3.get("title").toString();
            imageUrl = imageUrl.replace(".jpg", "");
            imageUrl = imageUrl.replace(".JPG", "");
            imageUrl = imageUrl.replace(".png", "");
            imageUrl = imageUrl.replace(".PNG", "");
            imageUrl = imageUrl.replace("File:", "");
            return imageUrl;

//        }
    }

    private InputStream getImageFromPageAsFile(String pageId) throws ParseException, IOException {
        String urlPage = urlWikimediaGetSourceOfImageByPageId + pageId;

        Document doc  = Jsoup.connect(urlPage).ignoreContentType(true).ignoreHttpErrors(true).get();

        JSONObject jsonObject = (JSONObject) jsonParser.parse(doc.text());
        JSONObject jsonObject1 = (JSONObject) jsonObject.get("query");
        JSONObject jsonObject2 = (JSONObject) jsonObject1.get("pages");
        JSONObject jsonObject3 = (JSONObject) jsonObject2.get(pageId);
        JSONObject jsonObject4 = (JSONObject) jsonObject3.get("original");
        String imageUrl =jsonObject4.get("source").toString();

        URL url = new URL(imageUrl);
        InputStream input = url.openStream();
        return input;
    }

    private String getRandomPageIdFromCategory() throws ParseException, IOException {
        int randomCategory = new Random().nextInt(categories.length);
        String urlCategory = urlWikimediaSearchByCategory + categories[randomCategory];

        Document doc  = Jsoup.connect(urlCategory).ignoreContentType(true).get();
        String getJson = doc.text();

        JSONObject jsonObject = (JSONObject) jsonParser.parse(getJson);
        JSONObject jsonObject1 = (JSONObject) jsonObject.get("query");
        JSONArray jsonArray = (JSONArray) jsonObject1.get("categorymembers");
        System.out.println("CATEGORY MEMBERS SIZE IS " + jsonArray.size() + " OF: " + urlCategory);

        int randomPage = new Random().nextInt(jsonArray.size());
        JSONObject jsonObject2 = (JSONObject) jsonArray.get(randomPage);

        String pageId = jsonObject2.get("pageid").toString();
        System.out.println("PAGE ID : " + pageId);

        return pageId;
    }

}
