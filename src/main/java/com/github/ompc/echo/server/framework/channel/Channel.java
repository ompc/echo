package com.github.ompc.echo.server.framework.channel;

import java.nio.channels.SocketChannel;

/**
 * Created by vlinux on 15/2/14.
 */
public interface Channel {

    void exceptionCaught(Throwable t, SocketChannel socketChannel) throws Throwable;

}
