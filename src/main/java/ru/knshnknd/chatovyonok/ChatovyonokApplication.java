package ru.knshnknd.chatovyonok;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.knshnknd.chatovyonok.bot.ChatovyonokBot;

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
