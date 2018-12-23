package com.xuhao.didi.socket.client.sdk.client;

import com.xuhao.didi.core.iocore.interfaces.IIOCoreOptions;
import com.xuhao.didi.core.protocol.IReaderProtocol;
import com.xuhao.didi.socket.client.impl.client.action.ActionDispatcher;
import com.xuhao.didi.socket.client.sdk.client.connection.AbsReconnectionManager;
import com.xuhao.didi.socket.client.sdk.client.connection.DefaultReconnectManager;
import com.xuhao.didi.socket.client.sdk.client.connection.abilities.IConfiguration;
import com.xuhao.didi.socket.common.interfaces.default_protocol.DefaultNormalReaderProtocol;

import java.nio.ByteOrder;

public class OkSocketOptions implements IIOCoreOptions {

    private static boolean isDebug;

    private IOThreadMode mIOThreadMode;

    private boolean isConnectionHolden;

    private ByteOrder mWriteOrder;

    private ByteOrder mReadByteOrder;

    private IReaderProtocol mReaderProtocol;

    private int mWritePackageBytes;

    private int mReadPackageBytes;

    private long mPulseFrequency;

    private int mPulseFeedLoseTimes;

    private int mConnectTimeoutSecond;

    private int mMaxReadDataMB;

    private AbsReconnectionManager mReconnectionManager;

    private OkSocketSSLConfig mSSLConfig;

    private OkSocketFactory mOkSocketFactory;

    private boolean isCallbackInIndependentThread;

    private ThreadModeToken mCallbackThreadModeToken;

    private OkSocketOptions() {
    }

    public static void setIsDebug(boolean isDebug) {
        OkSocketOptions.isDebug = isDebug;
    }

    public static abstract class ThreadModeToken {
        public abstract void handleCallbackEvent(ActionDispatcher.ActionRunnable runnable);
    }

    public static class Builder {
        private OkSocketOptions mOptions;

        public Builder() {
            this(OkSocketOptions.getDefault());
        }

        public Builder(IConfiguration configuration) {
            this(configuration.getOption());
        }

        public Builder(OkSocketOptions okOptions) {
            mOptions = okOptions;
        }

        public Builder setIOThreadMode(IOThreadMode IOThreadMode) {
            mOptions.mIOThreadMode = IOThreadMode;
            return this;
        }

        public Builder setMaxReadDataMB(int maxReadDataMB) {
            mOptions.mMaxReadDataMB = maxReadDataMB;
            return this;
        }

        public Builder setSSLConfig(OkSocketSSLConfig SSLConfig) {
            mOptions.mSSLConfig = SSLConfig;
            return this;
        }

        public Builder setReaderProtocol(IReaderProtocol readerProtocol) {
            mOptions.mReaderProtocol = readerProtocol;
            return this;
        }

        public Builder setPulseFrequency(long pulseFrequency) {
            mOptions.mPulseFrequency = pulseFrequency;
            return this;
        }

        public Builder setConnectionHolden(boolean connectionHolden) {
            mOptions.isConnectionHolden = connectionHolden;
            return this;
        }
        public Builder setPulseFeedLoseTimes(int pulseFeedLoseTimes) {
            mOptions.mPulseFeedLoseTimes = pulseFeedLoseTimes;
            return this;
        }

        public Builder setWriteOrder(ByteOrder writeOrder) {
            setWriteByteOrder(writeOrder);
            return this;
        }

        public Builder setWriteByteOrder(ByteOrder writeOrder) {
            mOptions.mWriteOrder = writeOrder;
            return this;
        }
        public Builder setReadByteOrder(ByteOrder readByteOrder) {
            mOptions.mReadByteOrder = readByteOrder;
            return this;
        }

        public Builder setWritePackageBytes(int writePackageBytes) {
            mOptions.mWritePackageBytes = writePackageBytes;
            return this;
        }
        public Builder setReadPackageBytes(int readPackageBytes) {
            mOptions.mReadPackageBytes = readPackageBytes;
            return this;
        }

        public Builder setConnectTimeoutSecond(int connectTimeoutSecond) {
            mOptions.mConnectTimeoutSecond = connectTimeoutSecond;
            return this;
        }
        public Builder setReconnectionManager(
                AbsReconnectionManager reconnectionManager) {
            mOptions.mReconnectionManager = reconnectionManager;
            return this;
        }

        public Builder setSocketFactory(OkSocketFactory factory) {
            mOptions.mOkSocketFactory = factory;
            return this;
        }

        public Builder setCallbackThreadModeToken(ThreadModeToken threadModeToken) {
            mOptions.mCallbackThreadModeToken = threadModeToken;
            return this;
        }

        public OkSocketOptions build() {
            return mOptions;
        }
    }

    public IOThreadMode getIOThreadMode() {
        return mIOThreadMode;
    }

    public long getPulseFrequency() {
        return mPulseFrequency;
    }

    public OkSocketSSLConfig getSSLConfig() {
        return mSSLConfig;
    }

    public OkSocketFactory getOkSocketFactory() {
        return mOkSocketFactory;
    }

    public int getConnectTimeoutSecond() {
        return mConnectTimeoutSecond;
    }

    public boolean isConnectionHolden() {
        return isConnectionHolden;
    }

    public int getPulseFeedLoseTimes() {
        return mPulseFeedLoseTimes;
    }

    public AbsReconnectionManager getReconnectionManager() {
        return mReconnectionManager;
    }

    public boolean isDebug() {
        return isDebug;
    }

    @Override
    public int getWritePackageBytes() {
        return mWritePackageBytes;
    }

    @Override
    public int getReadPackageBytes() {
        return mReadPackageBytes;
    }

    @Override
    public ByteOrder getWriteByteOrder() {
        return mWriteOrder;
    }

    @Override
    public IReaderProtocol getReaderProtocol() {
        return mReaderProtocol;
    }

    @Override
    public int getMaxReadDataMB() {
        return mMaxReadDataMB;
    }

    @Override
    public ByteOrder getReadByteOrder() {
        return mReadByteOrder;
    }

    public ThreadModeToken getCallbackThreadModeToken() {
        return mCallbackThreadModeToken;
    }

    public boolean isCallbackInIndependentThread() {
        return isCallbackInIndependentThread;
    }

    public static OkSocketOptions getDefault() {
        OkSocketOptions okOptions = new OkSocketOptions();
        okOptions.mPulseFrequency = 5 * 1000;
        okOptions.mIOThreadMode = IOThreadMode.DUPLEX;
        okOptions.mReaderProtocol = new DefaultNormalReaderProtocol();
        okOptions.mMaxReadDataMB = 5;
        okOptions.mConnectTimeoutSecond = 3;
        okOptions.mWritePackageBytes = 100;
        okOptions.mReadPackageBytes = 50;
        okOptions.mReadByteOrder = ByteOrder.BIG_ENDIAN;
        okOptions.mWriteOrder = ByteOrder.BIG_ENDIAN;
        okOptions.isConnectionHolden = true;
        okOptions.mPulseFeedLoseTimes = 5;
        okOptions.mReconnectionManager = new DefaultReconnectManager();
        okOptions.mSSLConfig = null;
        okOptions.mOkSocketFactory = null;
        okOptions.isCallbackInIndependentThread = true;
        okOptions.mCallbackThreadModeToken = null;
        return okOptions;
    }


    public enum IOThreadMode {
        SIMPLEX,
        DUPLEX;
    }
}