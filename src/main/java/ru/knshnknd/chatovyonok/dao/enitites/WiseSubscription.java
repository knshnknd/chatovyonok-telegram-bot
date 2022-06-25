package ru.knshnknd.chatovyonok.dao.enitites;

import javax.persistence.*;

@Entity
public class WiseSubscription {

    @Id
    @Column
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column
    private String chatId;

    @Column
    private Boolean isActive;

    public WiseSubscription() {
    }

    public WiseSubscription(String chatId, Boolean isActive) {
        this.chatId = chatId;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
