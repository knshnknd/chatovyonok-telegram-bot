package ru.knshnknd.chatovyonok.jpa.enitites;

import javax.persistence.*;

@Entity
public class ArtSubscription {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String chatId;

    private Boolean isActive;

    public ArtSubscription() {
    }

    public ArtSubscription(String chatId, Boolean isActive) {
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

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
