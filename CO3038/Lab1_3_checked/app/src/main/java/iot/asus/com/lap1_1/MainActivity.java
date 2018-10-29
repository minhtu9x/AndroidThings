package iot.asus.com.lap1_1;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

import java.io.IOException;

import static iot.asus.com.lap1_1.BoardDefault.getPWMPort;


public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    // Parameters of the servo PWM
    private static final double MIN_ACTIVE_PULSE_DURATION_MS = 0;
    private static final double MAX_ACTIVE_PULSE_DURATION_MS = 10;
    private static final double PULSE_PERIOD_MS = 10;  // Frequency of 50Hz (1000/20)

    // Parameters for the servo movement over time
    private static final double PULSE_CHANGE_PER_STEP_MS = 0.2;
    private static final int INTERVAL_BETWEEN_STEPS_MS = 100;

    private Handler mHandler = new Handler();
    private Pwm mPwm;
    private boolean mIsPulseIncreasing = true;
    private double mActivePulseDuration;
    private Gpio mLedGpioR, mLedGpioG, mLedGpioB;

    public int status = 1;
    private static final int LED_RED = 1;
    private static final int LED_GREEN = 2;
    private static final int LED_BLUE = 3;
    private int mLedState = LED_RED;
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 5000;

    private boolean mLedStateR = true;
    private boolean mLedStateG = true;
    private boolean mLedStateB = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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

        Log.i(TAG, "Starting PwmActivity");

        try {
            String pinName = getPWMPort();
            mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;

            mPwm = PeripheralManager.getInstance().openPwm(pinName);

            // Always set frequency and initial duty cycle before enabling PWM
            mPwm.setPwmFrequencyHz(1000 / PULSE_PERIOD_MS);
            mPwm.setPwmDutyCycle(mActivePulseDuration);
            mPwm.setEnabled(true);

            // Post a Runnable that continuously change PWM pulse width, effectively changing the
            // servo position
            Log.d(TAG, "Start changing PWM pulse");
            mHandler.post(mChangePWMRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove pending Runnable from the handler.
        mHandler.removeCallbacks(mChangePWMRunnable);
        // Close the PWM port.
        Log.i(TAG, "Closing port");
        try {
            mPwm.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mPwm = null;
        }

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

    private Runnable mChangePWMRunnable = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the port is already closed
            if (mPwm == null) {
                Log.w(TAG, "Stopping runnable since mPwm is null");
                return;
            }

            // Change the duration of the active PWM pulse, but keep it between the minimum and
            // maximum limits.
            // The direction of the change depends on the mIsPulseIncreasing variable, so the pulse
            // will bounce from MIN to MAX.
            if (mIsPulseIncreasing) {
                mActivePulseDuration += PULSE_CHANGE_PER_STEP_MS;
            } else {
                mActivePulseDuration -= PULSE_CHANGE_PER_STEP_MS;
            }

            // Bounce mActivePulseDuration back from the limits
            if (mActivePulseDuration > MAX_ACTIVE_PULSE_DURATION_MS) {
                mActivePulseDuration = MAX_ACTIVE_PULSE_DURATION_MS;
                mIsPulseIncreasing = !mIsPulseIncreasing;
            } else if (mActivePulseDuration < MIN_ACTIVE_PULSE_DURATION_MS) {
                mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
                mIsPulseIncreasing = !mIsPulseIncreasing;
            }

            Log.d(TAG, "Changing PWM active pulse duration to " + mActivePulseDuration + " ms");

            try {

                // Duty cycle is the percentage of active (on) pulse over the total duration of the
                // PWM pulse
                mPwm.setPwmDutyCycle(100 * mActivePulseDuration / PULSE_PERIOD_MS);

                // Reschedule the same runnable in {@link #INTERVAL_BETWEEN_STEPS_MS} milliseconds
                mHandler.postDelayed(this, INTERVAL_BETWEEN_STEPS_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

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

