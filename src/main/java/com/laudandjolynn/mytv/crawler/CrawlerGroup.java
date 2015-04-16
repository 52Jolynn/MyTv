package com.laudandjolynn.mytv.crawler;

import java.util.ArrayList;
import java.util.List;

import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月16日 下午1:12:03
 * @copyright: www.laudandjolynn.com
 */
public class CrawlerGroup implements Crawler {
	private List<Crawler> crawlers = new ArrayList<Crawler>();
	private final static String CRAWLER_GROUP_NAME = "crawlergroup";

	/**
	 * 添加抓取器
	 * 
	 * @param crawler
	 */
	public void addCrawler(Crawler crawler) {
		this.crawlers.add(crawler);
	}

	/**
	 * 移除抓取器
	 * 
	 * @param crawler
	 */
	public void removeCrawler(Crawler crawler) {
		this.crawlers.remove(crawler);
	}

	@Override
	public String getCrawlerName() {
		return CRAWLER_GROUP_NAME;
	}

	@Override
	public List<TvStation> crawlAllTvStation() {
		List<TvStation> resultList = new ArrayList<TvStation>();
		for (Crawler crawler : crawlers) {
			List<TvStation> stationList = crawler.crawlAllTvStation();
			if (stationList != null && stationList.size() > 0) {
				resultList.addAll(stationList);
			}
		}
		return resultList;
	}

	@Override
	public List<ProgramTable> crawlAllProgramTable(String date) {
		List<ProgramTable> resultList = new ArrayList<ProgramTable>();
		for (Crawler crawler : crawlers) {
			List<ProgramTable> ptList = crawler.crawlAllProgramTable(date);
			if (ptList != null && ptList.size() > 0) {
				resultList.addAll(ptList);
			}
		}
		return resultList;
	}

	@Override
	public List<ProgramTable> crawlProgramTable(TvStation station, String date) {
		List<ProgramTable> resultList = new ArrayList<ProgramTable>();
		for (Crawler crawler : crawlers) {
			if (crawler.exists(station)) {
				List<ProgramTable> ptList = crawler.crawlProgramTable(station,
						date);
				if (ptList != null && ptList.size() > 0) {
					resultList.addAll(ptList);
				}
				break;
			}
		}
		return resultList;
	}

	@Override
	public boolean exists(TvStation station) {
		for (Crawler crawler : crawlers) {
			if (crawler.exists(station)) {
				return true;
			}
		}
		return false;
	}

}
