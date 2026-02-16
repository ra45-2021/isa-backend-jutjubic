package com.jutjubic.controller;

import com.jutjubic.domain.PopularVideo;
import com.jutjubic.dto.PostViewDto;
import com.jutjubic.repository.PopularVideoRepository;
import com.jutjubic.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/popular")
@RequiredArgsConstructor
public class PopularVideoController {

    private final PopularVideoRepository popularVideoRepository;
    private final PostRepository postRepository;

    @GetMapping
    public List<PostViewDto> getPopularVideos() {

        return popularVideoRepository.findTopByOrderByPipelineRunAtDesc()
                .map(popular -> {

                    List<Long> ids = new ArrayList<>();

                    if (popular.getVideo1Id() != null) ids.add(popular.getVideo1Id());
                    if (popular.getVideo2Id() != null) ids.add(popular.getVideo2Id());
                    if (popular.getVideo3Id() != null) ids.add(popular.getVideo3Id());

                    if (ids.isEmpty()) return List.<PostViewDto>of();

                    return postRepository.findPopularPostsByIds(ids);
                })
                .orElse(List.of());
    }
}
