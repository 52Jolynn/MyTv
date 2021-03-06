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
package com.laudandjolynn.mytv.crawler.tvmao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlBold;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.laudandjolynn.mytv.crawler.AbstractCrawler;
import com.laudandjolynn.mytv.event.AllTvStationCrawlEndEvent;
import com.laudandjolynn.mytv.event.CrawlEventListener;
import com.laudandjolynn.mytv.event.ProgramTableCrawlEndEvent;
import com.laudandjolynn.mytv.event.ProgramTableFoundEvent;
import com.laudandjolynn.mytv.event.TvStationFoundEvent;
import com.laudandjolynn.mytv.exception.MyTvException;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;
import com.laudandjolynn.mytv.proxy.MyTvProxyManager;
import com.laudandjolynn.mytv.utils.Constant;
import com.laudandjolynn.mytv.utils.DateUtils;
import com.laudandjolynn.mytv.utils.MyTvUtils;
import com.laudandjolynn.mytv.utils.WebCrawler;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月15日 下午3:32:56
 * @copyright: www.laudandjolynn.com
 */
public class TvMaoCrawler extends AbstractCrawler {
	private final static Logger logger = LoggerFactory
			.getLogger(TvMaoCrawler.class);
	// tvmao节目表地址
	private final static String TV_MAO_URL_PREFIX = "http://www.tvmao.com";
	private final static String TV_MAO_URL = TV_MAO_URL_PREFIX
			+ "/program/channels";
	private final static String TV_MAO_NAME = "tvmao";
	private final static AtomicInteger SEQUENCE = new AtomicInteger(300000);
	// 防反爬虫
	private final static int MAX_ACTIVITY_CRALWER_SIZE = 2;
	private final static ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(
			Constant.CPU_PROCESSOR_NUM);
	private final static GenericKeyedObjectPool<TvMaoObjectKey, HtmlPage> TV_MAO_PAGES = new GenericKeyedObjectPool<TvMaoObjectKey, HtmlPage>(
			new TvMaoPageObjectFactory(), MAX_ACTIVITY_CRALWER_SIZE,
			GenericKeyedObjectPool.WHEN_EXHAUSTED_BLOCK, 1000,
			MAX_ACTIVITY_CRALWER_SIZE);
	private final static Random RANDOM = new Random();

	@Override
	public String getCrawlerName() {
		return TV_MAO_NAME;
	}

	@Override
	public String getUrl() {
		return TV_MAO_URL;
	}

	@Override
	public List<TvStation> crawlAllTvStation() {
		String tvMaoFile = Constant.CRAWL_FILE_PATH + getCrawlerName();
		File file = new File(tvMaoFile);
		List<TvStation> resultList = null;
		if (file.exists() && file.listFiles().length > 0) {
			resultList = crawlAllTvStationFromFile(file.listFiles());
		} else {
			resultList = crawlAllTvStationFromWeb();
		}
		for (CrawlEventListener listener : listeners) {
			listener.crawlEnd(new AllTvStationCrawlEndEvent(this, resultList));
		}
		return resultList;
	}

