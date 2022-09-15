package ru.practicum.statisticsforexplorewithme.endpointhit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long>, EndpointHitRepositoryCustom {

}
