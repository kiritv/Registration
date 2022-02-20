package com.ishtec.server.types;

public enum ROLE_TYPE {
    ROLE_USER("ROLE_USER"),
    ROLE_ADMIN("ROLE_ADMIN");
    private final String name;
    ROLE_TYPE(String name) {
        this.name = name;
    }
    @Override
    public String toString(){
        return name;
    }
}