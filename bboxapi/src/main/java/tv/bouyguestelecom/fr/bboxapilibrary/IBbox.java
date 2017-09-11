package tv.bouyguestelecom.fr.bboxapilibrary;

import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxApplication;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxDisplayToast;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetApplicationIcon;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetApplications;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetChannel;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetChannelListOnBox;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetChannels;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetCurrentChannel;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetEpg;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetEpgSimple;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetListEpg;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetOpenedChannels;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetRecommendationTv;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetSessionId;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetSpideoTv;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetVolume;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxMedia;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxMessage;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxRegisterApp;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxSearchEpgByExternalId;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxSearchEpgBySummary;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxSendMessage;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxSetVolume;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxStartApplication;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxStopApplication;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxSubscribe;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxUnsubscribe;
import tv.bouyguestelecom.fr.bboxapilibrary.model.ChannelProfil;
import tv.bouyguestelecom.fr.bboxapilibrary.model.EpgMode;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Moment;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Univers;

public interface IBbox {

    /*

    void getToken(IBboxGetToken iBboxGetToken);

    */

    void getSessionId(String ip, String appId, String appSecret, IBboxGetSessionId iBboxGetSessionId);

    void getChannel(String appId, String appSecret, int epgChannelNumber, IBboxGetChannel iBboxGetChannel);

    void getChannels(final String ip, String appId, String appSecret, final IBboxGetChannels iBboxGetChannels);

    void getChannels(String appId, String appSecret, ChannelProfil profil, IBboxGetChannels iBboxGetChannels);

    void getEpg(String appId, String appSecret, int epgChannelNumber, int period, String externalId, int limit, int page, EpgMode mode, IBboxGetListEpg iBboxGetListEpg);

    void getEpgByEventId(String appId, String appSecret, String eventId, EpgMode mode, IBboxGetEpg iBboxGetEpg);

    void getEpgSimple(String appId, String appSecret, String startTime, String endTime, EpgMode mode, IBboxGetEpgSimple iBboxGetEpgSimple);

   // void getTodayEpgFromBox(final String ip, String appId, String appSecret, final IBboxGetEpgSimple iBboxGetEpgFromBox);

    void getRecommendationsTV(String appId, String appSecret, String user, Univers[] universes, int limit, IBboxGetRecommendationTv iBboxGetRecommendationTv);
    void getRecoTv(String appId, String appSecret, String user, Moment moment, IBboxGetSpideoTv iBboxGetSpideoTv);

    void getApps(String ip, String appId, String appSecret, IBboxGetApplications iBboxGetApplications);

    void getCurrentChannel(String ip, String appId, String appSecret, IBboxGetCurrentChannel iBboxGetCurrentChannel);

    void registerApp(String ip, String appId, String appSecret, String appName, IBboxRegisterApp iBboxRegisterApp);

    void subscribeNotification(String ip, String appId, String appSecret, String appRegisterId, String ressourceId, IBboxSubscribe iBboxSubscribe);

    String addListener(String ip, String appId, IBboxMedia iBboxMedia);

    void removeMediaListener(String ip, String appId, String channelListenerId);

    void removeAppListener(String ip, String appId, String channelListenerId);

    void removeMsgListener(String ip, String appId, String channelListenerId);

    String addListener(String ip, String appId, IBboxApplication iBboxApplication);

    String addListener(String ip, String appId, IBboxMessage iBboxMessage);

    void startApp(String ip, String appId, String appSecret, String packageName, IBboxStartApplication iBboxStartApplication);

    void startApp(String ip, String appId, String appSecret, String packageName, String deeplink, IBboxStartApplication iBboxStartApplication);

    void stopApp(String ip, String appId, String appSecret, String packageName, IBboxStopApplication iBboxStopApplication);

    void displayToast(final String ip, String appId, String appSecret,
                      final String msg, final String color, final String duration,
                      final String pos_x, final String pos_y,
                      final IBboxDisplayToast iBboxDisplayToast);

    void setVolume(final String ip, String appId, String appSecret,
                   final String volume,
                   final IBboxSetVolume iBboxSetVolume);

    void getVolume(final String ip, String appId, String appSecret,
                   final IBboxGetVolume iBboxGetVolume);

    void getChannelListOnBox(final String ip, String appId, String appSecret,
                             final IBboxGetChannelListOnBox iBboxGetChannelListOnBox);

    void getAppInfo(String ip, String appId, String appSecret, String packageName, IBboxGetApplications iBboxGetApplications);

    void getAppIcon(String ip, String appId, String appSecret, String packageName, IBboxGetApplicationIcon iBboxGetApplicationIcon);

    void getOpenedChannels(String ip, String appId, String appSecret, IBboxGetOpenedChannels iBboxGetOpenedChannels);

    void unsubscribeNotification(String ip, String appId, String appSecret, String channelId, IBboxUnsubscribe iBboxUnsubscribe);

    void sendMessage(final String ip, final String appId, String appSecret,
                     final String channelIdOrRoomName, final String appIdFromRegister, final String msgToSend,
                     final IBboxSendMessage iBboxSendMessage);


    void SearchEpgBySummary(String ip, String appid, String appSecret, final String token, final String period, final String profil, final int typeEpg, final String epgChannelNumber, final String longSummaryfinal, IBboxSearchEpgBySummary iBboxSearchEpgBySummary);

    public void SearchEpgByExternalId(String ip, String appid, String appSecret, final String token, final String period, final String profil, final int typeEpg, final String epgChannelNumber, final String externalId, final IBboxSearchEpgByExternalId iBboxSearchEpgByExternalId);

    /*

    void getApps(String ip, String appId, String appSecret, IBboxGetApplications iBboxGetApplications);

    void getUrlLogoChannel(String ip, String appId, String appSecret, int channel, IBboxGetLogoChannel IBboxGetLogoChannel);

    void registerApp( String ip, String appId, String appSecret,  String appName,  IBboxRegisterApp iBboxRegisterApp);

    void subscribeNotifChannel( String ip,  String appId, String appSecret,  String registerApp,  IBboxSubscribe iBboxSubscribe);

    String addNotifChannelListener(IBboxMedia iChannelListener);

    void removeNotifChannelListener(String channelListenerId);

    void cancelRequest();

    */
}
