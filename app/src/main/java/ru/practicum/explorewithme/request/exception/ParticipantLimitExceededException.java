package ru.practicum.explorewithme.request.exception;

import ru.practicum.explorewithme.exception.ConditionsNotMetException;

public class ParticipantLimitExceededException extends ConditionsNotMetException {
    public ParticipantLimitExceededException(String reason) {
        super("Participant limit exceeded", reason);
    }
}
