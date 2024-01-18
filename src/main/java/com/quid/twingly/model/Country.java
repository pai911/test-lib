package com.quid.twingly.model;

public enum Country {
    FRANCE("fr"),
    GERMANY("de"),
    SPAIN("es"),
    ITALY("it"),
    UNITED_ARAB_EMIRATES("ae"),
    SAUDI_ARABIA("sa"),
    EGYPT("eg"),
    BRAZIL("br"),
    JAPAN("jp"),
    RUSSIA("ru");

    private final String code;

    Country(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}