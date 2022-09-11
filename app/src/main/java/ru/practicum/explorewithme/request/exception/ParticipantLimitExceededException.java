package ru.practicum.explorewithme.request.exception;

public class ParticipantLimitExceededException extends RuntimeException {
    public ParticipantLimitExceededException(String message) {
        super(message);
    }
}
