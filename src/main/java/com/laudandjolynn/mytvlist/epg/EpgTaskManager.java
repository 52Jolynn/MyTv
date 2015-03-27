package com.laudandjolynn.mytvlist.epg;

import java.util.List;

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
	public List<ProgramTable> queryProgramTable(String stationName, String date) {
		EpgTask epgTask = new EpgTask(stationName, date);

		return EpgCrawler.crawlProgramTable(stationName, date);
	}
}
