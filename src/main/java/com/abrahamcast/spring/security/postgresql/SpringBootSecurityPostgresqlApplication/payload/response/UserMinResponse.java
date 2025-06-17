package com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.payload.response;

public class UserMinResponse {
    private Long id;
    private String username;

    public UserMinResponse(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
