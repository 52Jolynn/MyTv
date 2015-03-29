package com.laudandjolynn.mytvlist;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytvlist.epg.EpgCrawler;
import com.laudandjolynn.mytvlist.utils.DateUtils;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 上午9:12:56
 * @copyright: www.laudandjolynn.com
 */
public class Main {
	private final static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		logger.info("start My TV Program Table Crawler.");
		// 启动应用
		startService();
		// 启动每天定时任务
		logger.info("create everyday crawl task.");
		createEverydayCron();
		logger.info("My TV Program Table Crawler is running.");
		while (true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error("My TV Program has stop.", e);
			}
		}
	}

	/**
	 * 启动应用
	 */
	private static void startService() {
		Init.getIntance().init();
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
				EpgCrawler.crawlAllProgramTable(DateUtils.today());
			}
		}, initDelay, 1, TimeUnit.DAYS);
	}
}
