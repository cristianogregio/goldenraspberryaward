package com.goldenraspberryawards.api;

import com.goldenraspberryawards.api.dto.ProducerIntervalResponse;
import com.goldenraspberryawards.service.ProducerIntervalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/producers")
public class ProducerIntervalController
{

    private final ProducerIntervalService producerIntervalService;

    public ProducerIntervalController(ProducerIntervalService producerIntervalService)
    {
        this.producerIntervalService = producerIntervalService;
    }

    @GetMapping("/intervals")
    public ResponseEntity<ProducerIntervalResponse> getProducerIntervals()
    {
        ProducerIntervalResponse response = producerIntervalService.getProducerIntervals();
        return ResponseEntity.ok(response);
    }
}
