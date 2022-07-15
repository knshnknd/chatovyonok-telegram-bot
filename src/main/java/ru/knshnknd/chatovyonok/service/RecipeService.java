package ru.knshnknd.chatovyonok.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.knshnknd.chatovyonok.model.enitites.Recipe;
import ru.knshnknd.chatovyonok.model.repositories.RecipeRepository;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private YoutubeService youtubeService;

    // Получить случайный рецепт
    public String getRandomRecipe() {
        if (getRecipesNumber() > 0) {
            Long randomNumber = (long) new Random().nextInt(getRecipesNumber());
            Optional<Recipe> recipeEntityOptional = recipeRepository.findById(randomNumber + 1);
            Recipe recipe = recipeEntityOptional.get();
            return getFormatRecipe(recipe);
        } else {
            return "Ошибка... Рецептов нет!";
        }
    }

    public String getRecipesNumberMessage() {
        return "Всего рецептов знаю вот сколько: " +
                getRecipesNumber() +
                ".\n\nРецепты беру из книг Влада Пискунова. Там" +
                " описана настоящая русская кухня, а не бездушные советские блюда на майонезе.";
    }

    private String getFormatRecipe(Recipe recipe) {
        return "Рецепт №" + recipe.getId()
                + " «" + recipe.getName() + "»\n"
                + "Тип блюда: " + recipe.getType() + ". "
                + getDescriptionOfMealIfVegetarianOrNot(recipe.getVeg()) + "\n\n"
                + "Ингредиенты: " + recipe.getIngredients() + "\n\n"
                + "Рецепт: " + recipe.getRecipe() + "\n\n"
                + "Комментарии: " + recipe.getComments() + "\n\n"
                + "Видео с Youtube: "
                + getDescriptionIfNeedsVideo(recipe.getDoesRecipeNeedsVideo(), recipe.getName());
    }

    private String getDescriptionOfMealIfVegetarianOrNot(Boolean isVegetarian) {
        if (isVegetarian) {
            return "Блюдо вегетарианское.";
        } else {
            return "Блюдо невегетарианское.";
        }
    }

    private String getDescriptionIfNeedsVideo(Boolean needsVideo, String recipeName) {
        if (needsVideo) {
            return youtubeService.getYoutubeVideo(recipeName + " рецепт");
        } else {
            return "нет.";
        }
    }

    private int getRecipesNumber() {
        List<Recipe> recipeList = recipeRepository.findAll();
        return recipeList.size();
    }
}
