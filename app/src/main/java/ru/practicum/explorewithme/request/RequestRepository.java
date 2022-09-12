package ru.practicum.explorewithme.request;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.event.Event;
import ru.practicum.explorewithme.user.User;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByRequester(User user);

    List<Request> findAllByEvent(Event event);

    List<Request> findAllByEventAndStatus(Event event, RequestStatus status);
}
