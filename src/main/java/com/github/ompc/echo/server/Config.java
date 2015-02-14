package com.github.ompc.echo.server;

/**
 * 配置信息
 * @author vlinux
 *
 */
public class Config {

	/*
	 * backlog
	 */
	private int backlog;
	
	/*
	 * 连接超时(秒)
	 */
	private int connectTimeoutSec;
	
	/*
	 * 服务端口号
	 */
	private int port;
	
	public int getBacklog() {
		return backlog;
	}

	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	public int getConnectTimeoutSec() {
		return connectTimeoutSec;
	}

	public void setConnectTimeoutSec(int connectTimeoutSec) {
		this.connectTimeoutSec = connectTimeoutSec;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}
