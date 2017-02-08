package tv.bouyguestelecom.fr.bboxapilibrary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
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
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetSessionId;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetSpideoTv;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetToken;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetVolume;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxMedia;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxMessage;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxRegisterApp;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxSendMessage;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxSetVolume;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxStartApplication;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxStopApplication;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxSubscribe;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxUnsubscribe;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Application;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Channel;
import tv.bouyguestelecom.fr.bboxapilibrary.model.ChannelLight;
import tv.bouyguestelecom.fr.bboxapilibrary.model.ChannelProfil;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Epg;
import tv.bouyguestelecom.fr.bboxapilibrary.model.EpgMode;
import tv.bouyguestelecom.fr.bboxapilibrary.model.EpgSimple;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Moment;
import tv.bouyguestelecom.fr.bboxapilibrary.ws.WebSocket;


public class Bbox implements IBbox {
    private static final String TAG = Bbox.class.getSimpleName();

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String URL_API_TOKEN = "https://api.bbox.fr/v1.1/security/token";
    private static final String URL_GET_RECO_TV = "https://api.bbox.fr/v1.1/media/live/recommendations";
    private static final String URL_GET_EPG = "https://api.bbox.fr/v1.1/media/live";
    private static final String URL_GET_EPG_SIMPLE = "https://api.bbox.fr/v1.1/media/live/epg";
    private static final String URL_GET_CHANNELS = "https://api.bbox.fr/v1.1/media/channels";

    private static final String URL_API_BOX = "http://@IP:8080/api.bbox.lan/v0";
    private static final String URL_SESSION_ID = URL_API_BOX + "/security/sessionId";
    private static final String URL_GET_APPLICATIONS = URL_API_BOX + "/applications";
    private static final String URL_GET_CURRENT_CHANNEL = URL_API_BOX + "/media";
    private static final String URL_GET_CHANNEL_LIST = URL_API_BOX + "/media/tvchannellist";
    private static final String URL_REGISTER_APP = URL_API_BOX + "/applications/register";
    private static final String URL_NOTIFICATION = URL_API_BOX + "/notification";

    private static final String URL_START_APP = URL_API_BOX + "/applications";

    private static final String URL_DISPLAY_TOAST = URL_API_BOX + "/userinterface/toast";
    private static final String URL_VOLUME = URL_API_BOX + "/userinterface/volume";

    private static Bbox instance;

    private OkHttpClient mClient = new OkHttpClient();

    private WebSocket mWebSocket;

    private String mSessionId;
    private String mToken;
    private Long mValidityToken = (long) -1;
    private Long mValiditySessionId = (long) -1;
    private boolean hasSecurity = true;


