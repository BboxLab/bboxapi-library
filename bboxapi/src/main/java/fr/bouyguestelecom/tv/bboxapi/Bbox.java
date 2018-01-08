package fr.bouyguestelecom.tv.bboxapi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.ConditionVariable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxApplication;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxDisplayToast;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetApplicationIcon;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetApplications;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetChannelListOnBox;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetChannels;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetCurrentChannel;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetEpgChannelNumber;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetOpenedChannels;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetSessionId;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetToken;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetVolume;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxMedia;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxMessage;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxRegisterApp;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxSendMessage;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxSetVolume;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxStartApplication;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxStopApplication;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxSubscribe;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxUnsubscribe;
import fr.bouyguestelecom.tv.bboxapi.model.Channel;
import fr.bouyguestelecom.tv.bboxapi.util.ListenerList;
import fr.bouyguestelecom.tv.bboxapi.util.Parser;
import fr.bouyguestelecom.tv.bboxapi.ws.WebSocket;

public class Bbox implements IBbox {
    public interface DiscoveryListener {
        public void bboxFound(Bbox bbox);
    }

    private static final String TAG = Bbox.class.getSimpleName();
    private static final String VERSION = "v1.3";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String URL_API_TOKEN = "https://api.bbox.fr/" + VERSION + "/security/token";
    private static final String URL_GET_CHANNELS = "https://api.bbox.fr/" + VERSION + "/media/channels";

    private static final String URL_API_BOX = "http://@IP:@PORT/api.bbox.lan/v0";
    private static final String URL_SESSION_ID = URL_API_BOX + "/security/sessionId";
    private static final String URL_GET_APPLICATIONS = URL_API_BOX + "/applications";
    private static final String LOCAL_URL_GET_CHANNELS = URL_API_BOX + "/media/tvchannellist";
    // private static final String LOCAL_URL_GET_TODAY_EPG = URL_API_BOX + "/media/programs";
    //  private static final String LOCAL_URL_GET_EPG_BY_ID = URL_API_BOX + "/media/program";
    private static final String URL_GET_CURRENT_CHANNEL = URL_API_BOX + "/media";
    private static final String URL_REGISTER_APP = URL_API_BOX + "/applications/register";
    private static final String URL_NOTIFICATION = URL_API_BOX + "/notification";

    private static final String URL_START_APP = URL_API_BOX + "/applications";

    private static final String URL_DISPLAY_TOAST = URL_API_BOX + "/userinterface/toast";
    private static final String URL_VOLUME = URL_API_BOX + "/userinterface/volume";

    private static Bbox instance;
    private static final ConditionVariable instanceLock = new ConditionVariable();

    private final String ip;
    private final int httpPort;
    private int wsPort;
    private final String appId;
    private final String appSecret;
    private OkHttpClient mClient;
    private WebSocket mWebSocket;
    private String mSessionId;
    private String mToken;
    private Long mValidityToken = (long) -1;
    private Long mValiditySessionId = (long) -1;
    private boolean hasSecurity = true;
    private ListenerList<IBboxMedia> notifMedia = new ListenerList<>();
    private ListenerList<IBboxApplication> notifApps = new ListenerList<>();
    private ListenerList<IBboxMessage> notifMsg = new ListenerList<>();

