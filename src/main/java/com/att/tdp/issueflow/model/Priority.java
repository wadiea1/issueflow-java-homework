package com.att.tdp.issueflow.model;

public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public Priority next() {
        return switch (this) {
            case LOW -> MEDIUM;
            case MEDIUM -> HIGH;
            case HIGH -> CRITICAL;
            case CRITICAL -> CRITICAL;
        };
    }
}
