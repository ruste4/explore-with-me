package ru.practicum.explorewithme.event;

import javax.persistence.AttributeConverter;
import java.util.stream.Stream;

public class EventStatusConverter implements AttributeConverter<EventState, String> {
    @Override
    public String convertToDatabaseColumn(EventState eventState) {
        if (eventState == null) {
            return null;
        }
        return eventState.getVal();
    }

    @Override
    public EventState convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }

        return Stream.of(EventState.values())
                .filter(es -> es.getVal().equals(s))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
