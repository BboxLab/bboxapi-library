package fr.bouyguestelecom.tv.bboxapi;

import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetChannel;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetChannels;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetEpg;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetListEpg;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetRecommendationTv;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetSpideoTv;
import fr.bouyguestelecom.tv.bboxapi.model.ChannelProfil;
import fr.bouyguestelecom.tv.bboxapi.model.EpgMode;
import fr.bouyguestelecom.tv.bboxapi.model.Moment;
import fr.bouyguestelecom.tv.bboxapi.model.Univers;

/**
 * Created by tao on 30/11/17.
 */

public interface IBboxCloud {
    void getChannel(int epgChannelNumber, IBboxGetChannel iBboxGetChannel);
    void getChannels(ChannelProfil profil, IBboxGetChannels iBboxGetChannels);
    void getRecoTv(String user, Moment moment, IBboxGetSpideoTv iBboxGetSpideoTv);
    void getRecommendationsTV(String user, Univers[] universes, int limit, IBboxGetRecommendationTv iBboxGetRecommendationTv);
    void getEpg(int epgChannelNumber, int period, String externalId, int limit, int page, EpgMode mode, IBboxGetListEpg iBboxGetListEpg);
    void getEpgByEventId(final String eventId, final EpgMode mode, final IBboxGetEpg iBboxGetEpg);
}
