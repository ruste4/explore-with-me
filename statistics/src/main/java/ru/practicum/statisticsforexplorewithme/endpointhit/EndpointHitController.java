package ru.practicum.statisticsforexplorewithme.endpointhit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statisticsforexplorewithme.endpointhit.dto.EndpointHitCreateDto;
import ru.practicum.statisticsforexplorewithme.endpointhit.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class EndpointHitController {

    private final EndpointHitService service;

    @PostMapping("/hit")
    public void saveVisiting(@RequestBody EndpointHitCreateDto createDto) {
        service.saveHit(createDto);
        log.info(createDto.toString());
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique
    ) {
        LocalDateTime rangeStart = LocalDateTime.parse(start);
        LocalDateTime rangeEnd = LocalDateTime.parse(end);

        return service.getStats(rangeStart, rangeEnd, uris, unique);
    }
}
