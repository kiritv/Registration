package com.ishtec.server.exceptions;

public class LookupStatusNotFoundException extends RuntimeException {
    public LookupStatusNotFoundException(String statusName) {
        super("LookupStatus: " + statusName + " was not found.");
    }
}
