package com.github.ompc.echo.server.multie;

import com.github.ompc.echo.server.Config;
import com.github.ompc.echo.server.EchoServer;
import com.github.ompc.echo.server.util.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.ompc.echo.server.multie.State.INIT;
import static com.github.ompc.echo.server.multie.State.STARTUP;
import static com.github.ompc.echo.server.util.IOUtils.close;
import static com.github.ompc.echo.server.util.LogUtils.info;
import static com.github.ompc.echo.server.util.LogUtils.warn;
import static java.net.StandardSocketOptions.SO_REUSEADDR;
import static java.nio.ByteBuffer.allocate;
import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.util.Arrays.asList;

/**
 * 服务器状态<br/>
 */
enum State {
    INIT,
    STARTUP
}

/**
 * Selector集合封装
 */
class Selectors implements Closeable {

    private final Selector[] selectors;
    private final int size;
    private int index = 0;

    public Selectors(int size) throws IOException {
        this.size = size;
        this.selectors = new Selector[size];
        try {
            for (int i = 0; i < size; i++) {
                selectors[i] = Selector.open();
            }
        } catch (IOException e) {
            asList(selectors).forEach((s) -> IOUtils.close(s));
            throw e;
        }

    }

    public Selector next() {
        return selectors[Math.abs(index++ % size)];
    }

    @Override
    public void close() throws IOException {
        asList(selectors).forEach((s) -> IOUtils.close(s));
    }
}

/**
 * echo服务器(多线程)
 * Created by vlinux on 15/2/14.
 */
public class MultiEchoServer implements EchoServer {

    private final AtomicReference<State> stateRef = new AtomicReference<>(INIT);
    private final ExecutorService workerPool;
    private final int workerSize;

    // 注册队列
    private Queue<SocketChannel> registerQueue = new ConcurrentLinkedQueue<>();

    private int workerIndex = 0;

    public MultiEchoServer(final int workerSize) {
        this.workerSize = workerSize;
        workerPool = Executors.newFixedThreadPool(workerSize, (r) -> new Thread(r, "ECHO-IO-WORKER-" + workerIndex++));

    }


    @Override
    public void startup(final Config cfg) throws IOException {

        if (!stateRef.compareAndSet(INIT, STARTUP)) {
            throw new IllegalStateException("echo-server was already started.");
        }

        try (final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
             final Selector accepterSelector = Selector.open();
             final Selectors workerSelectors = new Selectors(workerSize)) {

            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().setSoTimeout(cfg.getConnectTimeoutSec());
            serverSocketChannel.setOption(SO_REUSEADDR, true);
            serverSocketChannel.register(accepterSelector, OP_ACCEPT);

            // 服务器挂载端口
            serverSocketChannel.bind(new InetSocketAddress(cfg.getNetworkInterface(), cfg.getPort()), cfg.getBacklog());
            info("multi-echo-server listened on network=%s;port=%d;backlog=%d;high=%s;timeout=%d;buffer=%d;",
                    cfg.getNetworkInterface(),
                    cfg.getPort(),
                    cfg.getBacklog(),
                    cfg.isHighPerformanceForMulti(),
                    cfg.getConnectTimeoutSec(),
                    cfg.getBufferSize());

            startupWorker(cfg, workerSelectors);
            doAccepter(cfg, accepterSelector, workerSelectors);

        }

    }

    /**
     * 启动工作线程
     *
     * @param cfg             config
     * @param workerSelectors workerSelectors
     */
    private void startupWorker(final Config cfg, final Selectors workerSelectors) {
        for (int i = 0; i < workerSize; i++) {
            final Selector workerSelector = workerSelectors.next();
            workerPool.execute(() -> {
                for (; ; ) {
                    try {
                        doWorker(cfg, workerSelector);
                    } catch (Throwable t) {
                        warn(t, "worker failed.");
                    }
                }
            });
        }
    }

    private void doAccepter(final Config cfg, final Selector selector, final Selectors workerSelectors) throws IOException {

        while (selector.select() > 0) {
            final Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                final SelectionKey key = it.next();
                it.remove();

                // do ssc accept
                if (key.isValid() && key.isAcceptable()) {
                    final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                    final SocketChannel workerSocketChannel = serverSocketChannel.accept();
                    info("echo-server accept an connection, client=%s", workerSocketChannel);

                    // 新来的SocketChannel需要注册到selector才能工作，但只有占用到selector的锁才能被register
                    // 所以这里必须先暂存到队列中，然后唤醒其中一个selector，让其在select()之后完成注册
                    registerQueue.offer(workerSocketChannel);

                    // 高性能模式实现
                    if (!cfg.isHighPerformanceForMulti()) {
                        workerSelectors.next().wakeup();
                    }


                }

            }
        }

    }

    private void doWorker(final Config cfg, final Selector workerSelector) throws IOException {

        // 高性能模式实现
        if (cfg.isHighPerformanceForMulti()) {
            workerSelector.selectNow();
        } else {
            workerSelector.select();
        }


        // 待select()之后拿到锁，此时可以完成注册动作
        while (!registerQueue.isEmpty()) {
            final SocketChannel workerSocketChannel = registerQueue.poll();

            if (null == workerSocketChannel) {
                warn("worker take socketChannel from registerQueue was null, impossible!");
            }

            // 这里和子澄在压测的时候，极端情况下出现了一个空对象的返回
            else {
                workerSocketChannel.configureBlocking(false);
                workerSocketChannel.socket().setSoTimeout(cfg.getConnectTimeoutSec());
                workerSocketChannel.register(workerSelector, OP_READ, allocate(cfg.getBufferSize()));
            }


        }

        final Iterator<SelectionKey> workerIt = workerSelector.selectedKeys().iterator();
        while (workerIt.hasNext()) {
            final SelectionKey workerKey = workerIt.next();
            workerIt.remove();

            final SocketChannel workerSocketChannel = (SocketChannel) workerKey.channel();

            // do sc read
            if (workerKey.isValid() && workerKey.isReadable()) {

                final ByteBuffer buff = (ByteBuffer) workerKey.attachment();
                try {

                    final int n = workerSocketChannel.read(buff);

                    // 读出的数据大于0，说明读到了数据
                    if (n > 0) {
                        buff.flip();
                        // 写透为止
                        while (buff.hasRemaining()) {
                            workerSocketChannel.write(buff);
                        }
                        buff.clear();
                    }

                    // 若读到-1，则说明SocketChannel已经关闭
                    else if (n == -1) {
                        info("client=%s was closed.", workerSocketChannel);
                        closeSocketChannel(workerKey, workerSocketChannel);
                    }

                } catch (IOException e) {
                    warn(e, "read/write data failed, client=%s will be close.", workerSocketChannel);
                    closeSocketChannel(workerKey, workerSocketChannel);
                }

            }

        }

    }

    private void closeSocketChannel(SelectionKey key, SocketChannel socketChannel) {
        close(socketChannel);
        key.cancel();
    }

}