package com.github.ompc.echo.server.simple;

import com.github.ompc.echo.server.Config;
import com.github.ompc.echo.server.EchoServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.ompc.echo.server.Constants.BUFF_SIZE;
import static com.github.ompc.echo.server.util.IOUtils.close;
import static com.github.ompc.echo.server.util.LogUtils.info;
import static com.github.ompc.echo.server.util.LogUtils.warn;
import static java.net.StandardSocketOptions.SO_REUSEADDR;
import static java.nio.ByteBuffer.allocate;
import static java.nio.channels.SelectionKey.OP_READ;

/**
 * 服务器状态<br/>
 */
enum State {
    INIT,
    STARTUP
}


/**
 * echo服务器(单线程)
 *
 * @author vlinux
 */
public class SimpleEchoServer implements EchoServer {

    private final AtomicReference<State> stateRef = new AtomicReference<>(State.INIT);

    @Override
    public void startup(final Config cfg) throws IOException {

        if (!stateRef.compareAndSet(State.INIT, State.STARTUP)) {
            throw new IllegalStateException("echo-server was already started.");
        }

        try (final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
             final Selector selector = Selector.open()) {

            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.setOption(SO_REUSEADDR, true);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            // 服务器挂载端口
            serverSocketChannel.bind(new InetSocketAddress(cfg.getPort()), cfg.getBacklog());
            info("echo-server listened on port=%d;", cfg.getPort());

            doSelect(selector);

        }

    }

    /**
     * 服务器accept
     *
     * @param selector
     * @throws IOException
     */
    private void doSelect(final Selector selector) throws IOException {

        while (selector.select() > 0) {
            final Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                final SelectionKey key = it.next();
                it.remove();

                // do ssc accept
                if (key.isValid() && key.isAcceptable()) {
                    final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                    final SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, OP_READ, allocate(BUFF_SIZE));
                    info("simple-echo-server accept an connection, client=%s", socketChannel);
                }

                // do sc read
                if (key.isValid() && key.isReadable()) {

                    final ByteBuffer buff = (ByteBuffer) key.attachment();
                    final SocketChannel socketChannel = (SocketChannel) key.channel();
                    try {

                        final int n = socketChannel.read(buff);

                        // 读出的数据大于0，说明读到了数据
                        if (n > 0) {
                            buff.flip();
                            // 写透为止
                            while (buff.hasRemaining()) {
                                socketChannel.write(buff);
                            }
                            buff.clear();
                        }

                        // 若读到-1，则说明SocketChannel已经关闭
                        else if (n == -1) {
                            info("client=%s was closed.", socketChannel);
                            closeSocketChannel(key, socketChannel);
                        }

                    } catch (IOException e) {
                        warn(e, "read/write data failed, client=%s will be close.", socketChannel);
                        closeSocketChannel(key, socketChannel);
                    }

                }

            }
        }
    }

    private void closeSocketChannel(SelectionKey key, SocketChannel socketChannel) {
        close(socketChannel);
        key.cancel();
    }

}
