package fr.bouyguestelecom.tv.bboxapi.model;

public enum EpgMode {

    FULL("full"),
    SIMPLE("simple");

    private String value;

    EpgMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
