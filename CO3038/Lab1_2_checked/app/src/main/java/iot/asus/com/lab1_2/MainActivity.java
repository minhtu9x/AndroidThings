package iot.asus.com.lab1_2;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LED_RED = 1;
    private static final int LED_GREEN = 2;
    private static final int LED_BLUE = 3;
    private int mLedState = LED_RED;
    private Gpio mLedGpioR, mLedGpioG, mLedGpioB, mButtonGpio;

    private static int intervalBetweenBlink = 2000;
    private static final int INTERVAL_05s = 500;
    private static final int INTERVAL_1s = 1000;
    private static final int INTERVAL_2s = 2000;

    private Handler mHandler = new Handler();

    private boolean mLedStateR = true;
    private boolean mLedStateG = true;
    private boolean mLedStateB = true;
    Button mBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtn = (Button) findViewById(R.id.mbtn);
        try {
            PeripheralManager manager = PeripheralManager.getInstance();

            String buttonPin = BoardDefault.getGPIOForButton();
            mButtonGpio = manager.openGpio(buttonPin);
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setActiveType(Gpio.ACTIVE_HIGH);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);

            Log.i(TAG,"Gonna go register for button");

            mBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch(intervalBetweenBlink){
                        case INTERVAL_2s:
                            intervalBetweenBlink = 1000;
                            Log.i(TAG,"Blink in 1s");
                            break;

                        case INTERVAL_1s:
                            intervalBetweenBlink = 500;
                            Log.i(TAG,"Blink in 0.5s");
                            break;

                        case INTERVAL_05s:
                            intervalBetweenBlink = 2000;
                            Log.i(TAG,"Blink in 2s");
                            break;
                        default:
                            throw new IllegalStateException("State is incorrect");
                    }
                }
            });
//            mButtonGpio.registerGpioCallback(new GpioCallback() {
//                @Override
//                public boolean onGpioEdge(Gpio gpio) {
//                    Log.i(TAG, "Button pressed");
//                    switch(intervalBetweenBlink){
//                        case INTERVAL_2s:
//                            intervalBetweenBlink = 1000;
//                            Log.i(TAG,"Blink in 1s");
//                            break;
//
//                        case INTERVAL_1s:
//                            intervalBetweenBlink = 500;
//                            Log.i(TAG,"Blink in 0.5s");
//                            break;
//
//                        case INTERVAL_05s:
//                            intervalBetweenBlink = 2000;
//                            Log.i(TAG,"Blink in 2s");
//                            break;
//                        default:
//                            throw new IllegalStateException("State is incorrect");
//                    }
//                    return true;
//                }
//            });

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

                mHandler.postDelayed(mBlinkRunnable, intervalBetweenBlink);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };
}
