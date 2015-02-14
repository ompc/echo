package com.github.ompc.echo.server.framework.channel;

import java.nio.channels.SocketChannel;

/**
 * 下行处理
 * Created by vlinux on 15/2/14.
 */
public interface DownStreamChannel extends Channel {

    Object write(Object obj, SocketChannel socketChannel) throws Throwable;

}
