package com.laudandjolynn.mytvlist;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytvlist.epg.EpgCrawler;
import com.laudandjolynn.mytvlist.utils.Utils;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 上午9:12:56
 * @copyright: www.laudandjolynn.com
 */
public class MyTvList {
	private final static Logger logger = LoggerFactory
			.getLogger(MyTvList.class);
	private final static long DAY_MILLIS = 86400000;

	public static void main(String[] args) {
		logger.info("start My TV Program Table Crawler.");
		// 启动应用
		startService();
		// 启动每天定时任务
		createEverydayCron();
		logger.info("My TV Program Table Crawler is running.");
		while (true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO 处理中断
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
		scheduled.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				EpgCrawler.crawlAllProgramTable(Utils.today());
			}
		}, 0, DAY_MILLIS, TimeUnit.DAYS);
	}
}
