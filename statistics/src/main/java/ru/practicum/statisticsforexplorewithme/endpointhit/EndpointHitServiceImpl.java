package ru.practicum.statisticsforexplorewithme.endpointhit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statisticsforexplorewithme.endpointhit.dto.EndpointHitCreateDto;
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
    public void saveHit(EndpointHitCreateDto createDto) {
        EndpointHit endpointHit = EndpointHitMapper.toEndpointHit(createDto);
        LocalDateTime now = LocalDateTime.now();
        endpointHit.setTimestamp(now);

        repository.save(endpointHit);
    }

    @Override
    public List<ViewStats> getStats(GetStatsParams params) {

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
