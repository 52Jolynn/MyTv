package com.laudandjolynn.mytv.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytv.event.AllTvStationCrawlEndEvent;
import com.laudandjolynn.mytv.event.CrawlEvent;
import com.laudandjolynn.mytv.event.CrawlEventListener;
import com.laudandjolynn.mytv.event.CrawlEventListenerAdapter;
import com.laudandjolynn.mytv.event.ProgramTableCrawlEndEvent;
import com.laudandjolynn.mytv.exception.MyTvException;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;
import com.laudandjolynn.mytv.utils.Constant;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月16日 下午1:12:03
 * @copyright: www.laudandjolynn.com
 */
public class CrawlerGroup extends AbstractCrawler {
	private List<Crawler> crawlers = new ArrayList<Crawler>();
	private final static String CRAWLER_GROUP_NAME = "crawlergroup";
	private final static Logger logger = LoggerFactory
			.getLogger(CrawlerGroup.class);
	private CrawlEventListener listener = null;

	public CrawlerGroup() {
		this.listener = new CrawlEventListenerAdapter() {
			@Override
			public void itemFound(CrawlEvent event) {
				for (CrawlEventListener listener : listeners) {
					listener.itemFound(event);
				}
			}
		};
	}

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
		crawler.registerCrawlEventListener(listener);
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
		int size = crawlers.size();
		int maxThreadNum = Constant.CPU_PROCESSOR_NUM * 2;
		ExecutorService executorService = Executors
				.newFixedThreadPool(size > maxThreadNum ? maxThreadNum : size);
		CompletionService<List<TvStation>> completionService = new ExecutorCompletionService<List<TvStation>>(
				executorService);
		for (final Crawler crawler : crawlers) {
			Callable<List<TvStation>> task = new Callable<List<TvStation>>() {
				@Override
				public List<TvStation> call() throws Exception {
					return crawler.crawlAllTvStation();
				}
			};
			executorService.submit(task);
		}
		executorService.shutdown();
		int count = 0;
		while (count < size) {
			try {
				List<TvStation> stationList = completionService.take().get();
				if (stationList != null) {
					resultList.addAll(stationList);
				}
			} catch (InterruptedException e) {
				logger.error("crawl task of all tv station interrupted.", e);
			} catch (ExecutionException e) {
				logger.error("crawl task of all tv station executed fail.", e);
			}
			count++;
		}

		for (CrawlEventListener listener : listeners) {
			listener.crawlEnd(new AllTvStationCrawlEndEvent(this, resultList));
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
				if (ptList != null) {
					resultList.addAll(ptList);
				}
				break;
			}
		}
		for (CrawlEventListener listener : listeners) {
			listener.crawlEnd(new ProgramTableCrawlEndEvent(this, resultList,
					station.getName(), date));
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
