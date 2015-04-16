package com.laudandjolynn.mytv.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.laudandjolynn.mytv.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月16日 上午10:58:48
 * @copyright: www.laudandjolynn.com
 */
public class MemoryCache {
	private final static Map<String, TvStation> ALL_TV_STATION = new HashMap<String, TvStation>();

	private MemoryCache() {
	}

	public static MemoryCache getInstance() {
		return MemoryCacheSingleton.MEMORY_CACHE;
	}

	private final static class MemoryCacheSingleton {
		private final static MemoryCache MEMORY_CACHE = new MemoryCache();
	}

	public Collection<TvStation> getAllCacheTvStation() {
		return ALL_TV_STATION.values();
	}

	/**
	 * 缓存所有电视台
	 * 
	 * @param stations
	 */
	public void addCache(List<TvStation> stations) {
		for (TvStation station : stations) {
			ALL_TV_STATION.put(station.getName(), station);
		}
	}

	/**
	 * 缓存电视台
	 * 
	 * @param stations
	 */
	public void addCache(TvStation... stations) {
		for (TvStation station : stations) {
			ALL_TV_STATION.put(station.getName(), station);
		}
	}

	/**
	 * 判断电视台是否已经存在
	 * 
	 * @param stationName
	 * @return
	 */
	public boolean isStationExists(String stationName) {
		return ALL_TV_STATION.containsKey(stationName);
	}

	/**
	 * 根据名称获取电视台對象
	 * 
	 * @param stationName
	 * @return
	 */
	public TvStation getStation(String stationName) {
		return ALL_TV_STATION.get(stationName);
	}
}
