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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private final static BlockingQueue<TvMaoCrawlTask> TV_MAO_PROGRAM_TABLE_CRAWL_QUEUE = new ArrayBlockingQueue<TvMaoCrawler.TvMaoCrawlTask>(
			2);

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
		try {
			Thread.sleep(generateRandomSleepTime());
		} catch (InterruptedException e) {
			// do nothing
		}
		final HtmlPage htmlPage = (HtmlPage) WebCrawler.crawl(getUrl());
		List<?> elements = htmlPage
				.getByXPath("//div[@class='pgnav_wrap']/table[@class='pgnav']//a");
		int size = elements == null ? 0 : elements.size();
		for (int i = 0; i < size; i++) {
			final HtmlAnchor anchor = (HtmlAnchor) elements.get(i);
			if (!anchor.getAttribute("href").startsWith("/program/")) {
				continue;
			}
			final String city = anchor.getTextContent().trim();
			if ("CCTV".equals(city)) {
				logger.debug("a city program table of tvmao: " + city
						+ ", url: " + anchor.getHrefAttribute());
				resultList.addAll(getTvStations(htmlPage, city));
				resultList.addAll(getAllTvStationOfCity(htmlPage, city));
			} else {
				String href = anchor.getHrefAttribute();
				logger.debug("a city of tvmao: " + city + ", url: " + href);
				try {
					Thread.sleep(generateRandomSleepTime());
				} catch (InterruptedException e) {
					// do nothing
				}
				HtmlPage hp = (HtmlPage) WebCrawler.crawl(TV_MAO_URL_PREFIX
						+ href);
				resultList.addAll(getTvStations(hp, city));
				resultList.addAll(getAllTvStationOfCity(hp, city));
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
				.namingPattern("Mytv crawl all tv station of tvmao[%d]")
				.build();
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
			HtmlAnchor anchor = (HtmlAnchor) elements.get(i);
			String href = anchor.getHrefAttribute();
			logger.debug(anchor.getTextContent() + " program table of tvmao: "
					+ ", url: " + href);
			try {
				Thread.sleep(generateRandomSleepTime());
			} catch (InterruptedException e) {
				// do nothing
			}
			HtmlPage p = (HtmlPage) WebCrawler.crawl(TV_MAO_URL_PREFIX + href);
			resultList.addAll(getTvStations(p, city));
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
		TvMaoCrawlTask task = new TvMaoCrawlTask();
		task.date = queryDate;
		task.tvStation = station;
		try {
			logger.debug("crawl task: " + task
					+ ", of tv mao program table queue size: "
					+ TV_MAO_PROGRAM_TABLE_CRAWL_QUEUE.size());
			TV_MAO_PROGRAM_TABLE_CRAWL_QUEUE.put(task);
		} catch (InterruptedException e) {
			TV_MAO_PROGRAM_TABLE_CRAWL_QUEUE.remove(task);
		}
		task = TV_MAO_PROGRAM_TABLE_CRAWL_QUEUE.peek();

		if (task != null) {
			try {
				List<ProgramTable> resultList = crawlProgramTable(task);
				return resultList;
			} finally {
				logger.debug("remove task: " + task + " from queue.");
				TV_MAO_PROGRAM_TABLE_CRAWL_QUEUE.remove(task);
				logger.debug("crawl task of tv mao program table queue: "
						+ TV_MAO_PROGRAM_TABLE_CRAWL_QUEUE.size());
			}
		}
		return null;
	}

	private List<ProgramTable> crawlProgramTable(TvMaoCrawlTask task) {
		TvStation station = task.tvStation;
		String queryDate = task.date;
		String stationName = station.getName();

		logger.info("crawl program table of " + stationName + " at "
				+ queryDate);
		try {
			Thread.sleep(generateRandomSleepTime());
		} catch (InterruptedException e) {
			// do nothing
		}
		HtmlPage htmlPage = (HtmlPage) WebCrawler.crawl(TV_MAO_URL);
		try {
			htmlPage = searchStation(htmlPage, station);
			try {
				Thread.sleep(generateRandomSleepTime());
			} catch (Exception e) {
				// do nothing
			}
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
						try {
							Thread.sleep(generateRandomSleepTime());
						} catch (InterruptedException e) {
							// do nothing
						}
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

		try {
			Thread.sleep(generateRandomSleepTime());
		} catch (InterruptedException e) {
			// do nothing
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
				try {
					Thread.sleep(generateRandomSleepTime());
				} catch (InterruptedException e) {
					// do nothing
				}
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
					try {
						Thread.sleep(generateRandomSleepTime());
					} catch (InterruptedException e) {
						// do nothing
					}
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
					try {
						Thread.sleep(generateRandomSleepTime());
					} catch (InterruptedException e) {
						// do nothing
					}
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

	/**
	 * 生成随机休眠时间
	 * 
	 * @return
	 */
	private long generateRandomSleepTime() {
		Random random = new Random();
		int min = 500;
		int max = 1000;
		return min + random.nextInt(max) % (max - min + 1);
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

	private class TvMaoCrawlTask {
		private TvStation tvStation;
		private String date;

		@Override
		public String toString() {
			return "TvMaoCrawlTask [tvStation=" + tvStation + ", date=" + date
					+ "]";
		}

	}

}
