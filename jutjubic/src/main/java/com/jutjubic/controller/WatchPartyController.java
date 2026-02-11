package com.jutjubic.controller;

import com.jutjubic.dto.CreatePartyRequestDto;
import com.jutjubic.dto.PartyDto;
import com.jutjubic.dto.SetPartyVideoRequestDto;
import com.jutjubic.service.WatchPartyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parties")
public class WatchPartyController {

    private final WatchPartyService service;

    public WatchPartyController(WatchPartyService service) {
        this.service = service;
    }

    @PostMapping
    public PartyDto create(@RequestBody CreatePartyRequestDto req) {
        return service.create(req);
    }

    @GetMapping
    public List<PartyDto> listAll() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public PartyDto get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping("/{id}/join")
    public PartyDto join(@PathVariable String id) {
        return service.join(id);
    }

    @PostMapping("/{id}/start")
    public PartyDto start(@PathVariable String id, @RequestBody SetPartyVideoRequestDto req) {
        return service.startVideo(id, req.postId);
    }
}
