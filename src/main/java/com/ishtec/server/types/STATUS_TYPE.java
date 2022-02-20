package com.ishtec.server.types;

public enum STATUS_TYPE {
    NEW_STATUS("NEW_STATUS"),
    VERIFIED("VERIFIED"),
    ACTIVE("ACTIVE"),
    CLOSED("CLOSED");
    private final String name;
    STATUS_TYPE(String name) {
        this.name = name;
    }
    @Override
    public String toString(){
        return name;
    }
}