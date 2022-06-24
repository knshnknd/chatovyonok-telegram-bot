package ru.knshnknd.chatobuonok.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "updates")
public class UpdateEntity {

    @Id
    private String chatId;

    @Column
    private Long updateCount;

    @Column(columnDefinition = "TINYINT")
    private Integer wantsWise;

    @Column(columnDefinition = "TINYINT")
    private Integer wantsWeather;

    @Column(columnDefinition = "TEXT")
    private String weatherCity;

    public UpdateEntity() {}

    public UpdateEntity(String chatId, Long updateCount, Integer wantsWise, Integer wantsWeather, String weatherCity) {
        this.chatId = chatId;
        this.updateCount = updateCount;
        this.wantsWise = wantsWise;
        this.wantsWeather = wantsWeather;
        this.weatherCity = weatherCity;
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

    public Integer getWantsWise() {
        return wantsWise;
    }

    public void setWantsWise(Integer wantsWise) {
        this.wantsWise = wantsWise;
    }

    public Integer getWantsWeather() {
        return wantsWeather;
    }

    public void setWantsWeather(Integer wantsWeather) {
        this.wantsWeather = wantsWeather;
    }

    public String getWeatherCity() {
        return weatherCity;
    }

    public void setWeatherCity(String weatherCity) {
        this.weatherCity = weatherCity;
    }
}