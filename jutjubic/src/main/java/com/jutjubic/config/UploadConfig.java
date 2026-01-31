package com.jutjubic.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({UploadProperties.class, TranscodingProps.class})
public class UploadConfig {}
