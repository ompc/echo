package com.github.ompc.echo.server;

import java.io.IOException;

/**
 * 启动器
 * @author vlinux
 *
 */
public class AppLauncher {

	public static void main(String... args) throws IOException {
		final Configer cfg = new Configer();
		cfg.setBacklog(32);
		cfg.setConnectTimeoutSec(10);
		cfg.setPort(7341);
		final EchoServer server = new EchoServer(cfg);
		server.startup();
		
	}
	
}
