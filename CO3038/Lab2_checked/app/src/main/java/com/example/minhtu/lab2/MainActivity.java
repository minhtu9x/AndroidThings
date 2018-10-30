package com.example.minhtu.lab2;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import com.google.android.things.pio.*;

import java.io.IOException;

import static com.example.minhtu.lab2.BoardDefault.getPWMPort;


public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();


    private static final int LED_RED = 1;
    private static final int LED_GREEN = 2;
    private static final int LED_BLUE = 3;
    private int mLedState = LED_RED;
    private Gpio mLedGpioR, mLedGpioG, mLedGpioB;
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 2000;
    private static int intervalBetweenBlink = 2000;

    private static final int INTERVAL_05s = 500;
    private static final int INTERVAL_1s = 1000;
    private static final int INTERVAL_2s = 2000;


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

//    Button btn;
    public int status = 1;

    private boolean mLedStateR = true;
    private boolean mLedStateG = true;
    private boolean mLedStateB = true;

    // UART Configuration Parameters
    private static final int BAUD_RATE = 115200;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;

    private static final int CHUNK_SIZE = 1;

    private HandlerThread mInputThread;
    private Handler mInputHandler;

    private UartDevice mLoopbackDevice;

    private Runnable mTransferUartRunnable = new Runnable() {
        @Override
        public void run() {
            transferUartData();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Loopback Created");
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

        // Create a background looper thread for I/O
        mInputThread = new HandlerThread("InputThread");
        mInputThread.start();
        mInputHandler = new Handler(mInputThread.getLooper());

        // Attempt to access the UART device
        try {
            openUart(BoardDefault.getUartName(), BAUD_RATE);
            // Read any initially buffered data
            mInputHandler.post(mTransferUartRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Unable to open UART device", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Loopback Destroyed");

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


        // Terminate the worker thread
        if (mInputThread != null) {
            mInputThread.quitSafely();
        }

        // Attempt to close the UART device
        try {
            closeUart();
        } catch (IOException e) {
            Log.e(TAG, "Error closing UART device:", e);
        }
    }

    /**
     * Callback invoked when UART receives new incoming data.
     */
    private UartDeviceCallback mCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            // Queue up a data transfer
            transferUartData();
            //Continue listening for more interrupts
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.w(TAG, uart + ": Error event " + error);
        }
    };

    /* Private Helper Methods */

    /**
     * Access and configure the requested UART device for 8N1.
     *
     * @param name Name of the UART peripheral device to open.
     * @param baudRate Data transfer rate. Should be a standard UART baud,
     *                 such as 9600, 19200, 38400, 57600, 115200, etc.
     *
     * @throws IOException if an error occurs opening the UART port.
     */
    private void openUart(String name, int baudRate) throws IOException {
        mLoopbackDevice = PeripheralManager.getInstance().openUartDevice(name);
        // Configure the UART
        mLoopbackDevice.setBaudrate(baudRate);
        mLoopbackDevice.setDataSize(DATA_BITS);
        mLoopbackDevice.setParity(UartDevice.PARITY_NONE);
        mLoopbackDevice.setStopBits(STOP_BITS);

        mLoopbackDevice.registerUartDeviceCallback(mInputHandler, mCallback);
    }

    /**
     * Close the UART device connection, if it exists
     */
    private void closeUart() throws IOException {
        if (mLoopbackDevice != null) {
            mLoopbackDevice.unregisterUartDeviceCallback(mCallback);
            try {
                mLoopbackDevice.close();
            } finally {
                mLoopbackDevice = null;
            }
        }
    }

    /**
     * Loop over the contents of the UART RX buffer, transferring each
     * one back to the TX buffer to create a loopback service.
     *
     * Potentially long-running operation. Call from a worker thread.
     */
    private void transferUartData() {
        // Loop until there is no more data in the RX buffer.
        if (mLoopbackDevice != null) try {
            byte[] buffer = new byte[CHUNK_SIZE];
            int read;
            while ((read = mLoopbackDevice.read(buffer, buffer.length)) > 0) {
                mLoopbackDevice.write(buffer, read);
                Log.e(TAG, String.valueOf(buffer[0]));
                switch(buffer[0]) {
                    case 79 :
                        Log.e(TAG, "Ready to receive commands");
                        break;
                    case 49 :
                        Log.e(TAG, "1");
                        mHandler.removeCallbacks(mChangePWMRunnable);
                        mHandler.removeCallbacks(lab1_2);
                        mHandler.removeCallbacks(mBlinkRunnable_2);
                        mHandler.post(lab1_1);

                        break;
                    case 50 :
                        Log.e(TAG, "2");
                        mHandler.removeCallbacks(mChangePWMRunnable);
                        mHandler.removeCallbacks(lab1_1);
                        mHandler.removeCallbacks(mBlinkRunnable_2);
                        mHandler.post(lab1_2);
                        break;
                    case 51 :
                        Log.e(TAG, "3");
                        mHandler.removeCallbacks(mBlinkRunnable_2);
                        mHandler.post(mChangePWMRunnable);
                        mHandler.post(lab1_1);
                        break;
                    case 52 :
                        Log.e(TAG, "4");
                        mHandler.removeCallbacks(mChangePWMRunnable);
                        mHandler.removeCallbacks(lab1_1);
                        mHandler.removeCallbacks(mBlinkRunnable_2);
                        mHandler.post(lab1_4);
                        break;
                    case 53 :
                        Log.e(TAG, "5");
                        mHandler.removeCallbacks(mChangePWMRunnable);
                        mHandler.removeCallbacks(lab1_1);
                        mHandler.removeCallbacks(mBlinkRunnable_2);
                        mHandler.post(lab1_5);
                        break;
                    case 70 :
                        Log.e(TAG, "Stops any running");
                        break;
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "Unable to transfer data over UART", e);
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

//            Log.d(TAG, "Changing PWM active pulse duration to " + mActivePulseDuration + " ms");

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


    private Runnable lab1_1 = new Runnable() {
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

                mHandler.postDelayed(lab1_1, INTERVAL_BETWEEN_BLINKS_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

    private Runnable lab1_2 = new Runnable() {
        @Override
        public void run() {
            Button btn =  findViewById(R.id.btn);
            PeripheralManager manager = PeripheralManager.getInstance();

            Log.i(TAG,"Gonna go register for button");

            btn.setOnClickListener(new View.OnClickListener() {
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

            String ledPinR = BoardDefault.getGPIOForLedR();
            String ledPinB = BoardDefault.getGPIOForLedB();
            String ledPinG = BoardDefault.getGPIOForLedG();
            try {
                mLedGpioR = manager.openGpio(ledPinR);
                mLedGpioR.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
                mLedGpioB = manager.openGpio(ledPinB);
                mLedGpioB.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
                mLedGpioG = manager.openGpio(ledPinG);
                mLedGpioG.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mHandler.post(mBlinkRunnable_2);

        }
    };

    private Runnable mBlinkRunnable_2 = new Runnable() {
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

                mHandler.postDelayed(mBlinkRunnable_2, intervalBetweenBlink);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

    private Runnable lab1_4 = new Runnable() {
        @Override
        public void run() {
            Button btn = findViewById((R.id.btn));
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    switch (status) {
                        case 1:

                            mLedStateR = false;
                            mLedStateG = true;
                            mLedStateB = true;
                            mHandler.post(mBlinkRunnable_4);
                            Log.e(TAG, String.valueOf(status));
                            status++;
                            break;

                        case 2:
                            mLedStateR = true;
                            mLedStateG = false;
                            mLedStateB = true;
                            mHandler.post(mBlinkRunnable_4);
                            Log.e(TAG, String.valueOf(status));
                            status++;
                            break;

                        case 3:
                            mLedStateR = true;
                            mLedStateG = true;
                            mLedStateB = false;
                            mHandler.post(mBlinkRunnable_4);
                            Log.e(TAG, String.valueOf(status));
                            status = 1;
                            break;
                    }

                }

            });



        }
    };

    private Runnable mBlinkRunnable_4 = new Runnable() {
        @Override
        public void run() {
            if (mLedGpioB == null || mLedGpioG == null || mLedGpioR == null) {
                return;
            }
            try {
                mLedGpioR.setValue(mLedStateR);
                mLedGpioB.setValue(mLedStateB);
                mLedGpioG.setValue(mLedStateG);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    private static String mLedPinR = BoardDefault.getGPIOForLedR();
    private static String mLedPinG = BoardDefault.getGPIOForLedG();
    private static String mLedPinB = BoardDefault.getGPIOForLedB();

    private Runnable lab1_5 = new Runnable() {
        @Override
        public void run() {
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
    };

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
}