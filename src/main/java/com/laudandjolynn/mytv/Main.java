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

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytv.epg.EpgCrawler;
import com.laudandjolynn.mytv.epg.EpgService;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.utils.Config;
import com.laudandjolynn.mytv.utils.Constant;
import com.laudandjolynn.mytv.utils.DateUtils;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 上午9:12:56
 * @copyright: www.laudandjolynn.com
 */
public class Main {
	private final static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		// 初始化应用状态、启动内部任务
		startService();
		// 启动web应用
		InetSocketAddress address = new InetSocketAddress(
				Config.WEB_CONFIG.getIp(), Config.WEB_CONFIG.getPort());
		Server server = new Server(address);
		String resourcePath = Main.class.getResource("/").getPath() + "webapp";
		logger.info("web app context path: " + resourcePath);
		WebAppContext context = new WebAppContext();
		context.setContextPath("/");
		String descriptor = resourcePath + "/WEB-INF/web.xml";
		logger.info("web app descriptor: " + descriptor);
		context.setDescriptor(descriptor);
		context.setResourceBase(resourcePath);
		context.setParentLoaderPriority(true);
		server.setHandler(context);
		server.start();
		server.join();
		logger.info("My TV Program Table Crawler is running.");
	}

	/**
	 * 启动应用
	 */
	private static void startService() {
		logger.info("start My TV Program Table Crawler.");
		Init.getIntance().init();
		// 启动每天定时任务
		logger.info("create everyday crawl task.");
		createEverydayCron();
	}

	/**
	 * 创建每天定时任务
	 */
	private static void createEverydayCron() {
		ScheduledExecutorService scheduled = new ScheduledThreadPoolExecutor(1);
		long initDelay = DateUtils.string2Date(
				DateUtils.tommorow() + " 00:00:00").getTime()
				- new Date().getTime();
		scheduled.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				String today = DateUtils.today();
				List<ProgramTable> ptList = EpgCrawler
						.crawlAllProgramTable(today);
				ProgramTable[] ptArray = new ProgramTable[ptList.size()];
				EpgService.save(ptList.toArray(ptArray));
				MyTvData.getInstance().writeData(Constant.PROGRAM_TABLE_DATES,
						Constant.PROGRAM_TABLE_DATE, today);
			}
		}, initDelay, 1, TimeUnit.DAYS);
	}
}
