package com.laudandjolynn.mytvlist.epg;

import java.util.ArrayList;
import java.util.List;

import com.laudandjolynn.mytvlist.Init;
import com.laudandjolynn.mytvlist.model.ProgramTable;
import com.laudandjolynn.mytvlist.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月30日 下午3:05:47
 * @copyright: www.laudandjolynn.com
 */
public class EpgService {
	/**
	 * 保存电视台
	 * 
	 * @param stations
	 */
	public static void save(TvStation... stations) {
		int size = stations == null ? 0 : stations.length;
		if (size == 0) {
			return;
		}
		List<TvStation> resultList = new ArrayList<TvStation>();
		for (int i = 0; i < size; i++) {
			TvStation station = stations[i];
			String stationName = station.getName();
			if (isStationExists(stationName)) {
				continue;
			}
			resultList.add(station);
		}
		stations = new TvStation[resultList.size()];
		EpgDao.save(stations);
	}

	/**
	 * 判断电视台是否存在
	 * 
	 * @param stationName
	 * @return
	 */
	private static boolean isStationExists(String stationName) {
		return Init.getIntance().isStationExists(stationName)
				|| EpgDao.isStationExists(stationName);
	}

	/**
	 * 保存电视台节目表
	 * 
	 * @param programTables
	 */
	public static void save(ProgramTable... programTables) {
		int size = programTables == null ? 0 : programTables.length;
		if (size == 0) {
			return;
		}
		List<ProgramTable> resultList = new ArrayList<ProgramTable>();
		for (int i = 0; i < size; i++) {
			ProgramTable pt = programTables[i];
			String stationName = pt.getStationName();
			String date = pt.getAirTime();
			if (!isStationExists(stationName)
					|| EpgDao.isProgramTableExists(stationName, date)) {
				continue;
			}
			resultList.add(pt);
		}
		programTables = new ProgramTable[resultList.size()];
		EpgDao.save(programTables);
	}

	/**
	 * 获取电视台分类
	 * 
	 * @return
	 */
	public static List<String> getTvStationClassify() {
		return EpgDao.getTvStationClassify();
	}

	/**
	 * 获取所有电视台列表
	 * 
	 * @return
	 */
	public static List<TvStation> getAllStation() {
		return EpgDao.getAllStation();
	}
}
