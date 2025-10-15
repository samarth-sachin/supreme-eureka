package com.dsl.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatelliteInfoDTO {
    private String name;
    private Integer noradId;
    private String type;
    private String country;
    private String orbitType;
    private Double altitudeKm;
    private String purpose;
    private String missionDescription;
    private List<String> funFacts;
    private String icon;
    private Double latitude;
    private Double longitude;
    private Double velocity;
}
