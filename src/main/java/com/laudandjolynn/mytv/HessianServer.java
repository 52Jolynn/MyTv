/*******************************************************************************
 * Copyright 2015 htd0324@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.laudandjolynn.mytv;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytv.utils.Config;
import com.laudandjolynn.mytv.utils.MyTvUtils;

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
		connector.setPort(Config.NET_CONFIG.getHessianPort());
		connector.setHost(Config.NET_CONFIG.getIp());
		// 解决Windows下重复启动Jetty居然不报告端口冲突的问题.
		connector.setReuseAddress(false);
		server.setConnectors(new Connector[] { connector });
		// web配置
		WebAppContext context = new WebAppContext();
		String resourcePath = MyTvUtils.getRunningPath(HessianServer.class);
		logger.info("web app context path: " + resourcePath);
		context.setContextPath("/");
		String descriptor = resourcePath + "/WEB-INF/web.xml";
		logger.info("web app descriptor: " + descriptor);
		context.setDescriptor(descriptor);
		context.setResourceBase(resourcePath);
		context.setParentLoaderPriority(true);
		ClassLoader appClassLoader = Thread.currentThread()
				.getContextClassLoader();
		context.setClassLoader(appClassLoader);

		server.setHandler(context);
		server.start();
	}

}