    private Bbox() {
        mClient.newBuilder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).connectionPool(new ConnectionPool(1, 5, TimeUnit.MINUTES));

    }

    public synchronized static Bbox getInstance() {
        if (instance == null)
            instance = new Bbox();

        return instance;
    }

    public Call getToken(String appId, String appSecret, final IBboxGetToken iBboxGetToken) {
        if (mValidityToken == -1 || mValidityToken <= System.currentTimeMillis()) {
            RequestBody body = RequestBody.create(JSON, buildJsonRequestToken(appId, appSecret));
            Request request = new Request.Builder()
                    .url(URL_API_TOKEN)
                    .post(body)
                    .build();

            Call call = mClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Get token failed : " + e.getMessage());
                    iBboxGetToken.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_NO_CONTENT
                            && response.headers().get("x-token") != null
                            && !response.headers().get("x-token").isEmpty()) {
                        String tokenCloud = response.headers().get("x-token");
                        iBboxGetToken.onResponse(tokenCloud);
                        mValidityToken = System.currentTimeMillis() + 24 * 60 * 60 * 1000;
                        mToken = tokenCloud;
                        Log.d(TAG, "token = " + mToken);
                    } else {
                        iBboxGetToken.onFailure(call.request(), response.code());
                        Log.e(TAG, "Get token failed");
                    }

                    response.body().close();
                }
            });

            return call;
        } else {
            iBboxGetToken.onResponse(mToken);
            return null;
        }

    }

    public void getSessionId(final String ip, String appId, String appSecret, final IBboxGetSessionId iBboxGetSessionId) {
        if (hasSecurity) {
            getToken(appId, appSecret, new IBboxGetToken() {
                @Override
                public void onResponse(String token) {
                    Long currentTimeMillis = System.currentTimeMillis();

                    if (mValiditySessionId == -1 || mValiditySessionId <= currentTimeMillis) {
                        try {
                            RequestBody body = RequestBody.create(JSON, new JSONObject().put("token", token).toString());
                            Request request = new Request.Builder()
                                    .url(URL_SESSION_ID.replace("@IP", ip))
                                    .post(body)
                                    .build();
                            Log.d(TAG, "url =  : " + URL_SESSION_ID.replace("@IP", ip));
                            Call call = mClient.newCall(request);
                            call.enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Log.e(TAG, "Get sessionId failed : " + e.getMessage());
                                    iBboxGetSessionId.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    if (response.code() == HttpURLConnection.HTTP_NO_CONTENT
                                            && response.headers().get("x-sessionid") != null
                                            && !response.headers().get("x-sessionid").isEmpty()) {
                                        mSessionId = response.headers().get("x-sessionid");
                                        mValiditySessionId = System.currentTimeMillis() + 60 * 1000;
                                        iBboxGetSessionId.onResponse(mSessionId);
                                    } else {
                                        mSessionId = null;
                                        mValiditySessionId = (long) -1;
                                        iBboxGetSessionId.onFailure(call.request(), response.code());
                                    }

                                    response.body().close();
                                }
                            });
                        } catch (JSONException e) {
                            Log.e(TAG, "Error occured", e);
                        }

                    } else {
                        iBboxGetSessionId.onResponse(mSessionId);
                    }
                }

                @Override
                public void onFailure(Request request, int errorCode) {
                    mSessionId = null;
                    mValiditySessionId = (long) -1;
                    iBboxGetSessionId.onFailure(request, errorCode);
                }
            });
        } else {
            mSessionId = "855e4aa2f043550045c298e589220eca46769be82294f2bdda8345bffc160754";
            mValiditySessionId = System.currentTimeMillis() + 60 * 1000;
            iBboxGetSessionId.onResponse(mSessionId);
        }
    }

    @Override
    public void getChannel(String appId, String appSecret, final int epgChannelNumber, final IBboxGetChannel iBboxGetChannel) {
        getToken(appId, appSecret, new IBboxGetToken() {
            @Override
            public void onResponse(String token) {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(URL_GET_CHANNELS).newBuilder();
                urlBuilder.addPathSegment("" + epgChannelNumber);

                Request request = new Request.Builder()
                        .url(urlBuilder.build().toString())
                        .addHeader("x-token", token)
                        .build();

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetChannel.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() != HttpURLConnection.HTTP_OK)
                            iBboxGetChannel.onFailure(response.request(), response.code());

                        else
                            iBboxGetChannel.onResponse(parseJsonChannel(response.body().byteStream()));

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetChannel.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getChannels(String appId, String appSecret, final ChannelProfil profil, final IBboxGetChannels iBboxGetChannels) {
        getToken(appId, appSecret, new IBboxGetToken() {
            @Override
            public void onResponse(String token) {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(URL_GET_CHANNELS).newBuilder();
                urlBuilder.addQueryParameter("profil", profil.getValue());

                Request request = new Request.Builder()
                        .url(urlBuilder.build().toString())
                        .header("x-token", token)
                        .build();

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetChannels.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        Log.i(TAG, "Get channels success");
                        if (response.code() == HttpURLConnection.HTTP_OK)
                            iBboxGetChannels.onResponse(parseJsonChannels(response));

                        else
                            iBboxGetChannels.onFailure(response.request(), response.code());

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetChannels.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getEpg(String appId, String appSecret, final int epgChannelNumber, final int period, final String externalId, final int limit,
                       final int page, final EpgMode mode, final IBboxGetListEpg iBboxGetListEpg) {
        getToken(appId, appSecret, new IBboxGetToken() {
            @Override
            public void onResponse(String token) {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(URL_GET_EPG).newBuilder();
                urlBuilder.addQueryParameter("period", "" + period);
                urlBuilder.addQueryParameter("epgChannelNumber", "" + epgChannelNumber);
                urlBuilder.addQueryParameter("externalId", "" + externalId);
                urlBuilder.addQueryParameter("limit", "" + limit);
                urlBuilder.addQueryParameter("page", "" + page);
                urlBuilder.addQueryParameter("mode", "" + mode.getValue());

                Request request = new Request.Builder()
                        .url(urlBuilder.build().toString())
                        .addHeader("x-token", token)
                        .addHeader("", "")
                        .build();

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        iBboxGetListEpg.onFailure(call.request(), HttpURLConnection.HTTP_INTERNAL_ERROR);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK)
                            iBboxGetListEpg.onResponse(parseJsonListEpg(response));

                        else
                            iBboxGetListEpg.onFailure(call.request(), response.code());

                        response.body().close();
                    }
                });

            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetListEpg.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getEpgByEventId(String appId, String appSecret, final String eventId, final EpgMode mode, final IBboxGetEpg iBboxGetEpg) {
        getToken(appId, appSecret, new IBboxGetToken() {
            @Override
            public void onResponse(String token) {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(URL_GET_EPG).newBuilder();
                urlBuilder.addPathSegment(eventId);
                urlBuilder.addQueryParameter("mode", mode.getValue());

                Request request = new Request.Builder()
                        .url(urlBuilder.build().toString())
                        .addHeader("x-token", token)
                        .build();

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        iBboxGetEpg.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK)
                            iBboxGetEpg.onResponse(parseJsonEpg(response));

                        else
                            iBboxGetEpg.onFailure(call.request(), response.code());

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetEpg.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getEpgSimple(String appId, String appSecret, final String startTime, final String endTime, final EpgMode mode, final IBboxGetEpgSimple iBboxGetEpgSimple) {
        getToken(appId, appSecret, new IBboxGetToken() {
            @Override
            public void onResponse(String token) {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(URL_GET_EPG_SIMPLE).newBuilder();
                urlBuilder.addQueryParameter("mode", mode.getValue());
                urlBuilder.addQueryParameter("startTime", startTime);
                urlBuilder.addQueryParameter("endTime", endTime);

                Request request = new Request.Builder()
                        .url(urlBuilder.build().toString())
                        .addHeader("x-token", token)
                        .build();

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        iBboxGetEpgSimple.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK)
                            iBboxGetEpgSimple.onResponse(parseJsonEpgSimple(response));

                        else
                            iBboxGetEpgSimple.onFailure(call.request(), response.code());

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetEpgSimple.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getRecoTv(final String appId, final String appSecret, final String user, final Moment moment, final IBboxGetSpideoTv iBboxGetSpideoTv) {
        getToken(appId, appSecret, new IBboxGetToken() {
            @Override
            public void onResponse(String token) {
                try {
                    RequestBody body = RequestBody.create(JSON, new JSONObject().put("user", user).toString());
                    HttpUrl.Builder urlBuilder = HttpUrl.parse(URL_GET_RECO_TV).newBuilder();
                    urlBuilder.addPathSegment(moment.getValue());

                    Request request = new Request.Builder()
                            .url(urlBuilder.build().toString())
                            .post(body)
                            .addHeader("x-token", token)
                            .build();

                    Call call = mClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            iBboxGetSpideoTv.onFailure(call.request(), HttpURLConnection.HTTP_INTERNAL_ERROR);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.code() == HttpURLConnection.HTTP_OK)
                                iBboxGetSpideoTv.onResponse(parseJsonListEpg(response));

                            else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                                iBboxGetSpideoTv.onResponse(new ArrayList<Epg>());
                            } else
                                iBboxGetSpideoTv.onFailure(call.request(), response.code());

                            response.body().close();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetSpideoTv.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getApps(final String ip, String appId, String appSecret, final IBboxGetApplications iBboxGetApplications) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            public void onResponse(final String sessionId) {
                Request.Builder requestBuilder = new Request.Builder()
                        .url(URL_GET_APPLICATIONS.replace("@IP", ip));
                if (sessionId!=null) {
                    requestBuilder.header("x-sessionid", sessionId);
                }
                final Request request = requestBuilder.build();
                Call mCall = mClient.newCall(request);
                mCall.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetApplications.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK)
                            iBboxGetApplications.onResponse(parseJsonApplications(URL_API_BOX.replace("@IP", ip), response));

                        else
                            iBboxGetApplications.onFailure(call.request(), response.code());

                        response.body().close();
                    }
                });
            }

            public void onFailure(Request request, int errorCode) {
               // iBboxGetApplications.onFailure(request, errorCode);
                onResponse(null);
            }
        });
    }

    @Override
    public void getCurrentChannel(final String ip, String appId, String appSecret, final IBboxGetCurrentChannel iBboxGetCurrentChannel) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            public void onResponse(final String sessionId) {
                Log.v(TAG, "SessionId ==> " + sessionId);
                final Request request = new Request.Builder()
                        .url(URL_GET_CURRENT_CHANNEL.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .build();

                Call mCall = mClient.newCall(request);
                mCall.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetCurrentChannel.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK)
                            iBboxGetCurrentChannel.onResponse(parseJsonChannel(response.body().byteStream()));

                        else
                            iBboxGetCurrentChannel.onFailure(call.request(), response.code());

                        response.body().close();
                    }
                });
            }

            public void onFailure(Request request, int errorCode) {
                iBboxGetCurrentChannel.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void registerApp(final String ip, String appId, String appSecret, final String appName, final IBboxRegisterApp iBboxRegisterApp) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                try {
                    RequestBody body = RequestBody.create(JSON, new JSONObject().put("appName", appName).toString());
                    final Request request = new Request.Builder()
                            .url(URL_REGISTER_APP.replace("@IP", ip))
                            .header("x-sessionid", sessionId)
                            .post(body)
                            .build();

                    Call call = mClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.i(TAG, "Register app failed");
                            iBboxRegisterApp.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.code() == HttpURLConnection.HTTP_NO_CONTENT
                                    && response.headers().get("Location") != null
                                    && !response.headers().get("Location").isEmpty()) {
                                Log.i(TAG, "Register app success");
                                String location = response.headers().get("Location");
                                String appId = location.substring(location.lastIndexOf('/') + 1);
                                iBboxRegisterApp.onResponse(appId);
                            } else {
                                Log.i(TAG, "Register app failed");
                                iBboxRegisterApp.onFailure(call.request(), response.code());
                            }

                            response.body().close();
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "Error occured", e);
                }
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxRegisterApp.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void subscribeNotification(final String ip, String appId, String appSecret, final String appRegisterId, final String ressourceId,
                                      final IBboxSubscribe iBboxSubscribe) {
        //notification_body='{"appId":"'"${appId}"'","resources" : [ {"resourceId":"Iot"} ] }'
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                RequestBody body = RequestBody.create(JSON, buildJsonRequestNotif(appRegisterId, ressourceId));
                final Request request = new Request.Builder()
                        .url(URL_NOTIFICATION.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "Subscribe Notification failed");
                        iBboxSubscribe.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            Log.i(TAG, "Subscribe Notification success");
                            iBboxSubscribe.onSubscribe();
                        } else {
                            Log.i(TAG, "Subscribe Notification failed");
                            iBboxSubscribe.onFailure(call.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxSubscribe.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public String addListener(String ip, final String appId, IBboxMedia iBboxMedia) {
        if (mWebSocket == null) {
            mWebSocket = new WebSocket(ip, appId);
        }

        return mWebSocket.addNotifChannelListener(iBboxMedia);
    }

    @Override
    public String addListener(String ip, String appId, IBboxApplication iBboxApplication) {
        if (mWebSocket == null) {
            mWebSocket = new WebSocket(ip, appId);
        }

        return mWebSocket.addNotifApplication(iBboxApplication);
    }

    @Override
    public String addListener(String ip, String appId, IBboxMessage iBboxMessage) {
        if (mWebSocket == null) {
            mWebSocket = new WebSocket(ip, appId);
        }

        return mWebSocket.addNotifMessage(iBboxMessage);
    }

    @Override
    public void removeMediaListener(String ip, String appId, String channelListenerId) {
        WebSocket mWebSocketBis = new WebSocket(ip, appId);

        if (mWebSocketBis.getNotifMedia() != null)
        {
            mWebSocketBis.removeNotifChannelListener(channelListenerId);

            if (mWebSocketBis.getNotifMedia().size() == 0)
                mWebSocketBis.close();
        }
    }

    @Override
    public void removeAppListener(String ip, String appId, String channelListenerId) {
        WebSocket mWebSocketBis = new WebSocket(ip, appId);

        if (mWebSocketBis.getNotifApps() != null)
        {
            mWebSocketBis.removeNotifApps(channelListenerId);

            if (mWebSocketBis.getNotifApps().size() == 0)
                mWebSocketBis.close();
        }
    }

    @Override
    public void removeMsgListener(String ip, String appId, String channelListenerId) {
        WebSocket mWebSocketBis = new WebSocket(ip, appId);

        if (mWebSocketBis.getNotifMsg() != null)
        {
            mWebSocketBis.removeNotifMsg(channelListenerId);

            if (mWebSocketBis.getNotifMsg().size() == 0)
                mWebSocketBis.close();
        }
    }

    private List<Channel> parseJsonChannels(Response response) {
        List<Channel> channels = new ArrayList<>();

        if (response.code() == HttpURLConnection.HTTP_OK) {
            try {
                JsonReader reader = new JsonReader(new InputStreamReader(response.body().byteStream(), "UTF-8"));

                reader.beginArray();

                while (reader.hasNext()) {
                    Channel channel = new Channel(reader);
                    channels.add(channel);
                }

                reader.endArray();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return channels;
    }


    private List<Epg> parseJsonListEpg(Response response) throws SocketException {
        List<Epg> epgs = new ArrayList<>();

        try {
            JsonReader reader = new JsonReader(new InputStreamReader(response.body().byteStream(), "UTF-8"));

            reader.beginArray();

            while (reader.hasNext()) {
                Epg epg = new Epg(reader);
                epgs.add(epg);
            }

            reader.endArray();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return epgs;
    }

    private List<EpgSimple> parseJsonEpgSimple(Response response) throws SocketException {
        List<EpgSimple> epgs = new ArrayList<>();

        try {
            JsonReader reader = new JsonReader(new InputStreamReader(response.body().byteStream(), "UTF-8"));
            reader.beginArray();

            while (reader.hasNext()) {
                EpgSimple epg = new EpgSimple(reader);
                epgs.add(epg);
            }

            reader.endArray();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return epgs;
    }

    private Epg parseJsonEpg(Response response) throws SocketException {
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(response.body().byteStream(), "UTF-8"));
            Epg epg = new Epg(reader);
            reader.close();
            return epg;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private List<Application> parseJsonApplications(String ip, Response response) {
        List<Application> applications = new ArrayList<>();

        try {
            JsonReader reader = new JsonReader(new InputStreamReader(response.body().byteStream(), "UTF-8"));
            reader.beginArray();

            while (reader.hasNext()) {
                Application application = new Application(ip, reader);

                if (!application.getAppName().equals("Lanceur Leanback")
                        && !application.getAppName().equals("SmartUI")
                        && !application.getAppName().equals("Services Google Play"))
                    applications.add(application);
            }

            reader.endArray();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(applications);

        return applications;
    }

    private Channel parseJsonChannel(InputStream inputStream) {
        Channel channel = null;

        try {
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            channel = new Channel(reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return channel;
    }

    private String buildJsonRequestToken(String appId, String appSecret) {
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("appId", appId);
            jObject.put("appSecret", appSecret);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObject.toString();
    }


    private String buildJsonRequestNotif(String appResgisterId, String ressourceId) {
        JSONObject jObject = new JSONObject();
        JSONArray ressouces = new JSONArray();
        JSONObject resourceChannel = new JSONObject();

        try {
            resourceChannel.put("resourceId", ressourceId);
            ressouces.put(resourceChannel);
            jObject.put("appId", appResgisterId);
            jObject.put("resources", ressouces);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObject.toString();
    }

    @Override
    public void startApp(final String ip, String appId, String appSecret,
                         final String packageName,
                         final IBboxStartApplication iBboxStartApplication) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_START_APP +"/" + packageName;
                RequestBody body = RequestBody.create(JSON, "");
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();
                Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "Start app failed before response");
                        iBboxStartApplication.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            Log.i(TAG, "Start app success");
                            iBboxStartApplication.onResponse();
                        } else {
                            Log.i(TAG, "Start app failed "+response.code());
                            iBboxStartApplication.onFailure(call.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxStartApplication.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void startApp(final String ip, String appId, String appSecret,
                         final String packageName, final String deeplink,
                         final IBboxStartApplication iBboxStartApplication) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_START_APP +"/" + packageName;
                RequestBody body = RequestBody.create(JSON, buildJsonRequestDeeplink(deeplink));
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();
                Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "Start app failed before response");
                        iBboxStartApplication.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            Log.i(TAG, "Start app success");
                            iBboxStartApplication.onResponse();
                        } else {
                            Log.i(TAG, "Start app failed "+response.code());
                            iBboxStartApplication.onFailure(call.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxStartApplication.onFailure(request, errorCode);
            }
        });
    }

    private String buildJsonRequestDeeplink(String deeplink) {
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("intent", deeplink);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObject.toString();
    }

    private String buildJsonRequestToast(String msg, String color, String duration, String x, String y) {
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("message", msg);
            jObject.put("color", color);
            jObject.put("duration", duration);
            jObject.put("pos_x", x);
            jObject.put("pos_y", y);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObject.toString();
    }

    @Override
    public void displayToast(final String ip, String appId, String appSecret,
                             final String msg, final String color, final String duration,
                             final String pos_x, final String pos_y,
                             final IBboxDisplayToast iBboxDisplayToast) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_DISPLAY_TOAST;
                RequestBody body = RequestBody.create(JSON, buildJsonRequestToast(msg, color, duration, pos_x, pos_y));
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();
                Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "Display toast failed");
                        iBboxDisplayToast.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            Log.i(TAG, "Display toast success");
                            iBboxDisplayToast.onResponse();
                        } else {
                            Log.i(TAG, "Display toast failed");
                            iBboxDisplayToast.onFailure(call.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxDisplayToast.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void stopApp(final String ip, String appId, String appSecret,
                        final String packageName,
                        final IBboxStopApplication iBboxStopApplication) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_START_APP + "/" + packageName;
                RequestBody body = RequestBody.create(JSON, "");
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .delete(body)
                        .build();
                Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "Stop app failed before response");
                        iBboxStopApplication.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            Log.i(TAG, "Stop app success");
                            iBboxStopApplication.onResponse();
                        } else {
                            Log.i(TAG, "Stop app failed");
                            iBboxStopApplication.onFailure(call.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxStopApplication.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void setVolume(final String ip, String appId, String appSecret,
                          final String volume,
                          final IBboxSetVolume iBboxSetVolume) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_VOLUME;
                RequestBody body = RequestBody.create(JSON, buildJsonRequestVolume(volume));
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();
                Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "Set volume failed");
                        iBboxSetVolume.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            Log.i(TAG, "Set volume success");
                            iBboxSetVolume.onResponse();
                        } else {
                            Log.i(TAG, "Set volume failed");
                            iBboxSetVolume.onFailure(call.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxSetVolume.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getVolume(final String ip, String appId, String appSecret,
                          final IBboxGetVolume iBboxGetVolume) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_VOLUME;
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .build();
                Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "Get volume failed");
                        iBboxGetVolume.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK) {
                            Log.i(TAG, "Get volume success");
                            String volume = response.body().string();
                            iBboxGetVolume.onResponse(volume);
                        } else {
                            Log.i(TAG, "Get volume failed");
                            iBboxGetVolume.onFailure(call.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetVolume.onFailure(request, errorCode);
            }
        });
    }


    private String buildJsonRequestVolume(String volume) {
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("volume", volume);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObject.toString();
    }


    @Override
    public void getChannelListOnBox(final String ip, String appId, String appSecret,
                                    final IBboxGetChannelListOnBox iBboxGetChannelListOnBox) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {

                String url = URL_GET_CHANNEL_LIST;
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .build();

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetChannelListOnBox.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() != HttpURLConnection.HTTP_OK)
                            iBboxGetChannelListOnBox.onFailure(response.request(), response.code());

                        else
                            iBboxGetChannelListOnBox.onResponse(parseJsonChannelsLight(response));
                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetChannelListOnBox.onFailure(request, errorCode);
            }
        });
    }

    private List<ChannelLight> parseJsonChannelsLight(Response response) {
        List<ChannelLight> channels = new ArrayList<>();

        if (response.code() == HttpURLConnection.HTTP_OK) {
            try {
                JsonReader reader = new JsonReader(new InputStreamReader(response.body().byteStream(), "UTF-8"));

                reader.beginArray();

                while (reader.hasNext()) {
                    ChannelLight channel = new ChannelLight(reader);
                    channels.add(channel);
                }

                reader.endArray();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return channels;
    }

    @Override
    public void getAppInfo(final String ip, String appId, String appSecret, final String packageName, final IBboxGetApplications iBboxGetApplications) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            public void onResponse(final String sessionId) {
                final Request request = new Request.Builder()
                        .url(URL_GET_APPLICATIONS.replace("@IP", ip) + "/" + packageName)
                        .header("x-sessionid", sessionId)
                        .build();

                Call mCall = mClient.newCall(request);
                mCall.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetApplications.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK)
                            iBboxGetApplications.onResponse(parseJsonApplications(URL_API_BOX.replace("@IP", ip), response));

                        else
                            iBboxGetApplications.onFailure(call.request(), response.code());

                        response.body().close();
                    }
                });
            }

            public void onFailure(Request request, int errorCode) {
                iBboxGetApplications.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getAppIcon(final String ip, String appId, String appSecret, final String packageName, final IBboxGetApplicationIcon iBboxGetApplicationIcon) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            public void onResponse(final String sessionId) {
                final Request request = new Request.Builder()
                        .url(URL_GET_APPLICATIONS.replace("@IP", ip) + "/" + packageName + "/image")
                        .header("x-sessionid", sessionId)
                        .build();

                Call mCall = mClient.newCall(request);
                mCall.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetApplicationIcon.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK)
                        {
                            InputStream in = response.body().byteStream();
                            Bitmap bmp = BitmapFactory.decodeStream(in);
                            iBboxGetApplicationIcon.onResponse(bmp);
                        }

                        else
                            iBboxGetApplicationIcon.onFailure(call.request(), response.code());

                        response.body().close();
                    }
                });
            }

            public void onFailure(Request request, int errorCode) {
                iBboxGetApplicationIcon.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getOpenedChannels(final String ip, String appId, String appSecret, final IBboxGetOpenedChannels iBboxGetOpenedChannels) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            public void onResponse(final String sessionId) {
                final Request request = new Request.Builder()
                        .url(URL_NOTIFICATION.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .build();

                Call mCall = mClient.newCall(request);
                mCall.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "Get notification channels failed");
                        iBboxGetOpenedChannels.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK)
                        {
                            Log.i(TAG, "Get notification channels success");
                            iBboxGetOpenedChannels.onResponse(parseJsonOpenedChannel(response));
                        }

                        else
                            iBboxGetOpenedChannels.onFailure(call.request(), response.code());

                        response.body().close();
                    }
                });
            }

            public void onFailure(Request request, int errorCode) {
                Log.i(TAG, "Get notification channels failed");
                iBboxGetOpenedChannels.onFailure(request, errorCode);
            }
        });
    }

    private List<String> parseJsonOpenedChannel(Response response) {
        List<String> channels = new ArrayList<>();

        if (response.code() == HttpURLConnection.HTTP_OK) {
            try {

                JSONArray jsonArray = new JSONArray(response.body().string());

                for (int i = 0; i < jsonArray.length(); i++) {
                    String channel = new String(jsonArray.getString(i));
                    channels.add(channel);
                }

                Log.i(TAG, "channels : " + channels.toString());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return channels;
    }

    @Override
    public void unsubscribeNotification(final String ip, String appId, String appSecret, final String channelId,
                                      final IBboxUnsubscribe iBboxUnsubscribe) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_NOTIFICATION + "/" + channelId;
                RequestBody body = RequestBody.create(JSON, "");
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .delete(body)
                        .build();

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "Unsubscribe Notification failed");
                        iBboxUnsubscribe.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            Log.i(TAG, "Unsubscribe Notification success");
                            iBboxUnsubscribe.onUnsubscribe();
                        } else {
                            Log.i(TAG, "Subscribe Notification failed");
                            iBboxUnsubscribe.onFailure(call.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxUnsubscribe.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void sendMessage(final String ip, final String appId, String appSecret,
                            final String channelIdOrRoomName, final String appIdFromRegister, final String msgToSend,
                            final IBboxSendMessage iBboxSendMessage) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_NOTIFICATION + "/" + channelIdOrRoomName;
                RequestBody body = RequestBody.create(JSON, buildJsonRequestMessage(appIdFromRegister, msgToSend));
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();
                Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "Send message failed before response");
                        iBboxSendMessage.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            Log.i(TAG, "Send message success");
                            iBboxSendMessage.onResponse();
                        } else {
                            Log.i(TAG, "Send message failed "+response.code());
                            iBboxSendMessage.onFailure(call.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxSendMessage.onFailure(request, errorCode);
            }
        });
    }

    private String buildJsonRequestMessage(String appId, String msg) {
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("appId", appId);
            jObject.put("message", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObject.toString();
    }


}