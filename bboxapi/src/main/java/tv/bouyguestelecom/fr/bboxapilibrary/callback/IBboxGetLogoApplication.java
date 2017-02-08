package tv.bouyguestelecom.fr.bboxapilibrary.callback;


public interface IBboxGetLogoApplication {

    void onResponse(String sessionId, String url);

    void onFailure(Exception e);
}
