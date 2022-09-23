package ru.practicum.statisticsforexplorewithme.endpointhit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statisticsforexplorewithme.endpointhit.dto.EndpointHitCreateDto;
import ru.practicum.statisticsforexplorewithme.endpointhit.dto.EndpointHitDto;
import ru.practicum.statisticsforexplorewithme.endpointhit.dto.ViewStats;
import ru.practicum.statisticsforexplorewithme.endpointhit.requestparams.GetStatsParams;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class EndpointHitServiceImpl implements EndpointHitService {

    private final EndpointHitRepository repository;

    @Override
    public EndpointHitDto saveHit(EndpointHitCreateDto createDto) {
        EndpointHit endpointHit = EndpointHitMapper.toEndpointHit(createDto);
        LocalDateTime now = LocalDateTime.now();
        endpointHit.setTimestamp(now);

        return EndpointHitMapper.toEndpointHitDto(repository.save(endpointHit));
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {

        GetStatsParams params = GetStatsParams.builder()
                .start(start)
                .end(end)
                .uris(uris)
                .unique(unique)
                .build();

        return repository.getStats(params).stream()
                .map(item -> ViewStats
                        .builder()
                        .app(item[0].toString())
                        .uri(item[1].toString())
                        .hits(Integer.valueOf(item[2].toString()))
                        .build())
                .collect(Collectors.toList());
    }

}
