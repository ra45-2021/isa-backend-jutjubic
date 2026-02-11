package com.jutjubic.service;

import com.jutjubic.domain.User;
import com.jutjubic.domain.WatchParty;
import com.jutjubic.domain.WatchPartyWatcher;
import com.jutjubic.dto.CreatePartyRequestDto;
import com.jutjubic.dto.PartyDto;
import com.jutjubic.repository.PostRepository;
import com.jutjubic.repository.WatchPartyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class WatchPartyService {

    private final WatchPartyRepository partyRepo;
    private final PostRepository postRepo;
    private final CurrentUserService currentUser;
    private final SimpMessagingTemplate ws;

    public WatchPartyService(
            WatchPartyRepository partyRepo,
            PostRepository postRepo,
            CurrentUserService currentUser,
            SimpMessagingTemplate ws
    ) {
        this.partyRepo = partyRepo;
        this.postRepo = postRepo;
        this.currentUser = currentUser;
        this.ws = ws;
    }

    @Transactional
    public PartyDto create(CreatePartyRequestDto req) {
        if (req == null || req.name == null || req.name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Party name is required");
        }

        User u = currentUser.require();
        String authorUsername = u.getUsername();

        WatchParty p = new WatchParty();
        p.setId(UUID.randomUUID().toString());
        p.setName(req.name.trim());
        p.setDescription(req.description == null ? "" : req.description.trim());
        p.setCreatedAt(Instant.now());
        p.setAuthorUsername(authorUsername);
        p.setVideoPostId(null);

        addWatcher(p, authorUsername);

        partyRepo.save(p);

        ws.convertAndSend(topic(p.getId()), new WsEvent("WATCHERS", null, toDto(p).watchers));

        return toDto(p);
    }

    @Transactional(readOnly = true)
    public List<PartyDto> listAll() {
        return partyRepo.findAll().stream()
                .sorted(Comparator.comparing(WatchParty::getCreatedAt).reversed())
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PartyDto get(String id) {
        WatchParty p = partyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Party not found"));
        return toDto(p);
    }

    @Transactional
    public PartyDto join(String id) {
        WatchParty p = partyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Party not found"));

        User u = currentUser.optional();
        String display = (u == null) ? "Guest" : u.getUsername();

        addWatcher(p, display);

        partyRepo.save(p);

        ws.convertAndSend(topic(id), new WsEvent("WATCHERS", null, toDto(p).watchers));

        return toDto(p);
    }

    @Transactional
    public PartyDto startVideo(String partyId, Long postId) {
        if (postId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "postId required");
        }

        WatchParty p = partyRepo.findById(partyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Party not found"));

        User u = currentUser.require();
        String username = u.getUsername();

        if (!username.equals(p.getAuthorUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only author can start the video");
        }

        if (!postRepo.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found");
        }

        p.setVideoPostId(postId);
        partyRepo.save(p);

        ws.convertAndSend(topic(partyId), new WsEvent("PLAY", postId, null));

        return toDto(p);
    }

    private void addWatcher(WatchParty p, String displayName) {
        WatchPartyWatcher w = new WatchPartyWatcher();
        w.setParty(p);
        w.setDisplayName(displayName);
        w.setJoinedAt(Instant.now());
        p.getWatchers().add(w);
    }

    private PartyDto toDto(WatchParty p) {
        PartyDto dto = new PartyDto();
        dto.id = p.getId();
        dto.name = p.getName();
        dto.description = p.getDescription();
        dto.createdAt = p.getCreatedAt();
        dto.authorUsername = p.getAuthorUsername();
        dto.videoPostId = p.getVideoPostId();

        dto.watchers = p.getWatchers().stream()
                .sorted(Comparator.comparing(WatchPartyWatcher::getJoinedAt))
                .map(w -> w.getDisplayName() + " has joined!")
                .toList();

        return dto;
    }

    private String topic(String partyId) {
        return "/topic/party/" + partyId;
    }

    public static class WsEvent {
        public String type;
        public Long postId;
        public List<String> watchers;

        public WsEvent() {}

        public WsEvent(String type, Long postId, List<String> watchers) {
            this.type = type;
            this.postId = postId;
            this.watchers = watchers;
        }
    }
}
