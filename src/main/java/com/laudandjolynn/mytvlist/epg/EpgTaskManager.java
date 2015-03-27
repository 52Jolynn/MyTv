package com.laudandjolynn.mytvlist.epg;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.jetty.util.ConcurrentHashSet;

import com.laudandjolynn.mytvlist.model.EpgTask;
import com.laudandjolynn.mytvlist.model.ProgramTable;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月27日 下午11:42:27
 * @copyright: www.laudandjolynn.com
 */
public class EpgTaskManager {
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
//		EpgTask epgTask = new EpgTask(stationName, date);
//		synchronized (this) {
//			if (CURRENT_EPG_TASK.contains(epgTask)) {
//				try {
//					wait();
//				} catch (InterruptedException e) {
//					// TODO 处理中断
//				}
//			}
//		}
//		try {
//			Thread.sleep(10);
//		} catch (InterruptedException e) {
//			// TODO 处理中断
//		}
//
//		Callable<List<ProgramTable>> callable = new Callable<List<ProgramTable>>() {
//
//			@Override
//			public List<ProgramTable> call() throws Exception {
//				return EpgCrawler.crawlProgramTable(stationName, date);
//			}
//		};
//		Future<List<ProgramTable>> future = executorService.submit(callable);
//		try {
//			return future.get();
//		} catch (InterruptedException e) {
//			// TODO
//		} catch (ExecutionException e) {
//			// TODO
//		}
		return null;
	}
}
