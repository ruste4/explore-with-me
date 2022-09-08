package ru.practicum.explorewithme.event;

import java.util.Optional;

public enum EventState {

    PENDING("PENDING"),

    PUBLISHED("PUBLISHED"),

    CANCELED("CANCELED");

    private final String val;

    EventState(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public static EventState findByName(String name) {
        for (EventState status : values()) {
            if (status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }

        throw new IllegalArgumentException(String.format("EventState with name:%s not exist", name));
    }
}
