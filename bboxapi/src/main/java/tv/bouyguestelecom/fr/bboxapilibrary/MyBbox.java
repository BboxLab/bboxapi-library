package tv.bouyguestelecom.fr.bboxapilibrary;

import android.os.Build;

import tv.bouyguestelecom.fr.bboxapilibrary.util.WOLPowerManager;

/**
 * Created by dinh on 28/07/16.
 */

public class MyBbox {

    private final static String LOG_TAG = MyBbox.class.getName();
    private String ip;
    private String imei;
    private String macAddress;

    public MyBbox(String ip) {
        this.ip = ip;
        this.imei = Build.SERIAL.toString();
        this.macAddress = WOLPowerManager.getMacFromArpCache(ip);
        if (this.macAddress == null)
            this.macAddress = WOLPowerManager.getMACAddress("eth0");
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}