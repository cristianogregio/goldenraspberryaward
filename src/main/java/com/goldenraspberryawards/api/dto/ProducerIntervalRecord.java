package com.goldenraspberryawards.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProducerIntervalRecord(
        String producer,
        int interval,
        @JsonProperty("previousWin")
        int previousWin,
        @JsonProperty("followingWin")
        int followingWin
)
{
}
