package ru.knshnknd.chatovyonok.dao.enitites;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "updates")
public class Update {

    @Id
    private String chatId;

    @Column
    private Long updateCount;

    public Update() {
    }

    public Update(String chatId, Long updateCount) {
        this.chatId = chatId;
        this.updateCount = updateCount;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public Long getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(Long updateCount) {
        this.updateCount = updateCount;
    }
}