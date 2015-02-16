package com.github.ompc.echo.server;

import com.github.ompc.echo.server.multie.MultiEchoServer;
import com.github.ompc.echo.server.simple.SimpleEchoServer;
import com.github.ompc.echo.server.util.LogUtils;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.IOException;

import static com.github.ompc.echo.server.Constants.*;

/**
 * 启动器
 *
 * @author vlinux
 */
public class AppLauncher {

    public static void main(String... args) throws IOException {


        // 参数解析
        final OptionParser parser = new OptionParser();
        parser.accepts("buffer").withOptionalArg().ofType(int.class).defaultsTo(DEFAULT_BUFF_SIZE);
        parser.accepts("backlog").withOptionalArg().ofType(int.class).defaultsTo(DEFAULT_TCP_BACKLOG);
        parser.accepts("timeout").withOptionalArg().ofType(int.class).defaultsTo(DEFAULT_TCP_TIMEOUT);
        parser.accepts("runtime").withOptionalArg().ofType(String.class).defaultsTo(RUNTIME_SIMPLE);
        parser.accepts("log").withOptionalArg().ofType(String.class).defaultsTo(DEFAULT_LOG_PATH);
        parser.accepts("port").withOptionalArg().ofType(int.class).defaultsTo(DEFAULT_PORT);
        parser.accepts("network").withOptionalArg().ofType(String.class).defaultsTo(DEFAULT_NETWORK_INTERFACE);
        parser.accepts("high");


        final Config cfg = new Config();
        final OptionSet os = parser.parse(args);

        cfg.setBufferSize((int) os.valueOf("buffer"));
        cfg.setBacklog((int) os.valueOf("backlog"));
        cfg.setPort((int) os.valueOf("port"));
        cfg.setConnectTimeoutSec((int) os.valueOf("timeout"));
        cfg.setHighPerformanceForMulti(os.has("high"));
        cfg.setNetworkInterface((String)os.valueOf("network"));

        if(os.has("log")) {
            LogUtils.setLogPath((String)os.valueOf("log"));
        }

        final EchoServer echoServer;
        switch ((String) os.valueOf("runtime")) {

            case RUNTIME_MULTI: {
                echoServer = new MultiEchoServer(Runtime.getRuntime().availableProcessors());
                break;
            }

            case RUNTIME_SIMPLE: {
                echoServer = new SimpleEchoServer();
                break;
            }

            default: {
                throw new IllegalArgumentException("illegal arguments [runtime]");
            }

        }//switch

        // 服务器启动
        echoServer.startup(cfg);

    }

}
