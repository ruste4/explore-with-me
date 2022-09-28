package ru.practicum.statisticsforexplorewithme.endpointhit;

import lombok.RequiredArgsConstructor;
import ru.practicum.statisticsforexplorewithme.endpointhit.requestparams.GetStatsParams;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.List;

@RequiredArgsConstructor
public class EndpointHitRepositoryCustomImpl implements EndpointHitRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<Object[]> getStats(GetStatsParams params) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = builder.createQuery(Object[].class);
        Root<EndpointHit> root = query.from(EndpointHit.class);
        Predicate[] predicates = new Predicate[2];

        predicates[0] = root.get(EndpointHit_.uri).in(params.getUris());
        predicates[1] = builder.between(root.get(EndpointHit_.timestamp), params.getStart(), params.getEnd());

        query.where(predicates);

        if (params.isUnique()) {
            query.multiselect(root.get(EndpointHit_.app), root.get(EndpointHit_.uri), builder.countDistinct(root.get(EndpointHit_.ip)));
        } else {
            query.multiselect(root.get(EndpointHit_.app), root.get(EndpointHit_.uri), builder.count(root));
        }

        query.groupBy(root.get(EndpointHit_.app), root.get(EndpointHit_.uri));

        return entityManager.createQuery(query).getResultList();
    }
}
