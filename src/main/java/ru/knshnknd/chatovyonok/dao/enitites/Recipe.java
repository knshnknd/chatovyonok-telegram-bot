package ru.knshnknd.chatovyonok.dao.enitites;

import javax.persistence.*;

    @Entity(name = "recipes")
    public class Recipe {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String type;

    @Column(nullable = false)
    private Boolean isMealVegetarian;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String ingredients;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recipe;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column
    private Boolean doesRecipeNeedsVideo;

    public Recipe() {
    }

    public Recipe(
                        String name,
                        String type,
                        Boolean isMealVegetarian,
                        String ingredients,
                        String recipe,
                        String comments,
                        Boolean doesRecipeNeedsVideo) {
        this.name = name;
        this.type = type;
        this.isMealVegetarian = isMealVegetarian;
        this.ingredients = ingredients;
        this.recipe = recipe;
        this.comments = comments;
        this.doesRecipeNeedsVideo = doesRecipeNeedsVideo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getVeg() {
        return isMealVegetarian;
    }

    public void setVeg(Boolean veg) {
        isMealVegetarian = veg;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Boolean getDoesRecipeNeedsVideo() {
        return doesRecipeNeedsVideo;
    }

    public void setDoesRecipeNeedsVideo(Boolean needsVideo) {
        this.doesRecipeNeedsVideo = needsVideo;
    }
}
