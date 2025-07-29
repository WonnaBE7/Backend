package com.wonnabe.auth.dto;

public class SignupDTO {
    // 회원가입 시 프론트(Vue)에서 넘겨주는 데이터
    // 이름(name), 이메일(email), 비밀번호(password)를 담는 DTO
    // 프론트에서 보내는 JSON을 이 클래스가 자동으로 매핑받음
    private String name;
    private String email;
    private String password;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
