package ru.practicum.explorewithme.event;

import ru.practicum.explorewithme.event.category.Category;
import ru.practicum.explorewithme.user.User;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(Event.class)
public class Event_ {
    public static volatile SingularAttribute<Event, User> initiator;
    public static volatile SingularAttribute<Event, EventState> state;
    public static volatile SingularAttribute<Event, Category> category;
    public static volatile SingularAttribute<Event, LocalDateTime> eventDate;
}
