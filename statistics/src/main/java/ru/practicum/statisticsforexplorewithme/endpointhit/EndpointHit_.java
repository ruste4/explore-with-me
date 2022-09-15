package ru.practicum.statisticsforexplorewithme.endpointhit;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(EndpointHit.class)
public class EndpointHit_ {

    public static volatile SingularAttribute<EndpointHit, Long> id;

    public static volatile SingularAttribute<EndpointHit, String> app;

    public static volatile SingularAttribute<EndpointHit, String> uri;

    public static volatile SingularAttribute<EndpointHit, String> ip;
    public static volatile SingularAttribute<EndpointHit, LocalDateTime> timestamp;

}
