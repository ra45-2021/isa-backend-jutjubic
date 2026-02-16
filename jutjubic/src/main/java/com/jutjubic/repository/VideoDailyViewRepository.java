package com.jutjubic.repository;

import com.jutjubic.domain.VideoDailyView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VideoDailyViewRepository extends JpaRepository<VideoDailyView, Long> {

    Optional<VideoDailyView> findByVideoIdAndViewDate(Long videoId, LocalDate viewDate);

    List<VideoDailyView> findByViewDateBetween(LocalDate start, LocalDate end);
}
