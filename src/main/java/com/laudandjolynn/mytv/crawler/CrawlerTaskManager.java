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
package com.laudandjolynn.mytv.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytv.exception.MyTvException;
import com.laudandjolynn.mytv.model.CrawlerTask;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;
import com.laudandjolynn.mytv.service.TvServiceImpl;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月27日 下午11:42:27
 * @copyright: www.laudandjolynn.com
 */
public class CrawlerTaskManager {
	private final static Logger logger = LoggerFactory
			.getLogger(CrawlerTaskManager.class);
	private final ConcurrentHashSet<CrawlerTask> CURRENT_EPG_TASK = new ConcurrentHashSet<CrawlerTask>();
	private final int processor = Runtime.getRuntime().availableProcessors();
	private TvServiceImpl tvService = new TvServiceImpl();

	private CrawlerTaskManager() {
	}

	public static CrawlerTaskManager getIntance() {
		return EpgTaskManagerSingletonHolder.MANAGER;
	}

	private final static class EpgTaskManagerSingletonHolder {
		private final static CrawlerTaskManager MANAGER = new CrawlerTaskManager();
	}

	/**
	 * 抓取所有电视台节目表
	 * 
	 * @param date
	 * @return
	 */
	public List<ProgramTable> queryAllProgramTable(final String date) {
		List<TvStation> stationList = tvService.getAllStation();
		ExecutorService executorService = Executors
				.newFixedThreadPool(processor * 2);
		CompletionService<List<ProgramTable>> completionService = new ExecutorCompletionService<List<ProgramTable>>(
				executorService);
		int size = stationList == null ? 0 : stationList.size();
		for (int i = 0; i < size; i++) {
			TvStation tvStation = stationList.get(i);
			final String stationName = tvStation.getName();
			Callable<List<ProgramTable>> task = new Callable<List<ProgramTable>>() {
				@Override
				public List<ProgramTable> call() throws Exception {
					return queryProgramTable(stationName, date);
				}
			};
			completionService.submit(task);
		}
		int count = 0;
		List<ProgramTable> resultList = new ArrayList<ProgramTable>();
		while (count < size) {
			try {
				Future<List<ProgramTable>> future = completionService.poll(5,
						TimeUnit.MINUTES);
				List<ProgramTable> ptList = future.get(5, TimeUnit.MINUTES);
				if (ptList != null) {
					resultList.addAll(ptList);
				}
			} catch (InterruptedException e) {
				logger.error("craw program table of all station at " + date
						+ " was interrupted.", e);
			} catch (ExecutionException e) {
				logger.error(
						"error occur while craw program table of all station at "
								+ date, e);
			} catch (TimeoutException e) {
				logger.error("query program table of all sation at at " + date
						+ " is timeout.", e);
			}
			count++;
		}
		return resultList;
	}

	/**
	 * 查询电视节目表
	 * 
	 * @param stationName
	 *            电视台名称
	 * @param date
	 *            日期,yyyy-MM-dd
	 * @return
	 */
	public List<ProgramTable> queryProgramTable(String stationName, String date) {
		return queryProgramTable(stationName, null, date);
	}

	/**
	 * 查询电视节目表
	 * 
	 * @param displayName
	 *            电视台显示名
	 * @param classify
	 *            电视台分类，可以为null。为空时，将查找stationName与displayName相同的电视台
	 * @param date
	 *            日期，yyyy-MM-dd
	 * @return
	 */
	public List<ProgramTable> queryProgramTable(String displayName,
			String classify, final String date) {
		TvStation tvStation = tvService.getStationByDisplayName(displayName,
				classify);
		if (tvStation == null) {
			throw new MyTvException(displayName + " isn't exists.");
		}

		final String stationName = tvStation.getName();
		logger.info("query program table of " + stationName + " at " + date);
		if (tvService.isProgramTableExists(stationName, date)) {
			return tvService.getProgramTable(stationName, date);
		}
		CrawlerTask crawlerTask = new CrawlerTask(stationName, date);
		if (CURRENT_EPG_TASK.contains(crawlerTask)) {
			synchronized (this) {
				try {
					logger.debug(crawlerTask
							+ " is wait for the other same task's notification.");
					wait();
				} catch (InterruptedException e) {
					throw new MyTvException(
							"thread interrupted while query program table of "
									+ stationName + " at " + date, e);
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new MyTvException(
							"thread interrupted while query program table of "
									+ stationName + " at " + date, e);
				}

				logger.debug(crawlerTask
						+ " has receive notification and try to get program table from db.");
				return tvService.getProgramTable(stationName, date);
			}
		}

		logger.debug(crawlerTask
				+ " is try to query program table from network.");
		CURRENT_EPG_TASK.add(crawlerTask);
		try {
			return tvService.crawlProgramTable(stationName, date);
		} finally {
			synchronized (this) {
				CURRENT_EPG_TASK.remove(crawlerTask);
				logger.debug(crawlerTask
						+ " have finished to get program table data and send notification.");
				notifyAll();
			}
		}
	}
}
