package com.github.ompc.echo.server.framework.channel;

import java.nio.channels.SocketChannel;

/**
 * 上行处理
 * Created by vlinux on 15/2/14.
 */
public interface UpStreamChannel extends Channel {

    Object read(Object obj, SocketChannel socketChannel) throws Throwable;

}
