package ru.knshnknd.chatovyonok.model.enitites;

import javax.persistence.*;

@Entity
public class WeatherSubscription {

    @Id
    @Column
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column
    private String chatId;

    @Column
    private Boolean isActive;

    @Column(columnDefinition = "TEXT")
    private String weatherCity;

    public WeatherSubscription() {
    }

    public WeatherSubscription(String chatId, Boolean isActive, String weatherCity) {
        this.chatId = chatId;
        this.isActive = isActive;
        this.weatherCity = weatherCity;
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

    public String getWeatherCity() {
        return weatherCity;
    }

    public void setWeatherCity(String weatherCity) {
        this.weatherCity = weatherCity;
    }
}
