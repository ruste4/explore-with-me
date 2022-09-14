package ru.practicum.explorewithme.request;


import ru.practicum.explorewithme.event.Event;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Request.class)
public class Request_ {

    public static volatile SingularAttribute<Request, Long> id;

    public static volatile SingularAttribute<Request, Event> event;

    public static final String EVENT = "event";
}
