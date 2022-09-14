package ru.practicum.explorewithme.event;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.explorewithme.event.category.Category;
import ru.practicum.explorewithme.event.category.Category_;
import ru.practicum.explorewithme.request.Request;
import ru.practicum.explorewithme.request.Request_;
import ru.practicum.explorewithme.user.User;
import ru.practicum.explorewithme.user.User_;

import javax.persistence.criteria.*;
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

    public static Specification<Event> hasEventCategory(Long[] categories) {
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

    public static Specification<Event> hasTextInAnnotationOrDescription(String text) {
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get(Event_.annotation)), "%" + text.toLowerCase() + "%"),
                builder.like(builder.lower(root.get(Event_.description)), "%" + text.toLowerCase() + "%")
        );
    }

    public static Specification<Event> isPaid(Boolean isPaid) {
        return (root, query, builder) -> {
            if (isPaid != null) {
                return builder.equal(root.get(Event_.paid), isPaid);
            }

            return null;
        };
    }

    public static Specification<Event> isEventAvailable(boolean isAvailable) {
        if (!isAvailable) {
            return null;
        }
        return (root, query, builder) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<Request> subRoot = sub.from(Request.class);
            Join<Request, Event> subEvents = subRoot.join(Request_.event.getName());
            sub.select(builder.count(subRoot.get(Request_.id)));
            sub.where(builder.equal(root.get(Event_.id), subEvents.get(Event_.id)));
            return builder.lessThan(sub, root.get(Event_.participantLimit));
        };
    }


}
