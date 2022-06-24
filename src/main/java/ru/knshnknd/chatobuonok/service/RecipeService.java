package ru.knshnknd.chatobuonok.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.knshnknd.chatobuonok.dao.RecipeEntity;
import ru.knshnknd.chatobuonok.dao.RecipeRepository;

import java.util.List;
import java.util.Random;

@Service
public class RecipeService {

    @Autowired
    RecipeRepository recipeRepository;
    @Autowired
    YoutubeService youtubeService;

    // Получить случайный рецепт
    public String getRandomRecipe() {
        Long randomNumber = (long) new Random().nextInt(getRecipesNumber());
        RecipeEntity recipeEntity = recipeRepository.findById(randomNumber + 1).get();

        String response = "Рецепт №" + recipeEntity.getId()
                + " «" + recipeEntity.getName() + "»\n"
                + "Тип блюда: " + recipeEntity.getType() + ". "
                + (recipeEntity.getVeg() != 0 ? "Блюдо вегетарианское." : "Блюдо невегетарианское.") + "\n\n"
                + "Ингредиенты: " + recipeEntity.getIngredients() + "\n\n"
                + "Рецепт: " + recipeEntity.getRecipe() + "\n\n"
                + "Комментарии: " + recipeEntity.getComments() + "\n\n"
                + "Видео с Youtube: " +
                (recipeEntity.getNeedsVideo() != 0 ?
                youtubeService.getYoutubeVideo(recipeEntity.getName() + " рецепт")
                : " нет.");

        return response;
    }

    // Ответ, сколько всего рецептов знает бот
    public String getRecipesNumberMessage() {
        return "Всего рецептов знаю вот сколько: " +
                getRecipesNumber() +
                ".\n\nРецепты беру из книг Влада Пискунова. Там" +
                " описана настоящая русская кухня, а не бездушные советские блюда на майонезе.";
    }

    // Получить количество всех рецептов
    private int getRecipesNumber() {
        List<RecipeEntity> recipeEntityList = recipeRepository.findAll();
        return recipeEntityList.size();
    }
}
