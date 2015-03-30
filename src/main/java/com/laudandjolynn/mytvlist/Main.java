package com.laudandjolynn.mytvlist;

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

import com.laudandjolynn.mytvlist.epg.EpgCrawler;
import com.laudandjolynn.mytvlist.epg.EpgService;
import com.laudandjolynn.mytvlist.model.ProgramTable;
import com.laudandjolynn.mytvlist.utils.Config;
import com.laudandjolynn.mytvlist.utils.Constant;
import com.laudandjolynn.mytvlist.utils.DateUtils;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 上午9:12:56
 * @copyright: www.laudandjolynn.com
 */
public class Main {
	private final static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		// 启动应用
		startService();
		InetSocketAddress address = new InetSocketAddress(
				Config.WEB_CONFIG.getIp(), Config.WEB_CONFIG.getPort());
		Server server = new Server(address);
		WebAppContext context = new WebAppContext();
		context.setContextPath("/");
		context.setDescriptor(Main.class.getResource(".").getPath()
				+ "WEB-INF/web.xml");
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
