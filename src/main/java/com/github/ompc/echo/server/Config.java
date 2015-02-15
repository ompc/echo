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


    /**
     * multi实现下的高性能模式<br/>
     * 启用后selector的实现从select()/wakeup()切换为selectNow()
     */
    private boolean highPerformanceForMulti;

    public boolean isHighPerformanceForMulti() {
        return highPerformanceForMulti;
    }

    public void setHighPerformanceForMulti(boolean highPerformanceForMulti) {
        this.highPerformanceForMulti = highPerformanceForMulti;
    }

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
