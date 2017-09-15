package tv.bouyguestelecom.fr.bboxapilibrary.util;

import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Response;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Application;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Channel;
import tv.bouyguestelecom.fr.bboxapilibrary.model.ChannelLight;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Epg;
import tv.bouyguestelecom.fr.bboxapilibrary.model.EpgSimple;

/**
 * Created by rmessara on 08/09/17.
 * bboxapi-library
 */

public class Parser {

    public static String TAG = Parser.class.getSimpleName();


    public static List<String> parseJsonOpenedChannel(Response response) {
        List<String> channels = new ArrayList<>();

        if (response.code() == HttpURLConnection.HTTP_OK) {
            try {

                JSONArray jsonArray = new JSONArray(response.body().string());

                for (int i = 0; i < jsonArray.length(); i++) {
                    String channel = new String(jsonArray.getString(i));
                    channels.add(channel);
                }

                //Log.i(TAG, "channels : " + channels.toString());

            } catch (JSONException | IOException e) {
                Log.e(TAG, "Error occured", e);
                return null;
            }
        }

        return channels;
    }

    public static List<Channel> parseJsonChannels(Response response) {
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
                return null;
            }
        }

        return channels;
    }


    public static List<Epg> parseJsonListEpg(Response response) throws SocketException {
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
            return null;
        }

        return epgs;
    }

    public static List<EpgSimple> parseJsonEpgSimple(Response response) throws SocketException {
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
            return null;
        }

        return epgs;
    }

    public static Epg parseJsonEpg(Response response) throws SocketException {
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

    public static List<Application> parseJsonApplications(String ip, Response response) {
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
            return null;
        }

        Collections.sort(applications);

        return applications;
    }

    public static Channel parseJsonChannel(InputStream inputStream) {
        Channel channel = null;

        try {
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            channel = new Channel(reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return channel;
    }

    public static List<ChannelLight> parseJsonChannelsLight(Response response) {
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
                return null;
            }
        }

        return channels;
    }

    public static List<Channel> parseChannels(Response response) {
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
                return null;
            }
        }

        return channels;
    }
}
