package com.jutjubic.dto;

import java.time.Instant;
import java.util.List;

public class PartyDto {
    public String id;
    public String name;
    public String description;
    public Instant createdAt;
    public String authorUsername;
    public Long videoPostId;
    public List<String> watchers;
}