    private Bbox(String appId, String appSecret, String ip, int httpPort, int wsPort) {
        this.ip = ip;
        this.httpPort = httpPort;
        this.wsPort = wsPort;
        this.appId = appId;
        this.appSecret = appSecret;
        mClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(1, 5, TimeUnit.MINUTES))
                .build();
    }

    public static Bbox getInstance(boolean wait) {
        if (instance == null && wait) {
            if (!instanceLock.block(30000)) {
                Log.w(TAG, "Cannot find Bbox in 30 seconds", new TimeoutException("Cannot find Bbox in 30 seconds"));
            }
        }
        return instance;
    }

    public String getIp() {
        return ip;
    }

    public Call getToken(final IBboxGetToken iBboxGetToken) {
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
                    Log.e(TAG, "Get token failed", e);
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

    public void getSessionId(final IBboxGetSessionId iBboxGetSessionId) {
        if (hasSecurity) {
            getToken(new IBboxGetToken() {
                @Override
                public void onResponse(String token) {
                    Long currentTimeMillis = System.currentTimeMillis();

                    if (mValiditySessionId == -1 || mValiditySessionId <= currentTimeMillis) {
                        try {
                            RequestBody body = RequestBody.create(JSON, new JSONObject().put("token", token).toString());
                            Request request = new Request.Builder()
                                    .url(URL_SESSION_ID.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)))
                                    .post(body)
                                    .build();
                            //Log.d(TAG, "url =  : " + URL_SESSION_ID.replace("@IP", ip));
                            Call call = mClient.newCall(request);
                            call.enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Log.e(TAG, "Get sessionId failed", e);
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
                                        int responseCode = response.code();
                                        Log.w(TAG, "Cannot obtain bboxapi sessionId: " + responseCode);
                                        mSessionId = null;
                                        mValiditySessionId = (long) -1;
                                        iBboxGetSessionId.onFailure(call.request(), response.code());
                                    }

                                    response.body().close();
                                }
                            });
                        } catch (JSONException e) {
                            Log.e(TAG, "Cannot build getSessionId() request", e);
                            iBboxGetSessionId.onFailure(null, 500);
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
    public void getLocalChannels(final IBboxGetChannels iBboxGetChannels) {
        getSessionId(new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                final Request request = new Request.Builder()
                        .url(LOCAL_URL_GET_CHANNELS.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)))
                        .header("x-sessionid", sessionId)
                        .build();

                Call mCall = mClient.newCall(request);
                mCall.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetChannels.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        Log.i(TAG, "Get channels success");
                        if (response.code() == HttpURLConnection.HTTP_OK) {
                            List<Channel> channels = Parser.parseJsonChannels(response);
                            String logoUrlPrefix = URL_API_BOX.replace("@IP", ip);
                            for (Channel channel : channels) {
                                channel.setLogo(logoUrlPrefix + "/media/" + channel.getPositionId() + "/image");
                            }
                            iBboxGetChannels.onResponse(channels);
                        } else {
                            iBboxGetChannels.onFailure(response.request(), response.code());
                        }

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
    public void getApps(final IBboxGetApplications iBboxGetApplications) {
        getSessionId(new IBboxGetSessionId() {
            public void onResponse(final String sessionId) {
                Request.Builder requestBuilder = new Request.Builder()
                        .url(URL_GET_APPLICATIONS.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)));
                if (sessionId != null) {
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
                            iBboxGetApplications.onResponse(Parser.parseJsonApplications(URL_API_BOX.replace("@IP", ip), response));

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
    public void getCurrentChannel(final IBboxGetCurrentChannel iBboxGetCurrentChannel) {
        getSessionId(new IBboxGetSessionId() {
            public void onResponse(final String sessionId) {
                final Request request = new Request.Builder()
                        .url(URL_GET_CURRENT_CHANNEL.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)))
                        .header("x-sessionid", sessionId)
                        .build();

                Call mCall = mClient.newCall(request);

                mCall.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetCurrentChannel.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(final Call call, final Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK) {
                            final Channel bboxChannel = Parser.parseJsonChannel(response.body().byteStream());

                            getChannel(mToken, bboxChannel.getName(), "adsl", new IBboxGetEpgChannelNumber() {
                                @Override
                                public void onResponse(int epgChannelNumber) {
                                    bboxChannel.setEpgChannelNumber(epgChannelNumber);
                                    iBboxGetCurrentChannel.onResponse(bboxChannel);
                                }

                                @Override
                                public void onFailure(Request request, int errorCode) {
                                    iBboxGetCurrentChannel.onFailure(call.request(), response.code());
                                }
                            });
                        }
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


    private void getChannel(final String token, String name, final String profil, final IBboxGetEpgChannelNumber iBboxGetEpgChannelNumber) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(URL_GET_CHANNELS).newBuilder();
        urlBuilder.addQueryParameter("profil", profil);
        urlBuilder.addQueryParameter("name", name);

        Request request = new Request.Builder()
                .url(urlBuilder.build().toString())
                .addHeader("x-token", token)
                .build();

        Call call = mClient.newCall(request);

        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "Get channels failure");
                iBboxGetEpgChannelNumber.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    List<Channel> channels = Parser.parseChannels(response);

                    if (channels != null &&
                            !channels.isEmpty()) {
                        Log.v(TAG, "Get channels ==> : " + channels.toString());
                        iBboxGetEpgChannelNumber.onResponse(channels.get(0).getEpgChannelNumber());
                    }
                }
            }
        });
    }

    @Override
    public void registerApp(final String appName, final IBboxRegisterApp iBboxRegisterApp) {
        getSessionId(new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                try {
                    RequestBody body = RequestBody.create(JSON, new JSONObject().put("appName", appName).toString());
                    final Request request = new Request.Builder()
                            .url(URL_REGISTER_APP.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)))
                            .header("x-sessionid", sessionId)
                            .post(body)
                            .build();

                    Call call = mClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e(TAG, "Register app failed", e);
                            iBboxRegisterApp.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.code() == HttpURLConnection.HTTP_NO_CONTENT
                                    && response.headers().get("Location") != null
                                    && !response.headers().get("Location").isEmpty()) {
                                //Log.i(TAG, "Register app success");
                                String location = response.headers().get("Location");
                                String appId = location.substring(location.lastIndexOf('/') + 1);
                                iBboxRegisterApp.onResponse(appId);
                            } else {
                                //Log.i(TAG, "Register app failed");
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
    public void subscribeNotification(final String appRegisterId, final String ressourceId,
                                      final IBboxSubscribe iBboxSubscribe) {
        //notification_body='{"appId":"'"${appId}"'","resources" : [ {"resourceId":"Iot"} ] }'
        getSessionId(new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                IBboxSubscribe bothSubscribe = iBboxSubscribe;
                if (mWebSocket == null || mWebSocket.isClosed()) {
                    bothSubscribe = new IBboxSubscribe() {
                        CountDownLatch countDown = new CountDownLatch(2);

                        @Override
                        public void onSubscribe() {
                            countDown.countDown();
                            if (countDown.getCount() == 0) {
                                iBboxSubscribe.onSubscribe();
                            }
                        }

                        @Override
                        public void onFailure(Request request, int errorCode) {
                            iBboxSubscribe.onFailure(request, errorCode);
                        }
                    };
                    mWebSocket = new WebSocket(Bbox.this, ip, wsPort, appRegisterId, bothSubscribe);
                }
                RequestBody body = RequestBody.create(JSON, buildJsonRequestNotif(appRegisterId, ressourceId));
                final Request request = new Request.Builder()
                        .url(URL_NOTIFICATION.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();

                Call call = mClient.newCall(request);
                final IBboxSubscribe finalBboxSubscribe = bothSubscribe;
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Subscribe Notification failed", e);
                        finalBboxSubscribe.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            //Log.i(TAG, "Subscribe Notification success");
                            finalBboxSubscribe.onSubscribe();
                        } else {
                            //Log.i(TAG, "Subscribe Notification failed");
                            finalBboxSubscribe.onFailure(call.request(), response.code());
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
    public String addListener(IBboxMedia iBboxMedia) {
        return addNotifChannelListener(iBboxMedia);
    }

    @Override
    public String addListener(IBboxApplication iBboxApplication) {
        return addNotifApplication(iBboxApplication);
    }

    @Override
    public String addListener(IBboxMessage iBboxMessage) {
        return addNotifMessage(iBboxMessage);
    }

    @Override
    public void removeMediaListener(String appId, String channelListenerId) {
        if (getNotifMedia() != null) {
            removeNotifChannelListener(channelListenerId);
        }
    }

    @Override
    public void removeAppListener(String appId, String channelListenerId) {
        if (getNotifApps() != null) {
            removeNotifApps(channelListenerId);
        }
    }

    @Override
    public void removeMsgListener(String appId, String channelListenerId) {
        if (getNotifMsg() != null) {
            removeNotifMsg(channelListenerId);
        }
    }


    private String buildJsonRequestToken(String appId, String appSecret) {
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("appId", appId);
            jObject.put("appSecret", appSecret);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot build token request", e);
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
            Log.e(TAG, "Cannot build notif request", e);
        }
        return jObject.toString();
    }

    @Override
    public void startApp(final String packageName,
                         final IBboxStartApplication iBboxStartApplication) {
        getSessionId(new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_START_APP + "/" + packageName;
                RequestBody body = RequestBody.create(JSON, "");
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();
                //Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Start app failed before response", e);
                        iBboxStartApplication.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            //Log.i(TAG, "Start app success");
                            iBboxStartApplication.onResponse();
                        } else {
                            //Log.i(TAG, "Start app failed "+response.code());
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
    public void startApp(final String packageName, final String deeplink,
                         final IBboxStartApplication iBboxStartApplication) {
        getSessionId(new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_START_APP + "/" + packageName;
                RequestBody body = RequestBody.create(JSON, buildJsonRequestDeeplink(deeplink));
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();
                //Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Start app failed before response", e);
                        iBboxStartApplication.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            //Log.i(TAG, "Start app success");
                            iBboxStartApplication.onResponse();
                        } else {
                            //Log.i(TAG, "Start app failed "+response.code());
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
            Log.e(TAG, "Error occured", e);
        }
        return jObject.toString();
    }

    @Override
    public void displayToast(final String msg, final String color, final String duration,
                             final String pos_x, final String pos_y,
                             final IBboxDisplayToast iBboxDisplayToast) {
        getSessionId(new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_DISPLAY_TOAST;
                RequestBody body = RequestBody.create(JSON, buildJsonRequestToast(msg, color, duration, pos_x, pos_y));
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();
                //Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Display toast failed", e);
                        iBboxDisplayToast.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            //Log.i(TAG, "Display toast success");
                            iBboxDisplayToast.onResponse();
                        } else {
                            //Log.i(TAG, "Display toast failed");
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
    public void stopApp(final String packageName,
                        final IBboxStopApplication iBboxStopApplication) {
        getSessionId(new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_START_APP + "/" + packageName;
                RequestBody body = RequestBody.create(JSON, "");
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)))
                        .header("x-sessionid", sessionId)
                        .delete(body)
                        .build();
                //Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Stop app failed before response", e);
                        iBboxStopApplication.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            //Log.i(TAG, "Stop app success");
                            iBboxStopApplication.onResponse();
                        } else {
                            //Log.i(TAG, "Stop app failed");
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
    public void setVolume(final String volume,
                          final IBboxSetVolume iBboxSetVolume) {
        getSessionId(new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_VOLUME;
                RequestBody body = RequestBody.create(JSON, buildJsonRequestVolume(volume));
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();
                //Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //Log.e(TAG, "Set volume failed", e);
                        iBboxSetVolume.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            //Log.i(TAG, "Set volume success");
                            iBboxSetVolume.onResponse();
                        } else {
                            //Log.i(TAG, "Set volume failed");
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
    public void getVolume(final IBboxGetVolume iBboxGetVolume) {
        getSessionId(new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_VOLUME;
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)))
                        .header("x-sessionid", sessionId)
                        .build();
                //Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Get volume failed", e);
                        iBboxGetVolume.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK) {
                            //Log.i(TAG, "Get volume success");
                            String volume = response.body().string();
                            iBboxGetVolume.onResponse(volume);
                        } else {
                            //Log.i(TAG, "Get volume failed");
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
    public void getChannelListOnBox(final IBboxGetChannelListOnBox iBboxGetChannelListOnBox) {
        getSessionId(new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {

                String url = LOCAL_URL_GET_CHANNELS;
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)))
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
                            iBboxGetChannelListOnBox.onResponse(Parser.parseJsonChannelsLight(response));
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


    @Override
    public void getAppInfo(final String packageName, final IBboxGetApplications iBboxGetApplications) {
        getSessionId(new IBboxGetSessionId() {
            public void onResponse(final String sessionId) {
                final Request request = new Request.Builder()
                        .url(URL_GET_APPLICATIONS.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)) + "/" + packageName)
                        .header("x-sessionid", sessionId)
                        .build();

                Call mCall = mClient.newCall(request);
                mCall.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetApplications.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK)
                            iBboxGetApplications.onResponse(Parser.parseJsonApplications(URL_API_BOX.replace("@IP", ip), response));

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
    public void getAppIcon(final String packageName, final IBboxGetApplicationIcon iBboxGetApplicationIcon) {
        getSessionId(new IBboxGetSessionId() {
            public void onResponse(final String sessionId) {
                final Request request = new Request.Builder()
                        .url(URL_GET_APPLICATIONS.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)) + "/" + packageName + "/image")
                        .header("x-sessionid", sessionId)
                        .build();

                Call mCall = mClient.newCall(request);
                mCall.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetApplicationIcon.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK) {
                            InputStream in = response.body().byteStream();
                            Bitmap bmp = BitmapFactory.decodeStream(in);
                            iBboxGetApplicationIcon.onResponse(bmp);
                        } else
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
    public void getOpenedChannels(final IBboxGetOpenedChannels iBboxGetOpenedChannels) {
        getSessionId(new IBboxGetSessionId() {
            public void onResponse(final String sessionId) {
                final Request request = new Request.Builder()
                        .url(URL_NOTIFICATION.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)))
                        .header("x-sessionid", sessionId)
                        .build();

                Call mCall = mClient.newCall(request);
                mCall.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Get notification channels failed", e);
                        iBboxGetOpenedChannels.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK) {
                            //Log.i(TAG, "Get notification channels success");
                            iBboxGetOpenedChannels.onResponse(Parser.parseJsonOpenedChannel(response));
                        } else
                            iBboxGetOpenedChannels.onFailure(call.request(), response.code());

                        response.body().close();
                    }
                });
            }

            public void onFailure(Request request, int errorCode) {
                //Log.i(TAG, "Get notification channels failed");
                iBboxGetOpenedChannels.onFailure(request, errorCode);
            }
        });
    }


    @Override
    public void unsubscribeNotification(final String channelId,
                                        final IBboxUnsubscribe iBboxUnsubscribe) {
        getSessionId(new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_NOTIFICATION + "/" + channelId;
                RequestBody body = RequestBody.create(JSON, "");
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)))
                        .header("x-sessionid", sessionId)
                        .delete(body)
                        .build();

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Unsubscribe Notification failed", e);
                        iBboxUnsubscribe.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            //Log.i(TAG, "Unsubscribe Notification success");
                            iBboxUnsubscribe.onUnsubscribe();
                        } else {
                            //Log.i(TAG, "Subscribe Notification failed");
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
    public void sendMessage(final String channelIdOrRoomName, final String appIdFromRegister, final String msgToSend,
                            final IBboxSendMessage iBboxSendMessage) {
        getSessionId(new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_NOTIFICATION + "/" + channelIdOrRoomName;
                RequestBody body = RequestBody.create(JSON, buildJsonRequestMessage(appIdFromRegister, msgToSend));
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip).replace("@PORT", Integer.toString(httpPort)))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();
                //Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Send message failed before response", e);
                        iBboxSendMessage.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            //Log.i(TAG, "Send message success");
                            iBboxSendMessage.onResponse();
                        } else {
                            //Log.i(TAG, "Send message failed "+response.code());
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


    public String addNotifChannelListener(IBboxMedia iChannelListener) {
        return notifMedia.add(iChannelListener);
    }

    public void removeNotifChannelListener(String iMediaListenerId) {
        notifMedia.remove(iMediaListenerId);
    }

    public String addNotifApplication(IBboxApplication iAppListenerId) {
        return notifApps.add(iAppListenerId);
    }

    public void removeNotifApps(String iAppListenerId) {
        notifApps.remove(iAppListenerId);
    }

    public String addNotifMessage(IBboxMessage iMsgListenerId) {
        return notifMsg.add(iMsgListenerId);
    }

    public void removeNotifMsg(String iMsgListenerId) {
        notifMsg.remove(iMsgListenerId);
    }

    public ListenerList<IBboxMedia> getNotifMedia() {
        return notifMedia;
    }

    public ListenerList<IBboxApplication> getNotifApps() {
        return notifApps;
    }

    public ListenerList<IBboxMessage> getNotifMsg() {
        return notifMsg;
    }

    private static class WsResolveListener implements NsdManager.ResolveListener {
        private final NsdManager nsdManager;
        private final NsdManager.DiscoveryListener wsDiscoveryListener;
        private final DiscoveryListener bboxDiscoveryListener;
        private final String appId;
        private final String appSecret;
        private final int httpPort;

        public WsResolveListener(NsdManager nsdManager, NsdManager.DiscoveryListener wsDiscoveryListener, DiscoveryListener bboxDiscoveryListener, final String appId, final String appSecret, int httpPort) {
            this.nsdManager = nsdManager;
            this.wsDiscoveryListener = wsDiscoveryListener;
            this.bboxDiscoveryListener = bboxDiscoveryListener;
            this.appId = appId;
            this.appSecret = appSecret;
            this.httpPort = httpPort;
        }


        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {

        }

        @Override
        public void onServiceResolved(final NsdServiceInfo serviceInfo) {
            Log.i(TAG, "BboxapiWebSocket found " + serviceInfo.getHost() + ':' + serviceInfo.getPort());
            nsdManager.stopServiceDiscovery(wsDiscoveryListener);
            instance = new Bbox(appId, appSecret, serviceInfo.getHost().getHostAddress(), httpPort, serviceInfo.getPort());
            instanceLock.open();
            if (bboxDiscoveryListener != null) {
                bboxDiscoveryListener.bboxFound(instance);
            }
        }
    }

    private static class HttpResolveListener implements NsdManager.ResolveListener {
        private final NsdManager nsdManager;
        private final NsdManager.DiscoveryListener httpDiscoveryListener;
        private final DiscoveryListener bboxDiscoveryListener;
        private final String appId;
        private final String appSecret;

        public HttpResolveListener(NsdManager nsdManager, NsdManager.DiscoveryListener httpDiscoveryListener, DiscoveryListener bboxDiscoveryListener, final String appId, final String appSecret) {
            this.nsdManager = nsdManager;
            this.httpDiscoveryListener = httpDiscoveryListener;
            this.bboxDiscoveryListener = bboxDiscoveryListener;
            this.appId = appId;
            this.appSecret = appSecret;
        }

        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {

        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            final int httpPort = serviceInfo.getPort();
            if (httpPort == 8080) {
                Log.i(TAG, "Bboxapi found " + serviceInfo.getHost() + ':' + serviceInfo.getPort());
                nsdManager.stopServiceDiscovery(httpDiscoveryListener);
                instance = new Bbox(appId, appSecret, serviceInfo.getHost().getHostAddress(), httpPort, 9090);
                instanceLock.open();
                if (bboxDiscoveryListener != null) {
                    bboxDiscoveryListener.bboxFound(instance);
                }
            } else {
                Log.i(TAG, "Bboxapi found " + serviceInfo.getHost() + ':' + serviceInfo.getPort() + ". Wait for Bboxapi WebSocket to be discovered");
                nsdManager.stopServiceDiscovery(httpDiscoveryListener);
                nsdManager.discoverServices("_ws._tcp.", NsdManager.PROTOCOL_DNS_SD, new NsdManager.DiscoveryListener() {
                    @Override
                    public void onStartDiscoveryFailed(String serviceType, int errorCode) {

                    }

                    @Override
                    public void onStopDiscoveryFailed(String serviceType, int errorCode) {

                    }

                    @Override
                    public void onDiscoveryStarted(String serviceType) {

                    }

                    @Override
                    public void onDiscoveryStopped(String serviceType) {

                    }

                    @Override
                    public void onServiceFound(NsdServiceInfo serviceInfo) {
                        if ("BboxapiWebSocket".equals(serviceInfo.getServiceName())) {
                            nsdManager.resolveService(serviceInfo, new WsResolveListener(nsdManager, this, bboxDiscoveryListener, appId, appSecret, httpPort));
                        }
                    }

                    @Override
                    public void onServiceLost(NsdServiceInfo serviceInfo) {

                    }
                });
            }
        }
    }

    public static void discoverBboxApi(final Context context, final String appId, final String appSecret, final DiscoveryListener discoveryListener) {
        final NsdManager nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        nsdManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {

            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {

            }

            @Override
            public void onDiscoveryStarted(String serviceType) {

            }

            @Override
            public void onDiscoveryStopped(String serviceType) {

            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                String serviceName = serviceInfo.getServiceName();
                if ("Bboxapi".equals(serviceName)) {
                    nsdManager.resolveService(serviceInfo, new HttpResolveListener(nsdManager, this, discoveryListener, appId, appSecret));
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {

            }
        });
    }
}