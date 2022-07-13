package com.hypothetical.littlepay.model;

public enum TapType {
    ON("ON"),
    OFF("OFF");

    private final String displayName;

    TapType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get the TapType based on the display name
     *
     * @param displayName
     * @return TapType, default of ON
     */
    public static TapType getTapTypeFromDisplayName(String displayName) {
        for (TapType tapType : values()) {
            if (tapType.displayName.equals(displayName)) {
                return tapType;
            }
        }
        return TapType.ON;
    }
}
