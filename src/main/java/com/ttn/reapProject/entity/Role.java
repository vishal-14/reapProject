package com.ttn.reapProject.entity;

public enum Role {
    USER("User"),
    SUPERVISOR("Supervisor"),
    PRACTICE_HEAD("Practice head"),
    ADMIN("Admin");
    String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
