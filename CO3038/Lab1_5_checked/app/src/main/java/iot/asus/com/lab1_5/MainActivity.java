package iot.asus.com.lab1_5;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.os.Handler;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class MainActivity extends Activity {
    private static String TAG = MainActivity.class.getSimpleName();
    private static String mLedPinR = BoardDefault.getGPIOForLedR();
    private static String mLedPinG = BoardDefault.getGPIOForLedG();
    private static String mLedPinB = BoardDefault.getGPIOForLedB();

    private Gpio mLedGpioR, mLedGpioG, mLedGpioB;

    private Handler mHandler = new Handler();

    private boolean mLedStateR = true, mLedStateB = true, mLedStateG = true ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"On create");
        super.onCreate(savedInstanceState);
        PeripheralManager manager = PeripheralManager.getInstance();
        try{
            mLedGpioB = manager.openGpio(mLedPinB);
            mLedGpioB.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioB.setValue(true);

            mLedGpioG = manager.openGpio(mLedPinG);
            mLedGpioG.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioG.setValue(true);

            mLedGpioR = manager.openGpio(mLedPinR);
            mLedGpioR.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioR.setValue(true);
        } catch (IOException e){
            Log.e(TAG,"Error: ",e);
        }

        mHandler.post(mRunnableLedB);
        mHandler.post(mRunnableLedG);
        mHandler.post(mRunnableLedR);
    }

    private Runnable mRunnableLedR = new Runnable() {
        @Override
        public void run() {
            mLedStateR = !mLedStateR;
            try{
                mLedGpioR.setValue(mLedStateR);
                mHandler.postDelayed(mRunnableLedR,500);
            } catch (IOException e){
                Log.e(TAG,"Error: ",e);
            }
        }
    };

    private Runnable mRunnableLedG = new Runnable() {
        @Override
        public void run() {
            mLedStateG = !mLedStateG;
            try{
                mLedGpioG.setValue(mLedStateG);
                mHandler.postDelayed(mRunnableLedG,1000);
            } catch (IOException e){
                Log.e(TAG,"Error: ",e);
            }
        }
    };

    private Runnable mRunnableLedB = new Runnable() {
        @Override
        public void run() {
            mLedStateB = !mLedStateB;
            try{
                mLedGpioB.setValue(mLedStateB);
                mHandler.postDelayed(mRunnableLedB,2000);
            } catch (IOException e){
                Log.e(TAG,"Error: ",e);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnableLedR);
        mHandler.removeCallbacks(mRunnableLedG);
        mHandler.removeCallbacks(mRunnableLedB);

        if (mLedGpioR != null || mLedGpioB != null || mLedGpioG != null) {
            try {
                mLedGpioR.close();
                mLedGpioG.close();
                mLedGpioB.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API27", e);
            } finally {
                mLedGpioR = null;
                mLedGpioB = null;
                mLedGpioG = null;
            }
        }
    }
}

