package com.github.ompc.echo.server;

import com.github.ompc.echo.server.simple.SimpleEchoServer;

import java.io.IOException;

/**
 * 启动器
 *
 * @author vlinux
 */
public class AppLauncher {

    public static void main(String... args) throws IOException {
        final Config cfg = new Config();
        cfg.setBacklog(32);
        cfg.setConnectTimeoutSec(10);
        cfg.setPort(7341);
        final EchoServer server = new SimpleEchoServer();
        server.startup(cfg);

    }

}
