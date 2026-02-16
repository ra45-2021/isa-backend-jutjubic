package com.jutjubic.service;

import com.jutjubic.domain.PopularVideo;
import com.jutjubic.domain.VideoDailyView;
import com.jutjubic.domain.VideoViewCrdt;
import com.jutjubic.repository.PopularVideoRepository;
import com.jutjubic.repository.VideoDailyViewRepository;
import com.jutjubic.repository.VideoViewCrdtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularVideoETLService {

    private final VideoViewCrdtRepository videoViewCrdtRepository;
    private final PopularVideoRepository popularVideoRepository;
    private final VideoDailyViewRepository dailyViewRepository;

    @Scheduled(cron = "0 0 3 * * ?")
    public void runETL() {

        log.info("Starting Popular Video ETL pipeline...");

        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);

        List<VideoDailyView> last7Days =
                dailyViewRepository.findByViewDateBetween(sevenDaysAgo, today);

        Map<Long, Long> videoScores = new HashMap<>();

        for (VideoDailyView entry : last7Days) {

            long daysAgo = today.toEpochDay() - entry.getViewDate().toEpochDay();
            long weight = 7 - daysAgo;

            long weighted = entry.getViewCount() * weight;

            videoScores.merge(entry.getVideoId(), weighted, Long::sum);
        }

        List<Map.Entry<Long, Long>> sorted =
                videoScores.entrySet()
                        .stream()
                        .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                        .limit(3)
                        .toList();

        PopularVideo result = new PopularVideo();
        result.setPipelineRunAt(Instant.now());

        if (sorted.size() > 0) {
            result.setVideo1Id(sorted.get(0).getKey());
            result.setVideo1Score(sorted.get(0).getValue());
        }
        if (sorted.size() > 1) {
            result.setVideo2Id(sorted.get(1).getKey());
            result.setVideo2Score(sorted.get(1).getValue());
        }
        if (sorted.size() > 2) {
            result.setVideo3Id(sorted.get(2).getKey());
            result.setVideo3Score(sorted.get(2).getValue());
        }

        popularVideoRepository.save(result);

        log.info("Popular Video ETL finished.");
    }
}
