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
package com.laudandjolynn.mytv.epg;

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
import com.laudandjolynn.mytv.model.EpgTask;
import com.laudandjolynn.mytv.model.ProgramTable;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月27日 下午11:42:27
 * @copyright: www.laudandjolynn.com
 */
public class EpgTaskManager {
	private final static Logger logger = LoggerFactory
			.getLogger(EpgTaskManager.class);
	private final ConcurrentHashSet<EpgTask> CURRENT_EPG_TASK = new ConcurrentHashSet<EpgTask>();
	private final int processor = Runtime.getRuntime().availableProcessors();
	private final ExecutorService executorService = Executors
			.newFixedThreadPool(processor * 2);

	private EpgTaskManager() {
	}

	public static EpgTaskManager getIntance() {
		return EpgTaskManagerSingltonHolder.MANAGER;
	}

	private final static class EpgTaskManagerSingltonHolder {
		private final static EpgTaskManager MANAGER = new EpgTaskManager();
	}

	/**
	 * 查询电视节目表
	 * 
	 * @param stationName
	 *            电视台名称
	 * @param date
	 *            日期，yyyy-MM-dd
	 * @return
	 */
	public List<ProgramTable> queryProgramTable(final String stationName,
			final String date) {
		logger.info("query program table of " + stationName + " at " + date);
		if (EpgDao.isProgramTableExists(stationName, date)) {
			return EpgService.getProgramTable(stationName, date);
		}
		EpgTask epgTask = new EpgTask(stationName, date);
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
				return EpgService.getProgramTable(stationName, date);
			}
		}

		logger.debug(epgTask + " is try to query program table from network.");
		CURRENT_EPG_TASK.add(epgTask);
		Callable<List<ProgramTable>> callable = new Callable<List<ProgramTable>>() {

			@Override
			public List<ProgramTable> call() throws Exception {
				return EpgCrawler.crawlProgramTable(stationName, date);
			}
		};
		Future<List<ProgramTable>> future = executorService.submit(callable);
		try {
			future.get();
		} catch (InterruptedException e) {
			throw new MyTvException(
					"thread interrupted while query program table of "
							+ stationName + " at " + date, e);
		} catch (ExecutionException e) {
			throw new MyTvException("error occur while query program table of "
					+ stationName + " at " + date + ".", e);
		}
		synchronized (this) {
			CURRENT_EPG_TASK.remove(epgTask);
			logger.debug(epgTask
					+ " have finished to get program table data and send notification.");
			notifyAll();
		}
		return EpgService.getProgramTable(stationName, date);
	}
}
