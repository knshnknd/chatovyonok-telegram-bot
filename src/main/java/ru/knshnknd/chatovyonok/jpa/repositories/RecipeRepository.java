package ru.knshnknd.chatovyonok.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.knshnknd.chatovyonok.jpa.enitites.Recipe;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {}