	/**
	 * 从web抓取数据
	 * 
	 * @return
	 */
	private List<TvStation> crawlAllTvStationFromWeb() {
		logger.info("crawl all tv station from " + getUrl() + ".");
		List<TvStation> resultList = new ArrayList<TvStation>();

		String today = DateUtils.today();
		HtmlPage htmlPage = null;
		TvMaoObjectKey key = new TvMaoObjectKey(getUrl(), today);
		try {
			htmlPage = TV_MAO_PAGES.borrowObject(key);
			List<?> elements = htmlPage
					.getByXPath("//div[@class='pgnav_wrap']/table[@class='pgnav']//a");
			int size = elements == null ? 0 : elements.size();
			for (int i = 0; i < size; i++) {
				HtmlAnchor anchor = null;
				HtmlPage hp = null;
				TvMaoObjectKey hpKey = null;
				try {
					anchor = (HtmlAnchor) elements.get(i);
					if (!anchor.getAttribute("href").startsWith("/program/")) {
						continue;
					}
					final String city = anchor.getTextContent().trim();
					if ("CCTV".equals(city)) {
						logger.debug("a city program table of tvmao: " + city
								+ ", url: " + anchor.getHrefAttribute());
						resultList.addAll(getTvStations(htmlPage, city));
						resultList
								.addAll(getAllTvStationOfCity(htmlPage, city));
					} else {
						String href = anchor.getHrefAttribute();
						String url = TV_MAO_URL_PREFIX + href;
						hpKey = new TvMaoObjectKey(url, today);
						logger.debug("a city of tvmao: " + city + ", url: "
								+ url);
						TimeUnit.MILLISECONDS.sleep(getRandomSleepTime());
						hp = TV_MAO_PAGES.borrowObject(hpKey);
						resultList.addAll(getTvStations(hp, city));
						resultList.addAll(getAllTvStationOfCity(hp, city));
					}
				} catch (Exception e) {
					logger.error("error occur while crawl tv station.", e);
					continue;
				} finally {
					if (hp != null) {
						TV_MAO_PAGES.returnObject(hpKey, hp);
					}
				}
			}
		} catch (Exception e) {
			logger.error("borrow " + getUrl() + " fail.", e);
		} finally {
			if (htmlPage != null) {
				try {
					TV_MAO_PAGES.returnObject(key, htmlPage);
				} catch (Exception e) {
					logger.error("return " + getUrl() + " fail.", e);
				}
			}
		}
		return resultList;
	}

