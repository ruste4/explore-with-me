package ru.practicum.explorewithme.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.explorewithme.client.dto.HitDto;
import ru.practicum.explorewithme.client.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class StatisticClient {

    private final RestTemplate rest;


    private final String serverUrl;

    @Autowired
    public StatisticClient(@Value("${statistic-server.url}") String serverUrl) {
        this.rest = new RestTemplate();
        this.serverUrl = serverUrl;
    }

    public void sendHitAtStaticServer(String app, String uri, String ip) {
        HitDto hit = HitDto.builder().app(app).uri(uri).ip(ip).build();
        HttpEntity<HitDto> request = new HttpEntity<>(hit);
        rest.postForObject(serverUrl + "/hit", request, HitDto.class);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, Set<String> uris, boolean unique) {
        String requestUri = serverUrl + "/stats?start={start}&end={end}&uris={uris}&unique={unique}";

        Map<String, String> urlParam = new HashMap<>();
        urlParam.put("start", start.toString());
        urlParam.put("end", end.toString());
        urlParam.put("uris", String.join(",", uris));
        urlParam.put("unique", Boolean.toString(unique));

        ResponseEntity<ViewStats[]> entity = rest.getForEntity(requestUri, ViewStats[].class, urlParam);

        return entity.getBody() != null ? Arrays.asList(entity.getBody()) : Collections.emptyList();
    }
}
