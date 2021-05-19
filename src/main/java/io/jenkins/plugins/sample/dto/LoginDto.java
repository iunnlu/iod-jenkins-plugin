package io.jenkins.plugins.sample.dto;

import io.jenkins.plugins.sample.model.User;

public class LoginDto {
    private User user;
    private String access;
    private String refresh;
    private String expire_time;

    public LoginDto() {
    }

    public LoginDto(User user, String access, String refresh, String expire_time) {
        this.user = user;
        this.access = access;
        this.refresh = refresh;
        this.expire_time = expire_time;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getRefresh() {
        return refresh;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }

    public String getExpire_time() {
        return expire_time;
    }

    public void setExpire_time(String expire_time) {
        this.expire_time = expire_time;
    }

    @Override
    public String toString() {
        return "LoginDto{" +
                "user=" + user +
                ", access='" + access + '\'' +
                ", refresh='" + refresh + '\'' +
                ", expire_time='" + expire_time + '\'' +
                '}';
    }
}
