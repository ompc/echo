package com.github.ompc.echo.server;

/**
 * 常量类
 * @author vlinux
 *
 */
public class Constants {

	/**
	 * 缓存大小(默认值)
	 */
	public static final int DEFAULT_BUFF_SIZE = 16;

    /**
     * 默认TCP_BACKLOG大小
     */
    public static final int DEFAULT_TCP_BACKLOG = 128;

    /**
     * 默认TCP超时时间(秒)
     */
    public static final int DEFAULT_TCP_TIMEOUT = 10;

    /**
     * 默认Echo服务器端口
     */
    public static final int DEFAULT_PORT = 3333;

    /**
     * 默认Echo服务器网卡
     */
    public static final String DEFAULT_NETWORK_INTERFACE = "localhost";

    /**
     * Echo服务器的运行模式(Simple)
     */
    public static final String RUNTIME_SIMPLE = "simple";

    /**
     * Echo服务器的运行模式(Multi)
     */
    public static final String RUNTIME_MULTI = "multi";

    /**
     * 默认日志路径
     */
    public static final String DEFAULT_LOG_PATH = "./echo.log";

}
