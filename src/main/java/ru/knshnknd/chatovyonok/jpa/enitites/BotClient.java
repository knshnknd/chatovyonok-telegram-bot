package ru.knshnknd.chatovyonok.jpa.enitites;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BotClient {

    @Id
    private Long userId;

    private String username;

    private Long wiseCount;

    private Long wiseLimitCount;

    private boolean canAttack;

    public BotClient() {}

    public BotClient(Long userId, String username, Long wiseCount, Long wiseLimitCount) {
        this.userId = userId;
        this.username = username;
        this.wiseCount = wiseCount;
        this.wiseLimitCount = wiseLimitCount;
        this.canAttack = true;
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

    public boolean isCanAttack() {
        return canAttack;
    }

    public void setCanAttack(boolean canAttack) {
        this.canAttack = canAttack;
    }
}
