package com.laudandjolynn.mytv;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytv.utils.Config;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月8日 上午9:59:56
 * @copyright: www.laudandjolynn.com
 */
public class HessianServer implements com.laudandjolynn.mytv.Server {
	private final static Logger logger = LoggerFactory
			.getLogger(HessianServer.class);

	@Override
	public void start() throws Exception {
		// 启动web应用
		org.eclipse.jetty.server.Server server = new Server();
		server.setStopAtShutdown(true);
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(Config.WEB_CONFIG.getPort());
		connector.setHost(Config.WEB_CONFIG.getIp());
		// 解决Windows下重复启动Jetty居然不报告端口冲突的问题.
		connector.setReuseAddress(false);
		server.setConnectors(new Connector[] { connector });

		// web配置
		WebAppContext context = new WebAppContext();
		String resourcePath = Main.class.getResource("/").getPath();
		logger.info("web app context path: " + resourcePath);
		context.setContextPath("/");
		String descriptor = resourcePath + "WEB-INF/web.xml";
		logger.info("web app descriptor: " + descriptor);
		context.setDescriptor(descriptor);
		context.setResourceBase(resourcePath);
		context.setParentLoaderPriority(true);
		ClassLoader appClassLoader = Thread.currentThread()
				.getContextClassLoader();
		context.setClassLoader(appClassLoader);

		server.setHandler(context);
		server.start();
		server.join();
	}

}
