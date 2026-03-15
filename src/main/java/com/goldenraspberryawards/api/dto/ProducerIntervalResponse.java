package com.goldenraspberryawards.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProducerIntervalResponse(List<ProducerIntervalRecord> min, List<ProducerIntervalRecord> max)
{
}
