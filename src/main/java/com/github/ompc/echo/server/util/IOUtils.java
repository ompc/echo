package com.github.ompc.echo.server.util;

import java.io.Closeable;

import static com.github.ompc.echo.server.util.LogUtils.info;

/**
 * IO工具类
 * Created by vlinux on 15/2/14.
 */
public class IOUtils {

    /**
     * 关闭一个实现了Closeable接口的对象
     *
     * @param closeable 待关闭的对象
     */
    public static void close(Closeable closeable) {

        if (null != closeable) {
            try {
                closeable.close();
            } catch (Throwable t) {
                info(t, "close failed. closeable=%s", closeable);
            }
        }

    }

}
