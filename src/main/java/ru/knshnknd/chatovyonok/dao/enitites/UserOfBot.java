package ru.knshnknd.chatovyonok.dao.enitites;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "user")
public class UserOfBot {

    @Id
    private Long userId;

    @Column
    private String username;

    @Column
    private Long wiseCount;

    @Column
    private Long wiseLimitCount;

    public UserOfBot() {}

    public UserOfBot(Long userId, String username, Long wiseCount, Long wiseLimitCount) {
        this.userId = userId;
        this.username = username;
        this.wiseCount = wiseCount;
        this.wiseLimitCount = wiseLimitCount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getWiseCount() {
        return wiseCount;
    }

    public void setWiseCount(Long wiseCount) {
        this.wiseCount = wiseCount;
    }

    public Long getWiseLimitCount() {
        return wiseLimitCount;
    }

    public void setWiseLimitCount(Long wiseLimitCount) {
        this.wiseLimitCount = wiseLimitCount;
    }
}
