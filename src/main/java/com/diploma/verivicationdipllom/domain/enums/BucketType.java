package com.diploma.verivicationdipllom.domain.enums;

public enum BucketType {
    DOCUMENT_PATH("/doc/"),
    LIVENESS_PATH("/liveness/"),
    VERIFICATION_USER("verification-user-media");


    private final String value;

    BucketType(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }

    public static BucketType fromString(String text) {
        for (BucketType e : BucketType.values()) {
            if (e.value.equalsIgnoreCase(text)) {
                return e;
            }
        }
        throw new IllegalArgumentException("No enum constant with text " + text);
    }
}
