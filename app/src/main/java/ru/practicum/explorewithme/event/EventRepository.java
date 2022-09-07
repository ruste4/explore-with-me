package ru.practicum.explorewithme.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.user.User;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findAllByInitiator(User initiator, Pageable pageable);
}
