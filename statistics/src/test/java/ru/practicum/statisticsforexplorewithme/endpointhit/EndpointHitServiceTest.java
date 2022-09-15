package ru.practicum.statisticsforexplorewithme.endpointhit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.statisticsforexplorewithme.endpointhit.dto.ViewStats;
import ru.practicum.statisticsforexplorewithme.endpointhit.requestparams.GetStatsParams;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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
    public void getStatsSuccess() {
        Map<Long, EndpointHit> hitMap = generateAndPersistEndpointHits(5);
        GetStatsParams params = GetStatsParams.builder()
                .uris(hitMap.values().stream().map(EndpointHit::getUri).collect(Collectors.toList()))
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .unique(true)
                .build();

        for (long i : hitMap.keySet()) {
            EndpointHit eHit = hitMap.get(i);
            System.out.printf("app=%s, uri=%s, ip=%s%n", eHit.getApp(), eHit.getUri(), eHit.getIp());
        }

        List<ViewStats> stats = ehService.getStats(params);

//        stats.forEach(item -> System.out.println(item));
        System.out.println(1);
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

        EndpointHit hit = EndpointHit.builder()
                .app("TestApp")
                .uri("stats/22")
                .ip("213.59.151.21")
                .timestamp(LocalDateTime.now())
                .build();

        Long id = testEntityManager.persistAndGetId(hit, Long.class);
        res.put(id, hit);

        return res;
    }

}