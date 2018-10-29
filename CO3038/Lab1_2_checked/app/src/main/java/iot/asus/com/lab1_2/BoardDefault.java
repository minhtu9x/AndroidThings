package iot.asus.com.lab1_2;

import android.os.Build;

public class BoardDefault {
    private static final String DEVICE_RPI3 = "rpi3";

    public static String getGPIOForLedB(){
        switch (Build.DEVICE) {
            case DEVICE_RPI3:
                return "BCM2";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }

    public static String getGPIOForLedR(){
        switch(Build.DEVICE){
            case DEVICE_RPI3:
                return "BCM3";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }

    public static String getGPIOForLedG(){
        switch(Build.DEVICE){
            case DEVICE_RPI3:
                return "BCM4";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }

    public static String getGPIOForButton(){
        switch(Build.DEVICE){
            case DEVICE_RPI3:
                return "BCM5";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }
}

