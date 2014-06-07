package com.github.ompc.echo.server;

import static com.github.ompc.echo.server.Constants.BUFF_SIZE;
import static java.lang.String.format;
import static java.net.StandardSocketOptions.SO_REUSEADDR;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * echo服务器
 * @author vlinux
 *
 */
public class EchoServer {

	private final Logger logger = Logger.getAnonymousLogger();
	
	/*
	 * 配置参数
	 */
	private final Configer cfg;
	
	/**
	 * 构造echo服务器
	 * @param cfg 配置参数
	 */
	public EchoServer(Configer cfg) {
		this.cfg = cfg;
	}
	
	/**
	 * 启动echo服务器
	 * @throws IOException 
	 */
	public void startup() throws IOException {
		
		try(final ServerSocketChannel ssc = ServerSocketChannel.open();
			final Selector sel = Selector.open();) {
			
			ssc.configureBlocking(false);
			ssc.setOption(SO_REUSEADDR, true);
			ssc.register(sel, SelectionKey.OP_ACCEPT);
			
			// 服务器挂载端口
			ssc.bind(new InetSocketAddress(cfg.getPort()), cfg.getBacklog());
			logger.info(format("echo-server listened on port=%d;", cfg.getPort()));
			
			doSelect(sel);
			
		}
		
	}
	
	/**
	 * 服务器accept
	 * @param sel
	 * @throws IOException
	 */
	private void doSelect(final Selector sel) throws IOException {
		while( sel.select() > 0 ) {
			final Iterator<SelectionKey> it = sel.selectedKeys().iterator();
			while( it.hasNext() ) {
				final SelectionKey key = it.next();
				it.remove();
				
				// do ssc accept
				if( key.isAcceptable() ) {
					final ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
					final SocketChannel sc = ssc.accept();
					sc.configureBlocking(false);
					sc.register(sel, SelectionKey.OP_READ);
					final InetSocketAddress isa = (InetSocketAddress)sc.getRemoteAddress();
					logger.info(format("echo-server accept an connection client=%s:%s;", isa.getHostName(), isa.getPort()));
				}
				
				// do sc read
				else if( key.isReadable() ) {
					final SocketChannel sc = (SocketChannel)key.channel();
					try {
						if( echo(sc) < 0 ) {
							// EOF
							final InetSocketAddress isa = (InetSocketAddress)sc.getRemoteAddress();
							logger.log(Level.INFO, format("client=%s:%s closed.",isa.getHostName(), isa.getPort()));
							sc.close();
						}
					} catch(IOException e) {
						final InetSocketAddress isa = (InetSocketAddress)sc.getRemoteAddress();
						logger.log(Level.WARNING, format("echo datas failed, client=%s:%s will be close.",isa.getHostName(), isa.getPort()),e);
						sc.close();
					}
					
				}
				
				else {
					// do nothing...
				}
				
			}
		}
	}
	
	/**
	 * echo
	 * @param sc
	 * @return
	 * @throws IOException
	 */
	private int echo(final SocketChannel sc) throws IOException {
		final ByteBuffer buff = ByteBuffer.allocate(BUFF_SIZE);
		int length = 0;
		while( (length = sc.read(buff)) > 0 ) {
			buff.flip();
			sc.write(buff);
		}
		return length;
	}
	
}
