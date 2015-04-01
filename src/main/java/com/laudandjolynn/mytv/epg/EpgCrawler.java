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
package com.laudandjolynn.mytv.epg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.laudandjolynn.mytv.Crawler;
import com.laudandjolynn.mytv.Init;
import com.laudandjolynn.mytv.exception.MyTvException;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;
import com.laudandjolynn.mytv.utils.Constant;
import com.laudandjolynn.mytv.utils.DateUtils;
import com.laudandjolynn.mytv.utils.MyTvUtils;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月28日 上午12:00:44
 * @copyright: www.laudandjolynn.com
 */
public class EpgCrawler {
	private final static Logger logger = LoggerFactory
			.getLogger(EpgCrawler.class);

	/**
	 * 获取所有电视台
	 * 
	 * @param htmlPage
	 * @return
	 */
	public static List<TvStation> crawlAllTvStationByPage(HtmlPage htmlPage) {
		return EpgParser.parseTvStation(htmlPage.asXml());
	}

	/**
	 * 获取所有电视台
	 * 
	 * @return
	 */
	public static List<TvStation> crawlAllTvStation() {
		Page page = Crawler.crawl(Constant.EPG_URL);
		if (page.isHtmlPage()) {
			return crawlAllTvStationByPage((HtmlPage) page);
		}
		return null;
	}

	/**
	 * 获取指定日期的所有电视台节目表
	 * 
	 * @param date
	 *            日期，yyyy-MM-dd
	 * @return
	 */
	public static List<ProgramTable> crawlAllProgramTable(String date) {
		Collection<TvStation> stations = Init.getIntance()
				.getAllCacheTvStation();
		return crawlAllProgramTable(new ArrayList<TvStation>(stations), date);
	}

	/**
	 * 抓取所有电视台指定日志的电视节目表，多线程
	 * 
	 * @param stations
	 * @param date
	 * @return
	 */
	private static List<ProgramTable> crawlAllProgramTable(
			List<TvStation> stations, final String date) {
		List<ProgramTable> resultList = new ArrayList<ProgramTable>();
		int threadCount = EpgDao.getTvStationClassify().size();
		ExecutorService executorService = Executors
				.newFixedThreadPool(threadCount);
		CompletionService<List<ProgramTable>> completionService = new ExecutorCompletionService<List<ProgramTable>>(
				executorService);
		for (final TvStation station : stations) {
			Callable<List<ProgramTable>> task = new Callable<List<ProgramTable>>() {
				@Override
				public List<ProgramTable> call() throws Exception {
					return crawlProgramTable(station.getName(), date);
				}
			};
			completionService.submit(task);
		}
		int size = stations == null ? 0 : stations.size();
		int count = 0;
		while (count < size) {
			Future<List<ProgramTable>> future;
			try {
				future = completionService.poll(10, TimeUnit.MINUTES);
				resultList.addAll(future.get());
			} catch (InterruptedException e) {
				logger.error("craw program table of all station at " + date
						+ " was interrupted.", e);
			} catch (ExecutionException e) {
				logger.error(
						"error occur while craw program table of all station at "
								+ date, e);
			}
			count++;
		}
		executorService.shutdown();

		return resultList;
	}

	/**
	 * 根据电视台、日期获取电视节目表
	 * 
	 * @param stationName
	 *            电视台名称
	 * @param date
	 *            日期，yyyy-MM-dd
	 * @return
	 */
	public static List<ProgramTable> crawlProgramTable(String stationName,
			String date) {
		if (stationName == null || date == null) {
			logger.debug("station name or date is null.");
			return null;
		}
		TvStation station = Init.getIntance().getStation(stationName);
		if (station == null) {
			station = EpgDao.getStation(stationName);
		}
		return crawlProgramTable(station, date);
	}

	/**
	 * 根据电视台、日期获取电视节目表
	 * 
	 * @param htmlPage
	 *            已获取的html页面对象
	 * @param stationName
	 *            电视台名称
	 * @param date
	 *            日期，yyyy-MM-dd
	 * @return
	 */
	public static List<ProgramTable> crawlProgramTableByPage(HtmlPage htmlPage,
			String stationName, String date) {
		TvStation station = Init.getIntance().getStation(stationName);
		if (station == null) {
			station = EpgDao.getStation(stationName);
		}
		return crawlProgramTableByPage(htmlPage, station, date);
	}

	private static List<ProgramTable> crawlProgramTableByPage(
			HtmlPage htmlPage, TvStation station, String date) {
		if (station == null || htmlPage == null) {
			logger.debug("station and html page must not null.");
			return null;
		}
		Date dateObj = DateUtils.string2Date(date, "yyyy-MM-dd");
		if (dateObj == null) {
			logger.debug("date must not null.");
			return null;
		}
		String stationName = station.getName();
		String queryDate = DateUtils.date2String(dateObj, "yyyy-MM-dd");
		logger.info("crawl program table of " + stationName + " at "
				+ queryDate);
		if (EpgDao.isProgramTableExists(stationName, queryDate)) {
			logger.debug("the TV station's program table of " + stationName
					+ " have been saved in db.");
			return null;
		}

		String city = station.getCity();
		List<?> stationElements = null;
		if (city == null) {
			stationElements = htmlPage
					.getByXPath("//div[@class='md_left_right']/dl//h3//a[@class='channel']");
		} else {
			// 城市电视台
			stationElements = htmlPage
					.getByXPath("//dl[@id='cityList']//div[@class='lv3']//a[@class='channel']");
		}
		for (Object element : stationElements) {
			HtmlAnchor anchor = (HtmlAnchor) element;
			if (stationName.equals(anchor.getTextContent().trim())) {
				try {
					htmlPage = anchor.click();
				} catch (IOException e) {
					throw new MyTvException(
							"error occur while search program table of "
									+ stationName + " at spec date: " + date, e);
				}
				break;
			}
		}

		if (!queryDate.equals(DateUtils.today())) {
			DomElement element = htmlPage.getElementById("date");
			element.setNodeValue(queryDate);
			element.setTextContent(queryDate);
			List<?> list = htmlPage.getByXPath("//div[@id='search_1']/a");
			HtmlAnchor anchor = (HtmlAnchor) list.get(0);
			try {
				htmlPage = anchor.click();
			} catch (IOException e) {
				throw new MyTvException(
						"error occur while search program table of "
								+ stationName + " at spec date: " + date, e);
			}
		}
		String html = htmlPage.asXml();
		List<ProgramTable> ptList = EpgParser.parseProgramTable(html);
		ProgramTable[] ptArray = new ProgramTable[ptList.size()];
		EpgDao.save(ptList.toArray(ptArray));
		MyTvUtils.outputCrawlData(queryDate, html, stationName);
		return ptList;
	}

	/**
	 * 根据电视台、日期获取电视节目表
	 * 
	 * @param station
	 *            电视台对象
	 * @param date
	 *            日期，yyyy-MM-dd
	 * @return
	 */
	private static List<ProgramTable> crawlProgramTable(TvStation station,
			String date) {
		if (station == null) {
			logger.debug("the station must be not null.");
			return null;
		}
		Page page = Crawler.crawl(Constant.EPG_URL);
		if (!page.isHtmlPage()) {
			logger.debug("the page isn't html page at url " + Constant.EPG_URL);
			return null;
		}
		return crawlProgramTableByPage((HtmlPage) page, station, date);
	}
}
