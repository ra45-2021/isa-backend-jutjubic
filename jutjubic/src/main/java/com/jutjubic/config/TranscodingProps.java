package com.jutjubic.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "app.transcoding")
public class TranscodingProps {
    private String queue;
    private String exchange;
    private String routingKey;
    private String outputDir;
}
