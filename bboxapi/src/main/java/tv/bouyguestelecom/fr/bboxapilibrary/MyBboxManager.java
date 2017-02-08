package tv.bouyguestelecom.fr.bboxapilibrary;

/**
 * Created by dinh on 28/07/16.
 */
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

public class MyBboxManager {

    private final static String TAG = MyBboxManager.class.getName();
    private final static String SERVICE_NAME = "Bboxapi";
    private final static String SERVICE_TYPE_LOCAL = "_http._tcp.local.";
    private WifiManager.MulticastLock multicastLock = null;
    private WifiManager wifiManager = null;
    private JmDNS jmDNS;
    private ServiceListener serviceListener;
    private JmDNSThread jmDNSThread;

    public static InetAddress getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void startLookingForBbox(final Context context, final CallbackBboxFound callbackBboxFound) {

        Log.i(TAG, "Start looking for Bbox");

        if (multicastLock == null) {
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            multicastLock = wifiManager.createMulticastLock("LibBboxOpenAPI");
            multicastLock.setReferenceCounted(true);
            multicastLock.acquire();
        }

        jmDNSThread = new JmDNSThread(callbackBboxFound);
        jmDNSThread.execute();

    }

    public void stopLookingForBbox() {
        Log.i(TAG, "Stop looking for Bbox");
        StopThread stopThread = new StopThread();
        stopThread.execute();
    }

    private class StopThread extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (jmDNSThread != null) {
                jmDNS.removeServiceListener(SERVICE_TYPE_LOCAL, serviceListener);
                try {
                    jmDNS.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
                if (multicastLock != null) {
                    multicastLock.release();
                    multicastLock = null;
                }
                jmDNSThread.cancel(true);
            }
            return null;
        }
    };

    public interface CallbackBboxFound {
        public void onResult(MyBbox myBbox);
    }

    private class JmDNSThread extends AsyncTask<Void, Void, Void> {
        CallbackBboxFound callbackBboxFound;

        public JmDNSThread(CallbackBboxFound callbackBboxFound) {
            this.callbackBboxFound = callbackBboxFound;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                InetAddress ip = getLocalIpAddress();
                if (ip != null) {
                    Log.d(TAG, "My infos: " + ip.getHostAddress() + " " + ip.getHostName());
                    jmDNS = JmDNS.create(ip, ip.getHostName());
                } else {
                    jmDNS = JmDNS.create();
                }
                serviceListener = new ServiceListener() {

                    @Override
                    public void serviceAdded(ServiceEvent event) {
                        Log.d(TAG, "Service found and added : " + event.getName());
                    }

                    @Override
                    public void serviceRemoved(ServiceEvent event) {
                        Log.d(TAG, "Service remove: " + event.getName());
                    }

                    @Override
                    public void serviceResolved(ServiceEvent event) {
                        if (event.getName().contains(SERVICE_NAME)) {
                            String bboxIP = event.getInfo().getInet4Addresses()[0].getHostAddress();
                            Log.i(TAG, "Bbox found on IP : " + bboxIP);
                            callbackBboxFound.onResult(new MyBbox(bboxIP));
                        }
                    }
                };

                jmDNS.addServiceListener(SERVICE_TYPE_LOCAL, serviceListener);

            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }
    }
}
