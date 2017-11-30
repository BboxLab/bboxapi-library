package fr.bouyguestelecom.tv.bboxapi.model;

public enum Moment {

    NOW("now"),
    TONIGHT("tonight"),
    WEEK("week");

    private String value;

    Moment(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
