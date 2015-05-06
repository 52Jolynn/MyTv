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

import java.util.ArrayList;
import java.util.List;

import com.laudandjolynn.mytv.crawler.Crawler;
import com.laudandjolynn.mytv.crawler.MyTvCrawlerManager;
import com.laudandjolynn.mytv.datasource.TvDao;
import com.laudandjolynn.mytv.datasource.TvDaoImpl;
import com.laudandjolynn.mytv.event.AllTvStationCrawlEndEvent;
import com.laudandjolynn.mytv.event.CrawlEvent;
import com.laudandjolynn.mytv.event.CrawlEventListener;
import com.laudandjolynn.mytv.event.CrawlEventListenerAdapter;
import com.laudandjolynn.mytv.event.ProgramTableCrawlEndEvent;
import com.laudandjolynn.mytv.event.TvStationFoundEvent;
import com.laudandjolynn.mytv.model.MyTv;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;
import com.laudandjolynn.mytv.utils.MemoryCache;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月30日 下午3:05:47
 * @copyright: www.laudandjolynn.com
 */
public class TvServiceImpl implements TvService {
	private TvDao tvDao = new TvDaoImpl();
	private final static int TV_STATION_PERSISTENT_THRESHOLD = 100;

	/**
	 * 保存电视台
	 * 
	 * @param stations
	 */
	@Override
	public void save(TvStation... stations) {
		int size = stations == null ? 0 : stations.length;
		if (size == 0) {
			return;
		}
		tvDao.save(stations);
	}

	/**
	 * 保存电视台节目表
	 * 
	 * @param programTables
	 */
	@Override
	public void save(ProgramTable... programTables) {
		int size = programTables == null ? 0 : programTables.length;
		if (size == 0) {
			return;
		}
		List<ProgramTable> resultList = new ArrayList<ProgramTable>();
		for (int i = 0; i < size; i++) {
			ProgramTable pt = programTables[i];
			String stationName = pt.getStationName();
			String date = pt.getAirDate();
			if (tvDao.isProgramTableExists(stationName, date)) {
				continue;
			}
			resultList.add(pt);
		}
		programTables = new ProgramTable[resultList.size()];
		tvDao.save(resultList.toArray(programTables));
	}

	/**
	 * 获取电视台分类
	 * 
	 * @return
	 */
	@Override
	public List<String> getMyTvClassify() {
		return tvDao.getMyTvClassify();
	}

	@Override
	public List<TvStation> getAllCrawlableStation() {
		List<TvStation> stationList = tvDao.getAllCrawlableStation();
		if (stationList != null) {
			MemoryCache.getInstance().addCache(stationList);
		}
		return stationList;
	}

	@Override
	public TvStation getStation(String stationName) {
		TvStation tvStation = MemoryCache.getInstance().getStation(stationName);
		if (tvStation == null) {
			List<TvStation> stationList = tvDao.getStation(stationName);
			if (stationList.size() > 0) {
				tvStation = stationList.get(0);
				MemoryCache.getInstance().addCache(tvStation);
			}
		}
		return tvStation;
	}

	/**
	 * 根据显示名取得电视台对象
	 * 
	 * @param displayName
	 *            电视台显示名
	 * @param classify
	 *            电视台分类，可以为null。为空时，将查找stationName与displayName相同的电视台
	 * @return
	 */
	@Override
	public TvStation getStationByDisplayName(String displayName, String classify) {
		TvStation tvStation = tvDao.getStationByDisplayName(displayName,
				classify);
		if (tvStation != null) {
			MemoryCache.getInstance().addCache(tvStation);
		}
		return tvStation;
	}

	/**
	 * 获取指定电视台节目表
	 * 
	 * @param stationName
	 * @param date
	 * @return
	 */
	@Override
	public List<ProgramTable> getProgramTable(String stationName, String date) {
		return tvDao.getProgramTable(stationName, date);
	}

