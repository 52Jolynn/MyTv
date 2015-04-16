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
public class CralwerGroup implements Crawler {
	private List<Crawler> crawlers = new ArrayList<Crawler>();

	public void addCrawler(Crawler crawler) {
		this.crawlers.add(crawler);
	}

	@Override
	public List<TvStation> crawlAllTvStation() {
		List<TvStation> resultList = new ArrayList<TvStation>();
		for (Crawler crawler : crawlers) {
			resultList.addAll(crawler.crawlAllTvStation());
		}
		return resultList;
	}

	@Override
	public List<ProgramTable> crawlAllProgramTable(String date) {
		List<ProgramTable> resultList = new ArrayList<ProgramTable>();
		for (Crawler crawler : crawlers) {
			resultList.addAll(crawler.crawlAllProgramTable(date));
		}
		return resultList;
	}

	@Override
	public List<ProgramTable> crawlProgramTable(String stationName, String date) {
		List<ProgramTable> resultList = new ArrayList<ProgramTable>();
		for (Crawler crawler : crawlers) {
			if (crawler.exists(stationName)) {
				resultList.addAll(crawler.crawlProgramTable(stationName, date));
				break;
			}
		}
		return resultList;
	}

	@Override
	public boolean exists(String stationName) {
		for (Crawler crawler : crawlers) {
			if (crawler.exists(stationName)) {
				return true;
			}
		}
		return false;
	}

}
