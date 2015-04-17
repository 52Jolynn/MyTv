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
package com.laudandjolynn.mytv.service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytv.exception.MyTvException;
import com.laudandjolynn.mytv.model.CrawlerTask;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月27日 下午11:42:27
 * @copyright: www.laudandjolynn.com
 */
public class TvTaskManager {
	private final static Logger logger = LoggerFactory
			.getLogger(TvTaskManager.class);
	private final ConcurrentHashSet<CrawlerTask> CURRENT_EPG_TASK = new ConcurrentHashSet<CrawlerTask>();
	private final int processor = Runtime.getRuntime().availableProcessors();
	private final ExecutorService executorService = Executors
			.newFixedThreadPool(processor * 2);
	private TvServiceImpl tvService = new TvServiceImpl();

	private TvTaskManager() {
	}

	public static TvTaskManager getIntance() {
		return EpgTaskManagerSingletonHolder.MANAGER;
	}

	private final static class EpgTaskManagerSingletonHolder {
		private final static TvTaskManager MANAGER = new TvTaskManager();
	}

	/**
	 * 查询电视节目表
	 * 
	 * @param stationOrDisplayName
	 *            电视台名或显示名
	 * @param classify
	 *            电视台分类
	 * @param date
	 *            日期，yyyy-MM-dd
	 * @return
	 */
	public List<ProgramTable> queryProgramTable(String stationOrDisplayName,
			String classify, final String date) {
		TvStation tvStation = tvService.getStation(stationOrDisplayName);
		if (tvStation == null) {
			tvStation = tvService.getStationByDisplayName(stationOrDisplayName,
					classify);
		}
		if (tvStation == null) {
			throw new MyTvException(stationOrDisplayName + " isn't exists.");
		}

		final String stationName = tvStation.getName();
		logger.info("query program table of " + stationName + " at " + date);
		if (tvService.isProgramTableExists(stationName, date)) {
			return tvService.getProgramTable(stationName, date);
		}
		CrawlerTask epgTask = new CrawlerTask(stationName, date);
		if (CURRENT_EPG_TASK.contains(epgTask)) {
			synchronized (this) {
				try {
					logger.debug(epgTask
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

				logger.debug(epgTask
						+ " has receive notification and try to get program table from db.");
				return tvService.getProgramTable(stationName, date);
			}
		}

		logger.debug(epgTask + " is try to query program table from network.");
		CURRENT_EPG_TASK.add(epgTask);
		Callable<List<ProgramTable>> callable = new Callable<List<ProgramTable>>() {

			@Override
			public List<ProgramTable> call() throws Exception {
				return tvService.crawlProgramTable(stationName, date);
			}
		};
		Future<List<ProgramTable>> future = executorService.submit(callable);
		try {
			return future.get();
		} catch (InterruptedException e) {
			throw new MyTvException(
					"thread interrupted while query program table of "
							+ stationName + " at " + date, e);
		} catch (ExecutionException e) {
			throw new MyTvException("error occur while query program table of "
					+ stationName + " at " + date + ".", e);
		} finally {
			synchronized (this) {
				CURRENT_EPG_TASK.remove(epgTask);
				logger.debug(epgTask
						+ " have finished to get program table data and send notification.");
				notifyAll();
			}
		}
	}
}
