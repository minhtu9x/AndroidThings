package com.xuhao.didi.socket.common.interfaces.common_interfacies.client;

import com.xuhao.didi.core.iocore.interfaces.ISendable;


public interface ISender<T> {

    T send(ISendable sendable);
}
