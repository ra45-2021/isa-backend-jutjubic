package com.jutjubic.controller;

import com.jutjubic.dto.CrdtSyncMessageDto;
import com.jutjubic.service.VideoViewCrdtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crdt")
public class CrdtSyncController {

    private final VideoViewCrdtService crdtService;

    public CrdtSyncController(VideoViewCrdtService crdtService) {
        this.crdtService = crdtService;
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> receiveSync(@RequestBody CrdtSyncMessageDto message) {
        crdtService.mergeFromOtherReplica(
                message.getVideoId(),
                message.getSourceReplicaId(),
                message.getCount()
        );
        return ResponseEntity.ok().build();
    }
}