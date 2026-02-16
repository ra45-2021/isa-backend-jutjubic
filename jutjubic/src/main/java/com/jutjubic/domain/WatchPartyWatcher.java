package com.jutjubic.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "watch_party_watcher")
public class WatchPartyWatcher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private WatchParty party;

    @Column(nullable = false, length = 80)
    private String displayName;

    @Column(nullable = false)
    private Instant joinedAt;

    public Long getId() { return id; }

    public WatchParty getParty() { return party; }
    public void setParty(WatchParty party) { this.party = party; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }
}
