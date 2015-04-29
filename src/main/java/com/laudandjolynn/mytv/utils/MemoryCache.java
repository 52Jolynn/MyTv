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
package com.laudandjolynn.mytv.utils;

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
	 * 根据名称查找电视台对象
	 * 
	 * @param stationName
	 * @return
	 */
	public TvStation getStation(String stationName) {
		for (TvStation station : ALL_TV_STATION) {
			if (station.getName().equals(stationName)) {
				return station;
			}
		}
		return null;
	}

}
