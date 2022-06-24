package ru.knshnknd.chatobuonok.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "user_info")
public class UserInfoEntity {

    @Id
    private Long userId;

    @Column
    private String name;

    @Column
    private Long wiseCount;

    @Column
    private Long wiseLimitCount;

    public UserInfoEntity() {}

    public UserInfoEntity(Long userId, String name, Long wiseCount, Long wiseLimitCount) {
        this.userId = userId;
        this.name = name;
        this.wiseCount = wiseCount;
        this.wiseLimitCount = wiseLimitCount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
