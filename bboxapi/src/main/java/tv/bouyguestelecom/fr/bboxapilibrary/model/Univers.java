package tv.bouyguestelecom.fr.bboxapilibrary.model;

public enum Univers {

    NOW("now"),
    TODAY("today"),
    TONIGHT("tonight"),
    WEEK("week");

    private String value;

    Univers(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
