package com.jutjubic.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    private String dir;              // app.upload.dir
    private String videosDir;         // app.upload.videos-dir
    private String videosTranscodedDir;
    private String thumbsDir;         // app.upload.thumbs-dir
    private String thumbsCompressedDir; // app.upload.thumbs-compressed-dir
    private String tmpDir;            // app.upload.tmp-dir
    private long timeoutSeconds;      // app.upload.timeout-seconds

}
