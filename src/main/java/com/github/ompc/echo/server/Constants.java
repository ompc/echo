package com.github.ompc.echo.server;

import java.util.Scanner;

/**
 * 常量类
 * @author vlinux
 *
 */
public class Constants {

	/**
	 * 缓存大小(默认值)
	 */
	public static final int BUFF_SIZE = 2;


    public static final String LOGO = getLogo();

    /**
     * 展示logo
     *
     * @return
     */
    private static String getLogo() {
        final StringBuilder logoSB = new StringBuilder();
        final Scanner scanner = new Scanner(Object.class.getResourceAsStream("/com/github/ompc/echo/server/res/logo.txt"));
        while (scanner.hasNextLine()) {
            logoSB.append(scanner.nextLine()).append("\n");
        }
        return logoSB.toString();
    }

	
}
