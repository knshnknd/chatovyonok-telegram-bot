package ru.knshnknd.chatovyonok.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.knshnknd.chatovyonok.model.enitites.Recipe;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {}
