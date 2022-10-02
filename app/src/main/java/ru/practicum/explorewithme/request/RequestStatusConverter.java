package ru.practicum.explorewithme.request;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter
public class RequestStatusConverter implements AttributeConverter<RequestStatus, String> {
    @Override
    public String convertToDatabaseColumn(RequestStatus requestStatus) {
        if (requestStatus == null) {
            return null;
        }

        return requestStatus.getVal();
    }

    @Override
    public RequestStatus convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }

        return Stream.of(RequestStatus.values())
                .filter(es -> es.getVal().equals(s))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
