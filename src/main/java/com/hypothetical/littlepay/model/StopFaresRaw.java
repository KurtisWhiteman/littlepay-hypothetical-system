package com.hypothetical.littlepay.model;

import lombok.*;

@Data
@Builder
public class StopFaresRaw {
    private long id;
    private String fromStopId;
    private String toStopId;
    private double chargeAmount;
}
