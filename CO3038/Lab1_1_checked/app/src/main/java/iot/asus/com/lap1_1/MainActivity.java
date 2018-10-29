package iot.asus.com.lap1_1;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LED_RED = 1;
    private static final int LED_GREEN = 2;
    private static final int LED_BLUE = 3;
    private int mLedState = LED_RED;
    private Gpio mLedGpioR, mLedGpioG, mLedGpioB;
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 2000;
    private Handler mHandler = new Handler();

    private boolean mLedStateR = true;
    private boolean mLedStateG = true;
    private boolean mLedStateB = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            PeripheralManager manager = PeripheralManager.getInstance();
            String ledPinR = BoardDefault.getGPIOForLedR();
            mLedGpioR = manager.openGpio(ledPinR);
            mLedGpioR.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            String ledPinB = BoardDefault.getGPIOForLedB();
            mLedGpioB = manager.openGpio(ledPinB);
            mLedGpioB.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            Log.d(TAG, "On  create");
            String ledPinG = BoardDefault.getGPIOForLedG();
            mLedGpioG = manager.openGpio(ledPinG);
            mLedGpioG.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            mHandler.post(mBlinkRunnable);

        } catch (IOException e) {
            // untill I do something with GPIO, like openGPIO
            // IOException didn't cause error
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mLedGpioR.close();
            mLedGpioB.close();
            mLedGpioG.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            Log.d(TAG, "On destroy");
            mLedGpioR = null;
            mLedGpioG = null;
            mLedGpioB = null;
        }
    }

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLedGpioB == null || mLedGpioG == null || mLedGpioR == null) {
                return;
            }

            try {
                switch (mLedState) {
                    case LED_RED:
                        mLedStateR = false;
                        mLedStateB = true;
                        mLedStateG = true;
                        mLedState = LED_GREEN;

                        Log.d(TAG, "Led Red");
                        break;
                    case LED_GREEN:
                        mLedStateG = false;
                        mLedStateB = true;
                        mLedStateR = true;
                        mLedState = LED_BLUE;
                        Log.d(TAG, "Led Green");
                        break;
                    case LED_BLUE:
                        mLedStateB = false;
                        mLedStateR = true;
                        mLedStateG = true;
                        mLedState = LED_RED;
                        Log.d(TAG, "Led blue");
                        break;
                    default:
                        break;
                }
                mLedGpioR.setValue(mLedStateR);
                mLedGpioB.setValue(mLedStateB);
                mLedGpioG.setValue(mLedStateG);

                mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };
}

