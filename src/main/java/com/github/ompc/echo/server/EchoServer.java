package com.github.ompc.echo.server;

import java.io.IOException;

/**
 * EchoServer接口
 * Created by vlinux on 15/2/14.
 */
public interface EchoServer {

    /**
     * 启动服务器(阻塞)
     *
     * @param cfg 服务器参数配置
     * @throws IOException
     */
    void startup(Config cfg) throws IOException;

}
