package ru.knshnknd.chatovyonok.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.knshnknd.chatovyonok.model.enitites.Update;
import ru.knshnknd.chatovyonok.model.repositories.UpdateRepository;

import java.util.Optional;

@Service
@Transactional
public class UpdateService {
    @Autowired
    private UpdateRepository updateRepository;

    // Добавляем +1 к update_count у чата по chat_id в БД при каждом вызове бота,
    // если такого кортежа нет, то добавляем новый
    public void addUpdate(String chatId) {
            Optional<Update> updateEntityOptional = updateRepository.findUpdateByChatId(chatId);

        if (updateEntityOptional.isPresent()) {
            Update update = updateEntityOptional.get();
            Long previousUpdateCount = update.getUpdateCount();
            update.setUpdateCount(previousUpdateCount + 1L);
            updateRepository.save(update);
        } else {
            Update newUpdate = new Update(chatId, 1L);
            updateRepository.save(newUpdate);
        }
    }
}
