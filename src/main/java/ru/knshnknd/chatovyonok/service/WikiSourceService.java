package ru.knshnknd.chatovyonok.service;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONTokener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WikiSourceService {

    @Autowired
    private JSONParser jsonParser;

    private static final String URL_FOR_RANDOM_PAGE = "https://ru.wikisource.org/wiki/%D0%A1%D0%BB%D1%83%D0%B6%D0%B5%D0%B1%D0%BD%D0%B0%D1%8F:RandomInCategory/%D0%9C%D0%AD%D0%A1%D0%91%D0%95";
    private static final String URL_FOR_PARSING = "https://ru.wikisource.org/w/api.php?action=parse&prop=wikitext&format=json&page=";

    public String getRandomDictionaryArticle() {
        String result = "";

        HttpPost post = new HttpPost(URL_FOR_RANDOM_PAGE);
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();

        String randomPage = "";

        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
             CloseableHttpResponse response = httpClient.execute(post)) {

            for (Header s : response.getAllHeaders()) {
                if (s.getName().equals("location")) {
                    randomPage = s.getElements()[0].toString().replace("https://ru.wikisource.org/wiki/", "");
                    System.out.println("RANDOM PAGE = " + randomPage);
                }
            }

            String urlPage = URL_FOR_PARSING + randomPage;
            System.out.println(URL_FOR_PARSING);

            Document doc  = Jsoup.connect(urlPage).ignoreContentType(true).ignoreHttpErrors(true).get();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(doc.text());
            System.out.println("DOKUMENT");
            System.out.println(jsonObject);

            String full_parsed_text = jsonObject.toString();
            int indexf = full_parsed_text.indexOf('\'');
            int indexl = full_parsed_text.indexOf("[[Категория");
            full_parsed_text = full_parsed_text.substring(indexf, indexl);
            full_parsed_text = full_parsed_text.replaceFirst("'''", "<b>");
            full_parsed_text = full_parsed_text.replaceFirst("'''", "</b>");
            full_parsed_text = full_parsed_text.replace("''", "");
            full_parsed_text = full_parsed_text.replace("\\n\\n", "");
            full_parsed_text = full_parsed_text.replace("\\u2014", "—");
            full_parsed_text = full_parsed_text.replace("[[..\\/", "(");
            full_parsed_text = full_parsed_text.replace("|", ", ");
            full_parsed_text = full_parsed_text.replace("]]", ")");
            full_parsed_text = full_parsed_text.replace("[[МЭСБЕ\\/", "(");
            System.out.println(full_parsed_text);

            full_parsed_text = full_parsed_text + "\n\n – Малый энциклопедический словарь Брокгауза и Ефрона.";

            if (full_parsed_text.length() >= 4096) {
                full_parsed_text = getRandomDictionaryArticle();
            }

            result = full_parsed_text;

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
