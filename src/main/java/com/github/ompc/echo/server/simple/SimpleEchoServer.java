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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.ompc.echo.server.Constants.BUFF_SIZE;
import static com.github.ompc.echo.server.Constants.LOGO;
import static com.github.ompc.echo.server.util.IOUtils.close;
import static com.github.ompc.echo.server.util.LogUtils.info;
import static com.github.ompc.echo.server.util.LogUtils.warn;
import static java.net.StandardSocketOptions.SO_REUSEADDR;
import static java.nio.ByteBuffer.allocate;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

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

        final ByteBuffer heartBeatBuffer = ByteBuffer.allocate(1);
        heartBeatBuffer.put((byte) 0);
        heartBeatBuffer.flip();

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
                    socketChannel.register(selector, OP_READ | OP_WRITE, allocate(BUFF_SIZE));
                    info("echo-server accept an connection, client=%s", socketChannel);
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

                // do welcome write
                if (key.isValid() && key.isWritable()) {

                    key.interestOps(key.interestOps() & ~OP_WRITE);
                    final ByteBuffer writeBuff = ByteBuffer.wrap(welcome().getBytes("UTF-8"));
                    final SocketChannel socketChannel = (SocketChannel) key.channel();

                    try {
                        while (writeBuff.hasRemaining()) {
                            socketChannel.write(writeBuff);
                        }
                    } catch(IOException e) {
                        warn(e, "write data failed, client=%s will be close.", socketChannel);
                        closeSocketChannel(key, socketChannel);
                    }

                }

            }
        }
    }

    private String welcome() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return LOGO + "\n" + "Welcome to ECHO-SERVER(version:1.0.0), Today is " + sdf.format(new Date()) + "\n\n";
    }

    private void closeSocketChannel(SelectionKey key, SocketChannel socketChannel) {
        close(socketChannel);
        key.cancel();
    }

}
