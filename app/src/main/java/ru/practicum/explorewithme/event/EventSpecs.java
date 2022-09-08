package ru.practicum.explorewithme.event;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.explorewithme.event.category.Category;
import ru.practicum.explorewithme.event.category.Category_;
import ru.practicum.explorewithme.user.User;
import ru.practicum.explorewithme.user.User_;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.time.LocalDateTime;

public class EventSpecs {
    public static Specification<Event> hasInitiationIds(long[] userIds) {
        return (root, query, builder) -> {
            Join<Event, User> users = root.join(Event_.initiator, JoinType.LEFT);
            CriteriaBuilder.In<Long> inClause = builder.in(users.get(User_.id));
            if (userIds != null && userIds.length != 0) {
                for (long userId : userIds) {
                    inClause.value(userId);
                }
            }

            return inClause;
        };
    }

    public static Specification<Event> hasEventStates(String[] states) {
        return (root, query, builder) -> {
            CriteriaBuilder.In<EventState> inClause = builder.in(root.get(Event_.state));
            if (states != null && states.length != 0) {
                for (String state : states) {
                    inClause.value(EventState.findByName(state));
                }
            }

            return inClause;
        };
    }

    public static Specification<Event> hasEventCategory(long[] categories) {
        return (root, query, builder) -> {
            Join<Event, Category> categoryJoin = root.join(Event_.category, JoinType.LEFT);
            CriteriaBuilder.In<Long> inClause = builder.in(categoryJoin.get(Category_.id));
            if (categories != null && categories.length != 0) {
                for (long categoryId : categories) {
                    inClause.value(categoryId);
                }
            }

            return inClause;
        };
    }

    public static Specification<Event> betweenDates(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        return (root, query, builder) -> builder.between(root.get(Event_.eventDate), rangeStart, rangeEnd);
    }
}
