package org.recolnat.collection.manager.api.domain.enums;

public enum ParamBodyMediathequeEnum {
    STREAMFILE("streamfile"),
    BATCH("batch"),
    INSTITUTIONCODE("institutionCode"),
    COLLECTIONCODE("collectionCode"),
    CATALOGNUMBER("catalogNumber"),
    RANK("rank"),
    AUTHORS("authors"),
    YEAR("year"),
    CREDIT("credit"),
    RIGHTS("rights"),
    PROJECT("project"),
    PUBLISH("publish"),
    OWNER("owner");

    private final String paramBody;

    ParamBodyMediathequeEnum(String paramBody) {
        this.paramBody = paramBody;
    }

    public String getParamBody() {
        return this.paramBody;
    }
}
