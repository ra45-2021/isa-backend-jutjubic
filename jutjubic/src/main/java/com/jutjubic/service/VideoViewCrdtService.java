package com.jutjubic.service;

import com.jutjubic.domain.VideoViewCrdt;
import com.jutjubic.repository.VideoViewCrdtRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jutjubic.dto.CrdtSyncMessageDto;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class VideoViewCrdtService {

    private final VideoViewCrdtRepository repository;

    @Value("${replica.id}")
    private String replicaId;

    public VideoViewCrdtService(VideoViewCrdtRepository repository) {
        this.repository = repository;
    }


    @Transactional
    public void incrementViewForReplica(Long videoId) {
        VideoViewCrdt.VideoViewCrdtId id = new VideoViewCrdt.VideoViewCrdtId(videoId, replicaId);
        VideoViewCrdt entry = repository.findById(id).orElse(null);

        long currentLocalCount;

        if (entry == null) {
            VideoViewCrdt newEntry = new VideoViewCrdt(videoId, replicaId);
            newEntry.setViewCount(1L);
            newEntry.setLastUpdated(System.currentTimeMillis());
            repository.save(newEntry);
            currentLocalCount = 1L;
        } else {
            repository.incrementViewCount(videoId, replicaId, System.currentTimeMillis());
            VideoViewCrdt updated = repository.findById(id).orElse(entry);
            currentLocalCount = updated.getViewCount();
        }

        if (currentLocalCount <= 50) {
            // 1. Faza: Video je u usponu - BRZI SYNC na svakih 5 pregleda
            if (currentLocalCount % 5 == 0) {
                System.out.println("[CRDT] Fast-track sync (Small video) for ID: " + videoId);
                broadcastToOtherReplicas(videoId);
            }
        } else {
            // 2. Faza: Video je popularan - SPORIJI SYNC na svakih 100 pregleda
            if (currentLocalCount % 100 == 0) {
                System.out.println("[CRDT] Batch sync (Popular video) for ID: " + videoId);
                broadcastToOtherReplicas(videoId);
            }
        }
    }

    @Transactional(readOnly = true)
    public Long getTotalViewCount(Long videoId) {
        return repository.getTotalViewCount(videoId);
    }

    @Transactional(readOnly = true)
    public List<VideoViewCrdt> getAllCountersForVideo(Long videoId) {
        return repository.findAllByVideoId(videoId);
    }

    @Transactional
    public void mergeFromOtherReplica(Long videoId, String sourceReplicaId, Long receivedCount) {
        VideoViewCrdt.VideoViewCrdtId id = new VideoViewCrdt.VideoViewCrdtId(videoId, sourceReplicaId);

        VideoViewCrdt entry = repository.findById(id).orElse(null);

        if (entry == null) {
            VideoViewCrdt newEntry = new VideoViewCrdt(videoId, sourceReplicaId);
            newEntry.setViewCount(receivedCount);
            newEntry.setLastUpdated(System.currentTimeMillis());
            repository.save(newEntry);
        } else {
            if (receivedCount > entry.getViewCount()) {
                entry.setViewCount(receivedCount);
                entry.setLastUpdated(System.currentTimeMillis());
                repository.save(entry);
            }
        }

        System.out.println("[CRDT Sync] Merged replica " + sourceReplicaId +
                " for video " + videoId + ". New count: " + receivedCount);
    }

    @Value("${replicas.urls:}")
    private String[] otherReplicaUrls;

    private final RestTemplate restTemplate = new RestTemplate();

    public void broadcastToOtherReplicas(Long videoId) {
        VideoViewCrdt.VideoViewCrdtId id = new VideoViewCrdt.VideoViewCrdtId(videoId, replicaId);
        Long myLocalCount = repository.findById(id)
                .map(VideoViewCrdt::getViewCount)
                .orElse(0L);

        CrdtSyncMessageDto message = new CrdtSyncMessageDto();
        message.setSourceReplicaId(this.replicaId);
        message.setVideoId(videoId);
        message.setCount(myLocalCount);

        if (otherReplicaUrls != null) {
            for (String url : otherReplicaUrls) {
                if (url.isEmpty()) continue;
                try {
                    restTemplate.postForEntity(url + "/api/crdt/sync", message, Void.class);
                    System.out.println("[CRDT] Sent sync from " + replicaId + " to " + url + " for video " + videoId);
                } catch (Exception e) {
                    System.err.println("[CRDT] Failed sync to " + url + ": " + e.getMessage());
                }
            }
        }
    }

    @Transactional
    public void hardSyncAllReplicas(Long videoId) {
        if (otherReplicaUrls == null) return;

        for (String url : otherReplicaUrls) {
            if (url == null || url.isBlank()) continue;

            try {
                restTemplate.postForEntity(url + "/api/crdt/broadcast/" + videoId, null, Void.class);
                System.out.println("[CRDT HARD] Triggered broadcast on " + url + " for video " + videoId);
            } catch (Exception e) {
                System.err.println("[CRDT HARD] Failed to trigger broadcast on " + url + ": " + e.getMessage());
            }
        }

        broadcastToOtherReplicas(videoId);
    }


    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 120000) // 2 minuta
    public void periodicSync() {
        System.out.println("[CRDT] Starting periodic batch sync for replica: " + replicaId);

        List<VideoViewCrdt> myLocalCounters = repository.findAllByReplicaId(replicaId);

        if (myLocalCounters.isEmpty()) {
            System.out.println("[CRDT] No local data to sync.");
            return;
        }

        for (VideoViewCrdt counter : myLocalCounters) {
            broadcastToOtherReplicas(counter.getId().getVideoId());
        }

        System.out.println("[CRDT] Batch sync completed for " + myLocalCounters.size() + " videos.");
    }

    public String getReplicaId() {
        return replicaId;
    }
}
