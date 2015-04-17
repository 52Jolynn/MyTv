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
package com.laudandjolynn.mytv.crawler.epg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import java.util.concurrent.TimeoutException;

import org.dom4j.DocumentException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.laudandjolynn.mytv.crawler.AbstractCrawler;
import com.laudandjolynn.mytv.crawler.Parser;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;
import com.laudandjolynn.mytv.service.TvServiceImpl;
import com.laudandjolynn.mytv.utils.Constant;
import com.laudandjolynn.mytv.utils.DateUtils;
import com.laudandjolynn.mytv.utils.MyTvUtils;
import com.laudandjolynn.mytv.utils.WebCrawler;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月28日 上午12:00:44
 * @copyright: www.laudandjolynn.com
 */
class EpgCrawler extends AbstractCrawler {
	private final static Logger logger = LoggerFactory
			.getLogger(EpgCrawler.class);
	// cntv节目表地址
	private final static String EPG_URL = "http://tv.cntv.cn/epg";
	private final static String EPG_NAME = "epg";
	private TvServiceImpl tvService = new TvServiceImpl();
	private final static int MAX_THREAD = 5;

	public EpgCrawler(Parser parser) {
		super(parser);
	}

	@Override
	public String getCrawlerName() {
		return EPG_NAME;
	}

	/**
	 * 获取所有电视台
	 * 
	 * @return
	 */
	@Override
	public List<TvStation> crawlAllTvStation() {
		String epgFile = Constant.CRAWL_FILE_PATH + getCrawlerName()
				+ File.separator + getCrawlerName();
		File file = new File(epgFile);
		String html = null;
		if (file.exists()) {
			try {
				html = MyTvUtils.readAsXml(epgFile);
				return parser.parseTvStation(html);
			} catch (DocumentException e) {
				// do nothing
			}
		}
		Page page = WebCrawler.crawl(EPG_URL);
		if (page.isHtmlPage()) {
			HtmlPage htmlPage = (HtmlPage) page;
			html = htmlPage.asXml();
			List<TvStation> stationList = parser.parseTvStation(html);
			MyTvUtils.outputCrawlData(getCrawlerName(), html, getCrawlerName());
			return stationList;
		}

		return null;
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
	@Override
	public List<ProgramTable> crawlProgramTable(String date,
			TvStation... stations) {
		if (stations == null || stations.length == 0 || date == null) {
			logger.debug("station name or date is null.");
			return null;
		}
		if (stations.length == 1) {
			return crawlProgramTable(stations[0], date);
		} else {
			return crawlMultipleProgramTable(date, stations);
		}
	}

	@Override
	public boolean exists(TvStation station) {
		String epgFile = Constant.CRAWL_FILE_PATH + getCrawlerName()
				+ File.separator + getCrawlerName();
		File file = new File(epgFile);
		String city = station.getCity();
		String stationName = station.getName();
		if (file.exists()) {
			try {
				String html = MyTvUtils.readAsXml(epgFile);
				Document doc = Jsoup.parse(html);
				Elements elements = null;
				if (city == null) {
					elements = doc.select("div.md_left_right dl h3 a.channel");
				} else {
					elements = doc.select("dl#cityList div.lv3 a.channel");
				}
				for (Element element : elements) {
					if (stationName.equals(element.text())) {
						return true;
					}
				}
			} catch (DocumentException e) {
				// do noting
			}
		}
		Page page = WebCrawler.crawl(EPG_URL);
		if (!page.isHtmlPage()) {
			logger.debug("the page isn't html page at url " + EPG_URL);
			return false;
		}
		HtmlPage htmlPage = (HtmlPage) page;
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
				return true;
			}
		}
		return false;
	}

	private List<ProgramTable> crawlProgramTable(TvStation station, String date) {
		if (station == null) {
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
		String city = station.getCity();
		List<?> stationElements = null;
		Page page = WebCrawler.crawl(EPG_URL);
		HtmlPage htmlPage = null;
		if (page.isHtmlPage()) {
			htmlPage = (HtmlPage) page;
		}
		if (htmlPage == null) {
			logger.debug(EPG_URL + " is an empty page.");
			return null;
		}
		if (city == null) {
			stationElements = htmlPage
					.getByXPath("//div[@class='md_left_right']/dl//h3//a[@class='channel']");
		} else {
			// 城市电视台
			stationElements = htmlPage
					.getByXPath("//dl[@id='cityList']//div[@class='lv3']//a[@class='channel']");
		}
		boolean exists = false;
		for (Object element : stationElements) {
			HtmlAnchor anchor = (HtmlAnchor) element;
			if (stationName.equals(anchor.getTextContent().trim())) {
				exists = true;
				try {
					htmlPage = anchor.click();
				} catch (IOException e) {
					logger.error("error occur while search program table of "
							+ stationName + " at spec date: " + queryDate, e);
					return null;
				}
				break;
			}
		}

		if (!exists) {
			logger.info(stationName + " isn't exists at " + getCrawlerName());
			return null;
		}

		if (!queryDate.equals(DateUtils.today())) {
			DomElement element = htmlPage.getElementById("date");
			element.setAttribute("readonly", "false");
			element.setAttribute("value", queryDate);
			element.setNodeValue(queryDate);
			element.setTextContent(queryDate);
			List<?> list = htmlPage.getByXPath("//div[@id='search_1']/a");
			HtmlAnchor anchor = (HtmlAnchor) list.get(0);
			try {
				htmlPage = anchor.click();
			} catch (IOException e) {
				logger.error("error occur while search program table of "
						+ stationName + " at spec date: " + queryDate, e);
				return null;
			}
		}
		String html = htmlPage.asXml();
		List<ProgramTable> ptList = parser.parseProgramTable(html);
		MyTvUtils.outputCrawlData(queryDate, html, queryDate
				+ Constant.UNDERLINE + stationName);
		return ptList;
	}

	/**
	 * 抓取所有电视台指定日志的电视节目表，多线程
	 * 
	 * @param date
	 * @param stations
	 * @return
	 */
	private List<ProgramTable> crawlMultipleProgramTable(final String date,
			TvStation... stations) {
		int size = stations == null ? 0 : stations.length;
		if (size == 0) {
			return null;
		}
		List<ProgramTable> resultList = new ArrayList<ProgramTable>();
		int threadCount = tvService.getTvStationClassify().size();
		ExecutorService executorService = Executors
				.newFixedThreadPool(threadCount > MAX_THREAD ? MAX_THREAD
						: threadCount);
		CompletionService<List<ProgramTable>> completionService = new ExecutorCompletionService<List<ProgramTable>>(
				executorService);
		for (final TvStation station : stations) {
			Callable<List<ProgramTable>> task = new Callable<List<ProgramTable>>() {
				@Override
				public List<ProgramTable> call() throws Exception {
					return crawlProgramTable(date, station);
				}
			};
			completionService.submit(task);
		}
		int count = 0;
		while (count < size) {
			Future<List<ProgramTable>> future;
			try {
				future = completionService.poll(5, TimeUnit.MINUTES);
				List<ProgramTable> ptList = future.get(5, TimeUnit.MINUTES);
				if (ptList != null) {
					resultList.addAll(ptList);
				}
			} catch (InterruptedException e) {
				logger.error("craw program table of all station at " + date
						+ " was interrupted.", e);
			} catch (ExecutionException e) {
				logger.error(
						"error occur while craw program table of all station at "
								+ date, e);
			} catch (TimeoutException e) {
				logger.error("query program table of all sation at at " + date
						+ " is timeout.", e);
			}
			count++;
		}
		executorService.shutdown();

		return resultList;
	}
}
