package ru.practicum.statisticsforexplorewithme.endpointhit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.statisticsforexplorewithme.endpointhit.dto.EndpointHitCreateDto;
import ru.practicum.statisticsforexplorewithme.endpointhit.dto.EndpointHitDto;
import ru.practicum.statisticsforexplorewithme.endpointhit.dto.ViewStats;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class EndpointHitServiceTest {
    @Autowired
    private final TestEntityManager testEntityManager;

    @Autowired
    private final EndpointHitService ehService;

    @BeforeEach
    public void beforeEachEventServiceTest() {
        testEntityManager.clear();
    }

    @Test
    public void saveHitSuccess() {
        EndpointHitCreateDto createDto1 = generateEndpointHitCreateDto();

        EndpointHitDto hit = ehService.saveHit(createDto1);

        EndpointHit foundHit = testEntityManager.find(EndpointHit.class, hit.getId());

        assertAll(
                () -> assertEquals(hit.getApp(), foundHit.getApp()),
                () -> assertEquals(hit.getIp(), foundHit.getIp()),
                () -> assertEquals(hit.getUri(), foundHit.getUri())
        );

    }

    @Test
    public void getStatsSuccessWithUniqueTrue() {
        Map<Long, EndpointHit> hitMap = generateAndPersistEndpointHits(5);

        List<String> uris = hitMap.values().stream().map(EndpointHit::getUri).collect(Collectors.toList());
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);


        for (long i : hitMap.keySet()) {
            EndpointHit eHit = hitMap.get(i);
            System.out.printf("app=%s, uri=%s, ip=%s%n", eHit.getApp(), eHit.getUri(), eHit.getIp());
        }

        List<ViewStats> stats = ehService.getStats(start, end, uris, true);

        assertAll(
                () -> assertEquals(stats.get(0).getHits(), 1),
                () -> assertEquals(stats.get(1).getHits(), 1)
        );

    }

    @Test
    public void getStatsSuccessWithUniqueFalse() {
        Map<Long, EndpointHit> hitMap = generateAndPersistEndpointHits(5);

        for (long i : hitMap.keySet()) {
            EndpointHit eHit = hitMap.get(i);
            System.out.printf("app=%s, uri=%s, ip=%s%n", eHit.getApp(), eHit.getUri(), eHit.getIp());
        }

        List<String> uris = hitMap.values().stream().map(EndpointHit::getUri).collect(Collectors.toList());
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        List<ViewStats> stats = ehService.getStats(start, end, uris, false);

        assertAll(
                () -> assertEquals(stats.get(0).getHits(), 3),
                () -> assertEquals(stats.get(1).getHits(), 2)
        );

    }

    private Map<Long, EndpointHit> generateAndPersistEndpointHits(int quantity) {
        Map<Long, EndpointHit> res = new HashMap<>();

        for (int i = 0; i < quantity; i++) {
            EndpointHit hit = EndpointHit.builder()
                    .app("TestApp")
                    .uri("stats/" + ((i % 2 == 0) ? 22 : 33))
                    .ip("213.59.151." + ((i % 2 == 0) ? 22 : 33))
                    .timestamp(LocalDateTime.now())
                    .build();

            Long id = testEntityManager.persistAndGetId(hit, Long.class);
            res.put(id, hit);
        }

        return res;
    }

    private EndpointHitCreateDto generateEndpointHitCreateDto() {
        return EndpointHitCreateDto.builder()
                .app("TestApp")
                .uri("stats/" + ThreadLocalRandom.current().nextInt(1, 100))
                .ip("213.59.151." + ThreadLocalRandom.current().nextInt(10, 99))
                .build();
    }

}