package com.github.ompc.echo.server;

import com.github.ompc.echo.server.multie.MultiEchoServer;
import com.github.ompc.echo.server.simple.SimpleEchoServer;

import java.io.IOException;

/**
 * 启动器
 *
 * @author vlinux
 */
public class AppLauncher {

    private static boolean isMulti(String[] args) {
        if (null != args
                && args.length >= 1
                && null != args[0]) {
            return args[0].equalsIgnoreCase("multi");
        }

        return false;
    }

    public static void main(String... args) throws IOException {


        final Config cfg = new Config();
        cfg.setBacklog(32);
        cfg.setConnectTimeoutSec(10);
        cfg.setPort(7341);
        cfg.setHighPerformanceForMulti(false);


        final EchoServer server = isMulti(args)
                ? new MultiEchoServer(Runtime.getRuntime().availableProcessors())
                : new SimpleEchoServer();
        server.startup(cfg);

    }

}
