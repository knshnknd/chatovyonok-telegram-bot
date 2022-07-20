package ru.knshnknd.chatovyonok.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.knshnknd.chatovyonok.jpa.enitites.Update;

import java.util.Optional;

@Repository
public interface UpdateRepository extends JpaRepository<Update, String> {
    Optional<Update> findUpdateByChatId(String chatId);
}

