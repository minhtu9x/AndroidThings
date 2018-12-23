package com.xuhao.didi.oksocket;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xuhao.didi.core.iocore.interfaces.ISendable;
import com.xuhao.didi.core.pojo.OriginalData;
import com.xuhao.didi.core.utils.SLog;
import com.xuhao.didi.oksocket.data.MsgDataBean;
import com.xuhao.didi.socket.client.sdk.client.OkSocketOptions;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IClient;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IClientIOCallback;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IClientPool;
import com.xuhao.didi.socket.server.impl.OkServerOptions;

import java.nio.charset.Charset;
import java.util.Arrays;


public class DemoActivity extends AppCompatActivity implements IClientIOCallback {

    private Button mSimpleBtn;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);
        mSimpleBtn = findViewById(R.id.btn1);

        OkServerOptions.setIsDebug(true);
        OkSocketOptions.setIsDebug(true);
        SLog.setIsDebug(true);

        mSimpleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DemoActivity.this, SimpleDemoActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }



    @Override
    public void onClientRead(OriginalData originalData, IClient client, IClientPool<IClient, String> clientPool) {
        String str = new String(originalData.getBodyBytes(), Charset.forName("utf-8"));
        JsonObject jsonObject = null;
        try {
            jsonObject = new JsonParser().parse(str).getAsJsonObject();
            int cmd = jsonObject.get("cmd").getAsInt();
            if (cmd == 54) {
                String handshake = jsonObject.get("handshake").getAsString();
                Log.i("onClientIOServer", Thread.currentThread().getName() + " 接收到:" + client.getHostIp() + " 握手信息:" + handshake);
            } else if (cmd == 14) {//心跳
                Log.i("onClientIOServer", Thread.currentThread().getName() + " 接收到:" + client.getHostIp() + " 收到心跳");
            } else {
                Log.i("onClientIOServer", Thread.currentThread().getName() + " 接收到:" + client.getHostIp() + " " + str);
            }
        } catch (Exception e) {
            Log.i("onClientIOServer", Thread.currentThread().getName() + " 接收到:" + client.getHostIp() + " " + str);
        }
        MsgDataBean msgDataBean = new MsgDataBean(str);
        clientPool.sendToAll(msgDataBean);
    }

    @Override
    public void onClientWrite(ISendable sendable, IClient client, IClientPool<IClient, String> clientPool) {
        byte[] bytes = sendable.parse();
        bytes = Arrays.copyOfRange(bytes, 4, bytes.length);
        String str = new String(bytes, Charset.forName("utf-8"));
        JsonObject jsonObject = null;
        try {
            jsonObject = new JsonParser().parse(str).getAsJsonObject();
            int cmd = jsonObject.get("cmd").getAsInt();
            switch (cmd) {
                case 54: {
                    String handshake = jsonObject.get("handshake").getAsString();
                    Log.i("onClientIOServer", Thread.currentThread().getName() + " 发送给:" + client.getHostIp() + " 握手数据:" + handshake);
                    break;
                }
                default:
                    Log.i("onClientIOServer", Thread.currentThread().getName() + " 发送给:" + client.getHostIp() + " " + str);
            }
        } catch (Exception e) {
            Log.i("onClientIOServer", Thread.currentThread().getName() + " 发送给:" + client.getHostIp() + " " + str);
        }
    }

}
