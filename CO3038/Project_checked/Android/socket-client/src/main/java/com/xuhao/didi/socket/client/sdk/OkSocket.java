package com.xuhao.didi.socket.client.sdk;


import com.xuhao.didi.socket.client.impl.client.ManagerHolder;
import com.xuhao.didi.socket.client.sdk.client.ConnectionInfo;
import com.xuhao.didi.socket.client.sdk.client.OkSocketOptions;
import com.xuhao.didi.socket.client.sdk.client.connection.IConnectionManager;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.dispatcher.IRegister;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IServerActionListener;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IServerManager;

/**
 * OkSocket是一款轻量级的Socket通讯框架,可以提供单工,双工的TCP通讯.
 * 本类提供OkSocket的所有对外接口,使用OkSocket框架应从本类的open开启一个连接通道.
 * Created by xuhao on 2017/5/16.
 */
public class OkSocket {

    private static ManagerHolder holder = ManagerHolder.getInstance();

    public static IRegister<IServerActionListener, IServerManager> server(int serverPort) {
        return (IRegister<IServerActionListener, IServerManager>) holder.getServer(serverPort);
    }
    public static IConnectionManager open(ConnectionInfo connectInfo) {
        return holder.getConnection(connectInfo);
    }
    public static IConnectionManager open(String ip, int port) {
        ConnectionInfo info = new ConnectionInfo(ip, port);
        return holder.getConnection(info);
    }
    public static IConnectionManager open(ConnectionInfo connectInfo, OkSocketOptions okOptions) {
        return holder.getConnection(connectInfo, okOptions);
    }

    public static IConnectionManager open(String ip, int port, OkSocketOptions okOptions) {
        ConnectionInfo info = new ConnectionInfo(ip, port);
        return holder.getConnection(info, okOptions);
    }
}
