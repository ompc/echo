package com.github.ompc.echo.server.framework.channel;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * 处理器
 * Created by vlinux on 15/2/14.
 */
public class Channels {

    private final List<Channel> channels = new ArrayList<>();

    public void add(UpStreamChannel channel) {
        channels.add(channel);
    }

    public void add(DownStreamChannel channel) {
        channels.add(channel);
    }

    public void fireUpStreamChannels(Object obj, SocketChannel socketChannel) throws Throwable {
        Object resultObj = obj;
        for (Channel channel : channels) {
            if( channel instanceof UpStreamChannel ) {
                try {
                    resultObj = ((UpStreamChannel)channel).read(resultObj, socketChannel);
                    if( null == resultObj ) {
                        break;
                    }
                } catch(Throwable t) {
                    fireExceptionCaught(t, socketChannel);
                }
            }
        }
    }

    public void fireDownStreamChannels(Object obj, SocketChannel socketChannel) throws Throwable {
        Object resultObj = obj;
        for (Channel channel : channels) {
            if( channel instanceof UpStreamChannel ) {
                try {
                    resultObj = ((UpStreamChannel)channel).read(resultObj, socketChannel);
                    if( null == resultObj ) {
                        break;
                    }
                } catch(Throwable t) {
                    fireExceptionCaught(t, socketChannel);
                }
            }
        }
    }

    public void fireExceptionCaught(Throwable t, SocketChannel socketChannel) throws Throwable {
        for (Channel channel : channels) {
            channel.exceptionCaught(t, socketChannel);
        }
    }

}