	/**
	 * 根据电视台分类查询分类下的所有电视台
	 * 
	 * @param classify
	 * @return
	 */
	@Override
	public List<MyTv> getMyTvByClassify(String classify) {
		return tvDao.getMyTvByClassify(classify);
	}

	/**
	 * 判断指定的电视节目表是否已存在
	 * 
	 * @param stationName
	 * @param date
	 * @return
	 */
	@Override
	public boolean isProgramTableExists(String stationName, String date) {
		return tvDao.isProgramTableExists(stationName, date);
	}

	/**
	 * 根据电视台名称、日期抓取电视节目表
	 * 
	 * @param tvStation
	 *            电视台对象
	 * @param date
	 *            日期,yyyy-MM-dd
	 * @param listeners
	 *            抓取事件监听器
	 * @return
	 */
	@Override
	public List<ProgramTable> crawlProgramTable(TvStation tvStation,
			String date, CrawlEventListener... listeners) {
		Crawler crawler = MyTvCrawlerManager.getInstance().newCrawler();
		crawler.registerCrawlEventListener(new CrawlEventListenerAdapter() {

			@Override
			public void crawlEnd(CrawlEvent event) {
				if (event instanceof ProgramTableCrawlEndEvent) {
					List<ProgramTable> resultList = ((ProgramTableCrawlEndEvent) event)
							.getReturnValue();
					ProgramTable[] resultArray = new ProgramTable[resultList
							.size()];
					save(resultList.toArray(resultArray));
				}
			}
		});
		for (int i = 0, length = listeners == null ? 0 : listeners.length; i < length; i++) {
			crawler.registerCrawlEventListener(listeners[i]);
		}
		return crawler.crawlProgramTable(date, tvStation);
	}

	/**
	 * 抓取所有电视台
	 * 
	 * @param listeners
	 *            抓取事件监听器
	 * 
	 * @return
	 */
	@Override
	public List<TvStation> crawlAllTvStation(CrawlEventListener... listeners) {
		Crawler crawler = MyTvCrawlerManager.getInstance().newCrawler();
		final List<TvStation> stationList = new ArrayList<TvStation>();
		crawler.registerCrawlEventListener(new CrawlEventListenerAdapter() {

			@Override
			public void itemFound(CrawlEvent event) {
				if (event instanceof TvStationFoundEvent) {
					TvStation item = ((TvStationFoundEvent) event).getItem();
					synchronized (stationList) {
						int size = stationList.size();
						if (size > 0
								&& size % TV_STATION_PERSISTENT_THRESHOLD == 0) {
							TvStation[] stations = new TvStation[size];
							tvDao.save(stationList.toArray(stations));
							// 保存之后再写入缓存，因为持久化前会判断电视台是否已经存在
							MemoryCache.getInstance().addCache(stationList);
							stationList.clear();
						} else {
							stationList.add(item);
						}
					}
				}
			}

			@Override
			public void crawlEnd(CrawlEvent event) {
				if (event instanceof AllTvStationCrawlEndEvent) {
					synchronized (stationList) {
						if (stationList.size() > 0) {
							TvStation[] stations = new TvStation[stationList
									.size()];
							tvDao.save(stationList.toArray(stations));
							// 保存之后再写入缓存，因为持久化前会判断电视台是否已经存在
							MemoryCache.getInstance().addCache(stationList);
							stationList.clear();
						}
					}
				}
			}
		});
		for (int i = 0, length = listeners == null ? 0 : listeners.length; i < length; i++) {
			crawler.registerCrawlEventListener(listeners[i]);
		}
		return crawler.crawlAllTvStation();
	}

	@Override
	public List<TvStation> getDisplayedTvStation() {
		return tvDao.getDisplayedTvStation();
	}

	@Override
	public List<MyTv> getMyTv() {
		return tvDao.getMyTv();
	}

	@Override
	public boolean isInMyTv(TvStation tvStation) {
		return MemoryCache.getInstance().isInMyTv(tvStation);
	}
}
