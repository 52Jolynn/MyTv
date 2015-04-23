package com.laudandjolynn.mytv.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jetty.util.ConcurrentHashSet;

import com.laudandjolynn.mytv.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月16日 上午10:58:48
 * @copyright: www.laudandjolynn.com
 */
public class MemoryCache {
	private final static ConcurrentHashSet<TvStation> ALL_TV_STATION = new ConcurrentHashSet<TvStation>();

	private MemoryCache() {
	}

	public static MemoryCache getInstance() {
		return MemoryCacheSingleton.MEMORY_CACHE;
	}

	private final static class MemoryCacheSingleton {
		private final static MemoryCache MEMORY_CACHE = new MemoryCache();
	}

	public Collection<TvStation> getAllCachedTvStation() {
		return new ArrayList<TvStation>(ALL_TV_STATION);
	}

	/**
	 * 缓存所有电视台
	 * 
	 * @param stations
	 */
	public void addCache(List<TvStation> stations) {
		for (TvStation station : stations) {
			ALL_TV_STATION.add(station);
		}
	}

	/**
	 * 缓存电视台
	 * 
	 * @param stations
	 */
	public void addCache(TvStation... stations) {
		for (TvStation station : stations) {
			ALL_TV_STATION.add(station);
		}
	}

	/**
	 * 判断电视台是否已经存在
	 * 
	 * @param station
	 * @return
	 */
	public boolean isStationExists(TvStation station) {
		return ALL_TV_STATION.contains(station);
	}

	/**
	 * 根据名称查找电视台对象
	 * 
	 * @param stationName
	 * @return
	 */
	public TvStation getStation(String stationName) {
		for (TvStation station : ALL_TV_STATION) {
			if (station.getName().equals(stationName)
					&& station.getClassify() == null) {
				return station;
			}
		}
		return null;
	}

	/**
	 * 根据电视台显示名、分类查找电视台对象
	 * 
	 * @param displayName
	 * @param classify
	 * @return
	 */
	public TvStation getStation(String displayName, String classify) {
		if (classify == null) {
			return getStation(displayName);
		}
		for (TvStation station : ALL_TV_STATION) {
			if (station.getDisplayName().equals(displayName)
					&& station.getClassify().equals(classify)) {
				return station;
			}
		}
		return null;
	}
}
