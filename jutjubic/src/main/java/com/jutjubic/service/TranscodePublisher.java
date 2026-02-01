package com.jutjubic.service;

import com.jutjubic.config.TranscodingProps;
import com.jutjubic.dto.TranscodeJobMessageDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class TranscodePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final TranscodingProps props;

    public TranscodePublisher(RabbitTemplate rabbitTemplate, TranscodingProps props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    public void publish(TranscodeJobMessageDto msg) {
        rabbitTemplate.convertAndSend(props.getExchange(), props.getRoutingKey(), msg);
    }
}
