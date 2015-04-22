package com.laudandjolynn.mytv.crawler;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytv.exception.MyTvException;
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
	private final static Logger logger = LoggerFactory
			.getLogger(CrawlerGroup.class);

	/**
	 * 获取抓取器组中的所有抓取器
	 * 
	 * @return
	 */
	public List<Crawler> getCrawlers() {
		return crawlers;
	}

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
	public String getUrl() {
		throw new MyTvException("url isn't avaliable of crawler group.");
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
	public List<ProgramTable> crawlProgramTable(String date, TvStation station) {
		if (station == null || date == null) {
			logger.info("station and date must be not null.");
			return null;
		}
		List<ProgramTable> resultList = new ArrayList<ProgramTable>();
		for (Crawler crawler : crawlers) {
			if (crawler.exists(station)) {
				List<ProgramTable> ptList = crawler.crawlProgramTable(date,
						station);
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

	@Override
	public void registerCrawlEventListener(CrawlEventListener listener) {
		for (Crawler crawler : crawlers) {
			crawler.registerCrawlEventListener(listener);
		}
	}
}
