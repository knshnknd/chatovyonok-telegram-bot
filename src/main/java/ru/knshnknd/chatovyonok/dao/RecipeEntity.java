package ru.knshnknd.chatovyonok.dao;

import javax.persistence.*;

    @Entity(name = "recipes")
    public class RecipeEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String type;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private Integer isVeg;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String ingredients;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recipe;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(columnDefinition = "TINYINT")
    private Integer needsVideo;

    public RecipeEntity() {
    }

    public RecipeEntity(String name) {
        this.name = name;
    }

    public RecipeEntity(
                        String name,
                        String type,
                        Integer isVeg,
                        String ingredients,
                        String recipe,
                        String comments,
                        Integer needsVideo) {
        this.name = name;
        this.type = type;
        this.isVeg = isVeg;
        this.ingredients = ingredients;
        this.recipe = recipe;
        this.comments = comments;
        this.needsVideo = needsVideo;
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

    public Integer getVeg() {
        return isVeg;
    }

    public void setVeg(Integer veg) {
        isVeg = veg;
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

    public Integer getNeedsVideo() {
        return needsVideo;
    }

    public void setNeedsVideo(Integer needsVideo) {
        this.needsVideo = needsVideo;
    }
}