	/**
	 * 从本地文件抓取数据
	 * 
	 * @param files
	 * @return
	 */
	private List<TvStation> crawlAllTvStationFromFile(File[] files) {
		logger.info("crawl all tv station from files.");
		List<TvStation> resultList = new ArrayList<TvStation>();
		ThreadFactory threadFactory = new BasicThreadFactory.Builder()
				.namingPattern("Mytv_Crawl_All_TV_Station_Of_TvMao_%d").build();
		ExecutorService executorService = Executors.newFixedThreadPool(2,
				threadFactory);
		CompletionService<List<TvStation>> completionService = new ExecutorCompletionService<List<TvStation>>(
				executorService);
		int size = files == null ? 0 : files.length;
		for (int i = 0; i < size; i++) {
			final File file = files[i];
			Callable<List<TvStation>> task = new Callable<List<TvStation>>() {
				@Override
				public List<TvStation> call() throws Exception {
					String filePath = file.getPath();
					String classifyEnds = filePath.substring(0,
							filePath.lastIndexOf(Constant.UNDERLINE));
					String city = classifyEnds.substring(classifyEnds
							.lastIndexOf(Constant.UNDERLINE) + 1);
					String html = null;
					try {
						logger.debug("parse tv station file: " + filePath);
						html = MyTvUtils.readAsHtml(filePath);
					} catch (IOException e) {
						logger.error("read as xml error: " + filePath, e);
						return null;
					}
					return parseTvStation(city, html);
				}
			};
			completionService.submit(task);
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
				logger.error("crawl all tv station task interrupted.", e);
			} catch (ExecutionException e) {
				logger.error("crawl all tv station task executed fail.", e);
			}
			count++;
		}
		return resultList;
	}

	/**
	 * 抓取指定城市下的所有电视台
	 * 
	 * @param htmlPage
	 * @param city
	 * @return
	 */
	private List<TvStation> getAllTvStationOfCity(HtmlPage htmlPage, String city) {
		List<TvStation> resultList = new ArrayList<TvStation>();
		List<?> elements = htmlPage
				.getByXPath("//div[@class='chlsnav']//div[@class='plst']/parent::*");
		for (int i = 0, size = elements == null ? 0 : elements.size(); i < size; i++) {
			try {
				HtmlAnchor anchor = (HtmlAnchor) elements.get(i);
				String href = anchor.getHrefAttribute();
				if (!href.startsWith("/program/")) {
					continue;
				}
				logger.debug(anchor.getTextContent()
						+ " program table of tvmao: " + ", url: " + href);
				TimeUnit.MILLISECONDS.sleep(getRandomSleepTime());
				HtmlPage p = (HtmlPage) WebCrawler.crawl(TV_MAO_URL_PREFIX
						+ href);
				resultList.addAll(getTvStations(p, city));
			} catch (Exception e) {
				logger.error("error occur while get all tv station of city: "
						+ city, e);
				continue;
			}
		}
		return resultList;
	}

	/**
	 * 解析指定城市下的电视台
	 * 
	 * @param htmlPage
	 * @param city
	 *            所属城市
	 * @return
	 */
	private List<TvStation> getTvStations(HtmlPage htmlPage, String city) {
		String html = htmlPage.asXml();
		List<?> elements = htmlPage
				.getByXPath("//div[@class='chlsnav']/div[@class='pbar']/b");
		HtmlBold hb = (HtmlBold) elements.get(0);
		String classify = hb.getTextContent().trim();
		MyTvUtils.outputCrawlData(getCrawlerName(), html,
				getCrawlFileName(city, classify));
		List<TvStation> stationList = parseTvStation(city, html);
		logger.debug("tv station crawled." + stationList);
		return stationList;
	}

	@Override
	public List<ProgramTable> crawlProgramTable(String date, TvStation station) {
		if (station == null) {
			logger.debug("station is null while crawl program table.");
			return null;
		}

		Date dateObj = DateUtils.string2Date(date, "yyyy-MM-dd");
		if (dateObj == null) {
			logger.debug("date is null while crawl program table of "
					+ station.getName());
			return null;
		}
		String queryDate = DateUtils.date2String(dateObj, "yyyy-MM-dd");
		final TvMaoCrawlTask task = new TvMaoCrawlTask();
		task.date = queryDate;
		task.tvStation = station;

		ScheduledFuture<List<ProgramTable>> future = SCHEDULED_EXECUTOR_SERVICE
				.schedule(new Callable<List<ProgramTable>>() {
					@Override
					public List<ProgramTable> call() throws Exception {
						return crawlProgramTable(task);
					}
				}, getScheduleFrequency(), TimeUnit.MILLISECONDS);
		try {
			return future.get();
		} catch (InterruptedException e) {
			logger.error("crawl task interrupted while crawl program table of "
					+ station + " at " + queryDate, e);
		} catch (ExecutionException e) {
			logger.error(
					"crawl task executed fail while crawl program table of "
							+ station + " at " + queryDate, e);
		}
		return null;
	}

	private List<ProgramTable> crawlProgramTable(TvMaoCrawlTask task) {
		TvStation station = task.tvStation;
		String queryDate = task.date;
		String stationName = station.getName();

		logger.info("crawl program table of " + stationName + " at "
				+ queryDate);
		HtmlPage htmlPage = (HtmlPage) WebCrawler.crawl(TV_MAO_URL);
		try {
			htmlPage = searchStation(htmlPage, station);
		} catch (Exception e) {
			logger.error("error occur while search station: " + stationName, e);
			return null;
		}

		if (htmlPage == null) {
			logger.debug("cannot get station data from " + TV_MAO_URL + " of "
					+ stationName);
			return null;
		}

		if (!queryDate.equals(DateUtils.today())) {
			Set<String> availableQueryDate = new HashSet<String>();
			String[] dates = DateUtils.getWeek(new Date(), "yyyy-MM-dd");
			for (String d : dates) {
				availableQueryDate.add(d);
			}

			if (availableQueryDate.contains(queryDate)) {
				List<?> dateElements = htmlPage
						.getByXPath("//div[@class='pgnav_wrap']//div[@class='epghdc lt']//dl[@class='commtab clear']/dd/a");
				for (int i = 0, size = dateElements == null ? 0 : dateElements
						.size(); i < size; i++) {
					HtmlAnchor anchor = (HtmlAnchor) dateElements.get(i);
					String value = anchor.getTextContent().trim();
					if (value.endsWith(")")
							&& queryDate.equals(Calendar.getInstance().get(
									Calendar.YEAR)
									+ "-"
									+ value.substring(2, value.length() - 1))) {
						String href = anchor.getHrefAttribute();
						htmlPage = (HtmlPage) WebCrawler
								.crawl(TV_MAO_URL_PREFIX + href);
						break;
					}
				}
			}
		}

		String html = htmlPage.asXml();
		List<ProgramTable> ptList = parseProgramTable(html);
		MyTvUtils.outputCrawlData(queryDate, html, queryDate
				+ Constant.UNDERLINE + getCrawlerName() + Constant.UNDERLINE
				+ stationName);
		for (CrawlEventListener listener : listeners) {
			listener.crawlEnd(new ProgramTableCrawlEndEvent(this, ptList,
					station.getName(), queryDate));
		}
		return ptList;
	}

	@Override
	public boolean exists(TvStation station) {
		String city = station.getCity();
		String classify = station.getClassify();
		if (city == null || classify == null) {
			return false;
		}
		String tvMaoFile = getCrawlFilePath(station);
		File file = new File(tvMaoFile);
		if (file.exists()) {
			String html = null;
			try {
				html = MyTvUtils.readAsHtml(tvMaoFile);
			} catch (IOException e) {
				return false;
			}
			Document doc = Jsoup.parse(html);
			Elements classifyElements = doc.select("div.chlsnav div.pbar b");
			String classifyName = classifyElements.get(0).text().trim();
			Elements channelElements = doc.select("div.chlsnav ul.r li");
			for (Element element : channelElements) {
				Element channel = element.child(0);
				String stationName = channel.text().trim();
				if (stationName.equals(station.getName())
						&& classifyName.equals(classify)) {
					return true;
				}
			}
			return false;
		}

		HtmlPage htmlPage = (HtmlPage) WebCrawler.crawl(TV_MAO_URL);
		try {
			if ((htmlPage = searchStation(htmlPage, station)) != null) {
				MyTvUtils.outputCrawlData(getCrawlerName(), htmlPage.asXml(),
						getCrawlFileName(city, classify));
				return true;
			}
		} catch (Exception e) {
			logger.error(
					"error occur while search station: " + station.getName(), e);
		}

		return false;
	}

	/**
	 * 搜索电视台在html中的位置
	 * 
	 * @param htmlPage
	 * @param station
	 * @return
	 */
	private HtmlPage searchStation(HtmlPage htmlPage, TvStation station) {
		String city = station.getCity();
		List<?> cityElements = htmlPage
				.getByXPath("//div[@class='pgnav_wrap']/table[@class='pgnav']//a");
		int ssize = cityElements == null ? 0 : cityElements.size();
		boolean found = false;
		for (int i = 0; i < ssize; i++) {
			final HtmlAnchor anchor = (HtmlAnchor) cityElements.get(i);
			if (!anchor.getAttribute("href").startsWith("/program/")) {
				continue;
			} else if (city.equals(anchor.getTextContent().trim())) {
				String href = anchor.getHrefAttribute();
				found = true;
				htmlPage = (HtmlPage) WebCrawler
						.crawl(TV_MAO_URL_PREFIX + href);
				break;
			}
		}
		if (!found) {
			return null;
		}
		found = false;
		List<?> classifyElements = htmlPage
				.getByXPath("//div[@class='chlsnav']/div[@class='pbar']/b");
		String classify = station.getClassify();
		HtmlBold hb = (HtmlBold) classifyElements.get(0);
		if (classify.equals(hb.getTextContent().trim())) {
			found = true;
		} else {
			classifyElements = htmlPage
					.getByXPath("//div[@class='chlsnav']//div[@class='plst']/parent::*");
			for (int i = 0, size = classifyElements == null ? 0
					: classifyElements.size(); i < size; i++) {
				HtmlAnchor anchor = (HtmlAnchor) classifyElements.get(i);
				String elementText = anchor.getFirstElementChild()
						.getFirstElementChild().getTextContent().trim();
				if (classify.equals(elementText)) {
					String href = anchor.getHrefAttribute();
					found = true;
					htmlPage = (HtmlPage) WebCrawler.crawl(TV_MAO_URL_PREFIX
							+ href);
					break;
				}
			}
		}
		if (!found) {
			return null;
		}
		String stationName = station.getName();
		List<?> stationElements = htmlPage
				.getByXPath("//div[@class='chlsnav']//ul[@class='r']//li");
		for (int i = 0, size = stationElements == null ? 0 : stationElements
				.size(); i < size; i++) {
			DomElement element = ((DomElement) stationElements.get(i))
					.getFirstElementChild();
			if (stationName.equals(element.getTextContent().trim())) {
				if (element instanceof HtmlBold) {
					return htmlPage;
				} else if (element instanceof HtmlAnchor) {
					String href = ((HtmlAnchor) element).getHrefAttribute();
					return (HtmlPage) WebCrawler
							.crawl(TV_MAO_URL_PREFIX + href);
				}
				break;
			}
		}
		return null;
	}

	/**
	 * 取得将被存储的抓取文件路径
	 * 
	 * @param station
	 * @return
	 */
	private String getCrawlFilePath(TvStation station) {
		return Constant.CRAWL_FILE_PATH + getCrawlerName() + File.separator
				+ getCrawlFileName(station.getCity(), station.getClassify());
	}

	/**
	 * 取得将被存储的抓取文件名
	 * 
	 * @param city
	 * @param classify
	 * @return
	 */
	private String getCrawlFileName(String city, String classify) {
		return getCrawlerName() + Constant.UNDERLINE + city
				+ Constant.UNDERLINE + classify;
	}

	private enum Week {
		SUNDAY("星期日"), MONDAY("星期一"), TUESDAY("星期二"), WEDNESDAY("星期三"), THURSDAY(
				"星期四"), FRIDAY("星期五"), SATURDAY("星期六");

		private String value;

		private Week(String value) {
			this.value = value;
		}

	}

	/**
	 * 解析电视台对象
	 * 
	 * @param city
	 * @param html
	 * @return
	 */
	private List<TvStation> parseTvStation(String city, String html) {
		Document doc = Jsoup.parse(html);
		Elements classifyElements = doc.select("div.chlsnav div.pbar b");
		String classify = classifyElements.get(0).text().trim();
		List<TvStation> resultList = new ArrayList<TvStation>();
		Elements channelElements = doc.select("div.chlsnav ul.r li");
		for (Element element : channelElements) {
			Element channel = element.child(0);
			TvStation tv = new TvStation();
			String stationName = channel.text().trim();
			tv.setName(stationName);
			tv.setCity(city);
			tv.setClassify(classify);
			tv.setSequence(SEQUENCE.incrementAndGet());
			for (CrawlEventListener listener : listeners) {
				listener.itemFound(new TvStationFoundEvent(this, tv));
			}
			resultList.add(tv);
		}
		return resultList;
	}

	/**
	 * 解析电视节目表
	 * 
	 * @param html
	 * @return
	 */
	private List<ProgramTable> parseProgramTable(String html) {
		Document doc = Jsoup.parse(html);
		Elements dateElements = doc
				.select("div.pgmain div[class=\"mt10 clear\"] b:first-child");
		String dateAndWeek = dateElements.get(0).text().trim();
		String[] dateAndWeekArray = dateAndWeek.split("\\s+");
		String date = Calendar.getInstance().get(Calendar.YEAR) + "-"
				+ dateAndWeekArray[0];
		String weekString = dateAndWeekArray[1];
		int week = weekStringToInt(weekString);
		Elements stationElements = doc
				.select("aside[class=\"related-aside rt\"] section[class=\"aside-section clear\"] div.bar");
		String stationName = stationElements.get(0).text().trim();
		Elements programElements = doc.select("ul#pgrow li");

		List<ProgramTable> resultList = new ArrayList<ProgramTable>();
		for (Element element : programElements) {
			List<Node> children = element.childNodes();
			int size = children.size();
			if (size < 2) {
				continue;
			}

			int i = 0;
			// 查找节目播出时间
			boolean foundAirTime = false;
			for (; i < size; i++) {
				Node child = children.get(i);
				if (child instanceof Element
						&& "SPAN".equalsIgnoreCase(((Element) child).tagName())) {
					foundAirTime = true;
					break;
				}
			}
			if (!foundAirTime) {
				logger.info("the program table of " + stationName + " at "
						+ date + " does not exists.");
				return resultList;
			}
			String airTime = ((Element) children.get(i++)).text().trim();
			StringBuffer program = new StringBuffer();
			// 查找节目名称
			for (; i < size; i++) {
				Node child = children.get(i);
				if (child instanceof TextNode) {
					program.append(((TextNode) child).text().trim());
				} else if (child instanceof Element
						&& "A".equalsIgnoreCase(((Element) child).tagName())) {
					program.append(((Element) child).text().trim());
					i++;
					break;
				}
			}

			if (i < size - 1) {
				// 还有textnode元素
				Node child = children.get(i);
				if (child instanceof TextNode) {
					program.append(((TextNode) child).text().trim());
				}
			}
			ProgramTable pt = new ProgramTable();
			pt.setAirDate(date);
			pt.setAirTime(date + " " + airTime);
			pt.setProgram(program.toString().trim());
			pt.setStationName(stationName);
			pt.setWeek(week);
			for (CrawlEventListener listener : listeners) {
				listener.itemFound(new ProgramTableFoundEvent(this, pt));
			}
			resultList.add(pt);
		}
		return resultList;
	}

	private int weekStringToInt(String weekString) {
		if (Week.MONDAY.value.equals(weekString)) {
			return 1;
		} else if (Week.TUESDAY.value.equals(weekString)) {
			return 2;
		} else if (Week.WEDNESDAY.value.equals(weekString)) {
			return 3;
		} else if (Week.THURSDAY.value.equals(weekString)) {
			return 4;
		} else if (Week.FRIDAY.value.equals(weekString)) {
			return 5;
		} else if (Week.SATURDAY.value.equals(weekString)) {
			return 6;
		} else if (Week.SUNDAY.value.equals(weekString)) {
			return 7;
		}
		throw new MyTvException("invalid week. " + weekString);
	}

	private final class TvMaoCrawlTask {
		private TvStation tvStation;
		private String date;

		@Override
		public String toString() {
			return "TvMaoCrawlTask [tvStation=" + tvStation + ", date=" + date
					+ "]";
		}

	}

	private final static class TvMaoPageObjectFactory extends
			BaseKeyedPoolableObjectFactory<TvMaoObjectKey, HtmlPage> {

		@Override
		public HtmlPage makeObject(TvMaoObjectKey key) throws Exception {
			Page page = WebCrawler.crawl(key.url);
			if (page.isHtmlPage()) {
				return (HtmlPage) page;
			}
			throw new MyTvException("invalid web page which url: " + key.url);
		}

		@Override
		public void destroyObject(TvMaoObjectKey key, HtmlPage obj)
				throws Exception {
			String today = DateUtils.today();
			if (!key.date.equals(today)) {
				super.destroyObject(key, obj);
			}
		}
	}

	private final static class TvMaoObjectKey {
		private String url;
		private String date;

		public TvMaoObjectKey(String url, String date) {
			super();
			this.url = url;
			this.date = date;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((date == null) ? 0 : date.hashCode());
			result = prime * result + ((url == null) ? 0 : url.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TvMaoObjectKey other = (TvMaoObjectKey) obj;
			if (date == null) {
				if (other.date != null)
					return false;
			} else if (!date.equals(other.date))
				return false;
			if (url == null) {
				if (other.url != null)
					return false;
			} else if (!url.equals(other.url))
				return false;
			return true;
		}
	}

	/**
	 * 生成随机数字
	 * 
	 * @return
	 */
	private long getRandomNumber(int min, int max) {
		return min + RANDOM.nextInt(max) % (max - min + 1);
	}

	private long getRandomSleepTime() {
		return getRandomNumber(0, 30);
	}

	/**
	 * 获取调度频率
	 * 
	 * @return
	 */
	private long getScheduleFrequency() {
		int proxySize = MyTvProxyManager.getInstance().getProxySize();
		if (proxySize == 0) {
			return getRandomNumber(1000, 2000);
		} else {
			return getRandomNumber(1000, 2000) / proxySize;
		}
	}
}
