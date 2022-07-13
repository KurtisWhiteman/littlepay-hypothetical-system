package com.hypothetical.littlepay.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@Builder
public class TapRaw {
    private long id;
    private String createdAt;
    private String tapType;
    private String stopId;
    private String companyId;
    private String busId;
    private long pan;
}
