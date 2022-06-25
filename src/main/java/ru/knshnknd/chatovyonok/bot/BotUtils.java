package ru.knshnknd.chatovyonok.bot;

public class BotUtils {

    public static boolean isTextMessageHasAnyWordsMore(String[] textFromMessage) {
        return textFromMessage.length >= 2;
    }

    public static boolean isTextInteger(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            System.out.println(e);
        }
        return false;
    }

}
