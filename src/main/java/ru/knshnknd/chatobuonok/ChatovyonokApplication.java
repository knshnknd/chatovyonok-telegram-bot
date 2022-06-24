package ru.knshnknd.chatobuonok;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.knshnknd.chatobuonok.bot.ChatovyonokBot;
import ru.knshnknd.chatobuonok.dao.RecipeEntity;
import ru.knshnknd.chatobuonok.dao.RecipeRepository;

@SpringBootApplication
@EnableScheduling
public class ChatovyonokApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ChatovyonokApplication.class, args);
		ChatovyonokBot bot = context.getBean("chatovyonokBot", ChatovyonokBot.class);

		try {
			TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
			telegramBotsApi.registerBot(bot);
		} catch (TelegramApiException ex) {
			System.out.println(ex);
		}
	}
}
