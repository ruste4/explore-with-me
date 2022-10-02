package ru.practicum.explorewithme.exception;

public class ConditionsNotMetException extends RuntimeException {

    private final String reason;

    public ConditionsNotMetException(String message, String reason) {
        super(message);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
