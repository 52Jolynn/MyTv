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
import java.util.concurrent.Future;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlBold;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.laudandjolynn.mytv.crawler.AbstractCrawler;
import com.laudandjolynn.mytv.crawler.Parser;
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
class TvMaoCrawler extends AbstractCrawler {
	private final static Logger logger = LoggerFactory
			.getLogger(TvMaoCrawler.class);
	// tvmao节目表地址
	private final static String TV_MAO_URL_PREFIX = "http://www.tvmao.com";
	private final static String TV_MAO_URL = TV_MAO_URL_PREFIX
			+ "/program/channels";
	private final static String TV_MAO_NAME = "tvmao";

	public TvMaoCrawler(Parser parser) {
		super(parser);
	}

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
		ExecutorService executorService = Executors
				.newFixedThreadPool(Constant.CPU_PROCESSOR_NUM);
		CompletionService<List<TvStation>> stationCompletionService = new ExecutorCompletionService<List<TvStation>>(
				executorService);
		int size = 0;
		List<TvStation> resultList = new ArrayList<TvStation>();
		if (file.exists() && file.listFiles().length > 0) {
			logger.info("crawl all tv station from files.");
			File[] files = file.listFiles();
			size = files == null ? 0 : files.length;
			for (int i = 0; i < size; i++) {
				final File f = files[i];
				Callable<List<TvStation>> task = new Callable<List<TvStation>>() {
					@Override
					public List<TvStation> call() throws Exception {
						try {
							String html = MyTvUtils.readAsHtml(f.getPath());
							logger.debug("parse tv station file: "
									+ f.getPath());
							return parser.parseTvStation(html);
						} catch (IOException e) {
							logger.error("read as xml error: " + f.getPath(), e);
						}
						return null;
					}
				};
				stationCompletionService.submit(task);
			}
		} else {
			logger.info("crawl all tv station from " + getUrl() + ".");
			try {
				Thread.sleep(generateRandomSleepTime());
			} catch (InterruptedException e) {
				// do nothing
			}
			final HtmlPage htmlPage = (HtmlPage) WebCrawler.crawl(getUrl());
			List<?> elements = htmlPage
					.getByXPath("//div[@class='pgnav_wrap']/table[@class='pgnav']//a");
			int ssize = size = elements == null ? 0 : elements.size();
			for (int i = 0; i < ssize; i++) {
				final HtmlAnchor anchor = (HtmlAnchor) elements.get(i);
				if (!anchor.getAttribute("href").startsWith("/program/")) {
					size--;
					continue;
				}
				final String city = anchor.getTextContent().trim();
				if ("CCTV".equals(city)) {
					Callable<List<TvStation>> task = new Callable<List<TvStation>>() {
						@Override
						public List<TvStation> call() throws Exception {
							logger.debug("a city program table of tvmao: "
									+ city + ", url: "
									+ anchor.getHrefAttribute());
							List<TvStation> stationList = new ArrayList<TvStation>();
							stationList.addAll(getTvStations(htmlPage, city));
							stationList.addAll(getAllTvStationOfCity(htmlPage,
									city));
							return stationList;
						}
					};
					stationCompletionService.submit(task);
				} else {
					Callable<List<TvStation>> task = new Callable<List<TvStation>>() {
						@Override
						public List<TvStation> call() throws Exception {
							String href = anchor.getHrefAttribute();
							logger.debug("a city program table of tvmao: "
									+ city + ", url: " + href);
							try {
								Thread.sleep(generateRandomSleepTime());
							} catch (InterruptedException e) {
								// do nothing
							}
							HtmlPage hp = (HtmlPage) WebCrawler
									.crawl(TV_MAO_URL_PREFIX + href);
							List<TvStation> stationList = new ArrayList<TvStation>();
							stationList.addAll(getTvStations(hp, city));
							stationList.addAll(getAllTvStationOfCity(hp, city));
							return stationList;
						}
					};
					stationCompletionService.submit(task);
				}
			}
		}
		int count = 0;
		while (count < size) {
			try {
				Future<List<TvStation>> future = stationCompletionService
						.take();
				List<TvStation> stationList = future.get();
				if (stationList != null) {
					resultList.addAll(stationList);
				}
			} catch (InterruptedException e) {
				logger.error("crawl task of all station was interrupted.", e);
			} catch (ExecutionException e) {
				logger.error(
						"error occur while execute crawl task of all station.",
						e);
			}
			count++;
		}
		executorService.shutdown();
		return resultList;
	}

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

	private List<TvStation> getTvStations(HtmlPage htmlPage, String city) {
		String html = htmlPage.asXml();
		List<?> elements = htmlPage
				.getByXPath("//div[@class='chlsnav']/div[@class='pbar']/b");
		HtmlBold hb = (HtmlBold) elements.get(0);
		String classify = hb.getTextContent().trim();
		List<TvStation> stationList = parser.parseTvStation(html);
		for (TvStation station : stationList) {
			station.setCity(city);
		}
		logger.debug("tv station found." + stationList);
		MyTvUtils.outputCrawlData(getCrawlerName(), html, getCrawlerName()
				+ Constant.UNDERLINE + classify);
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
		String stationName = station.getName();
		String queryDate = DateUtils.date2String(dateObj, "yyyy-MM-dd");
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
		List<ProgramTable> ptList = parser.parseProgramTable(html);
		MyTvUtils.outputCrawlData(queryDate, html, queryDate
				+ Constant.UNDERLINE + getCrawlerName() + Constant.UNDERLINE
				+ stationName);
		return ptList;
	}

	@Override
	public boolean exists(TvStation station) {
		String city = station.getCity();
		String classify = station.getClassify();
		if (city == null || classify == null) {
			return false;
		}
		String tvMaoFile = Constant.CRAWL_FILE_PATH + getCrawlerName()
				+ File.separator + getCrawlerName() + Constant.UNDERLINE
				+ classify;
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
				try {
					Thread.sleep(generateRandomSleepTime());
				} catch (Exception e) {
					// do nothing
				}
				MyTvUtils.outputCrawlData(getCrawlerName(), htmlPage.asXml(),
						getCrawlerName() + Constant.UNDERLINE + classify);
				return true;
			}
		} catch (Exception e) {
			logger.error(
					"error occur while search station: " + station.getName(), e);
			return false;
		}

		return false;
	}

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
	 * 生成随机休眠时间
	 * 
	 * @return
	 */
	private long generateRandomSleepTime() {
		Random random = new Random();
		int min = 1000;
		int max = 10000;
		return min + random.nextInt(max) % (max - min + 1);
	}
}
