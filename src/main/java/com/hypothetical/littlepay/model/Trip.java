package com.hypothetical.littlepay.model;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
public class Trip {
    private String started;
    private String finished;
    private int duration;
    private String fromStopId;
    private String toStopId;
    private String chargeAmount;
    private String companyId;
    private String busId;
    private long pan;
    private TripStatus status;
}
