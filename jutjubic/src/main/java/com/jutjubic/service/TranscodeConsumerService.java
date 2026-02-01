package com.jutjubic.service;

import com.jutjubic.config.TranscodingProps;
import com.jutjubic.domain.Post;
import com.jutjubic.domain.TranscodingJob;
import com.jutjubic.dto.TranscodeJobMessageDto;
import com.jutjubic.repository.PostRepository;
import com.jutjubic.repository.TranscodingJobRepository;
import com.rabbitmq.client.Channel;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

//za pokretanje komanda u terminalu:
//docker run -it --rm -p 5672:5672 -p 15672:15672 rabbitmq:3-management
@Service
public class TranscodeConsumerService {

    private final TranscodingJobRepository jobRepo;
    private final PostRepository postRepo;
    private final TranscodingProps props;
    private final FfmpegTranscodingService ffmpeg;

    public TranscodeConsumerService(
            TranscodingJobRepository jobRepo,
            PostRepository postRepo,
            TranscodingProps props,
            FfmpegTranscodingService ffmpeg
    ) {
        this.jobRepo = jobRepo;
        this.postRepo = postRepo;
        this.props = props;
        this.ffmpeg = ffmpeg;
    }

    @RabbitListener(queues = "${app.transcoding.queue}", concurrency = "2")
    public void handle(
            @Payload TranscodeJobMessageDto msg,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag
    ) throws Exception {

        try {
            if (!tryInsertJob(msg)) {
                System.out.println("DUPLICATE JOB SKIPPED: jobId=" + msg.jobId());
                channel.basicAck(tag, false);
                return;
            }

            System.out.println("CONSUMER START JOB: jobId=" + msg.jobId() + ", postId=" + msg.postId());
            setStatus(msg.jobId(), "PROCESSING", null);

            Path input = Paths.get(msg.inputAbsolutePath());
            if (!Files.exists(input)) throw new IllegalStateException("Input not found: " + input);

            Path outDir = input.getParent().resolve(props.getOutputDir());
            Files.createDirectories(outDir);

            String base = input.getFileName().toString();
            if (base.toLowerCase().endsWith(".mp4")) {
                base = base.substring(0, base.length() - 4);
            }
            String outName = base + "_720p.mp4";
            Path output = outDir.resolve(outName);

            ffmpeg.transcode720p(input, output);
            System.out.println("FFMPEG DONE: output=" + output);

            updatePostVideoUrl(msg.postId(), "/media/videos/" + props.getOutputDir() + "/" + outName);

            markDone(msg.jobId());
            channel.basicAck(tag, false);

        } catch (Exception e) {
            try {
                setStatus(msg.jobId(), "FAILED", e.getMessage());
            } catch (Exception ignored) {}

            System.err.println("TRANSCODE FAILED jobId=" + msg.jobId() + " err=" + e.getMessage());
            channel.basicNack(tag, false, false);
        }
    }

    @Transactional
    protected boolean tryInsertJob(TranscodeJobMessageDto msg) {
        try {
            TranscodingJob job = new TranscodingJob();
            job.setJobId(msg.jobId());
            job.setPostId(msg.postId());
            job.setStatus("RECEIVED");
            job.setCreatedAt(Instant.now());

            jobRepo.saveAndFlush(job);
            return true;

        } catch (Exception e) {
            return false;
        }
    }


    @Transactional
    protected void setStatus(String jobId, String status, String error) {
        TranscodingJob job = jobRepo.findByJobId(jobId).orElseThrow();
        job.setStatus(status);
        job.setError(error);
        jobRepo.save(job);
    }

    @Transactional
    protected void markDone(String jobId) {
        TranscodingJob job = jobRepo.findByJobId(jobId).orElseThrow();
        job.setStatus("DONE");
        job.setFinishedAt(Instant.now());
        jobRepo.save(job);
    }

    @Transactional
    protected void updatePostVideoUrl(Long postId, String newUrl) {
        Post p = postRepo.findById(postId).orElseThrow();
        p.setVideoUrl(newUrl);
        postRepo.save(p);
    }
}
