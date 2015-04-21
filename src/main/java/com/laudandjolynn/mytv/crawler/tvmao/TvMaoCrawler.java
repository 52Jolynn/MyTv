package com.laudandjolynn.mytv.crawler.tvmao;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.dom4j.DocumentException;
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
				.newFixedThreadPool(Constant.CPU_PROCESSOR_NUM * 2);
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
							String html = MyTvUtils.readAsXml(f);
							logger.debug("parse tv station file: "
									+ f.getPath());
							return parser.parseTvStation(html);
						} catch (DocumentException e) {
							logger.error("read as xml error: " + f.getPath(), e);
						}
						return null;
					}
				};
				stationCompletionService.submit(task);
			}
		} else {
			logger.info("crawl all tv station from " + getUrl() + ".");
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
				Future<List<TvStation>> future = stationCompletionService.poll(
						5, TimeUnit.MINUTES);
				if (future != null) {
					List<TvStation> stationList = future.get();
					if (stationList != null) {
						resultList.addAll(stationList);
					}
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
		String classify = hb.getTextContent();
		List<TvStation> stationList = parser.parseTvStation(html);
		for (TvStation station : stationList) {
			station.setCity(city);
		}
		logger.debug("some tv station found." + stationList);
		MyTvUtils.outputCrawlData(getCrawlerName(), html, getCrawlerName()
				+ Constant.UNDERLINE + classify);
		return stationList;
	}

	@Override
	public List<ProgramTable> crawlProgramTable(String date, TvStation station) {
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
		HtmlPage htmlPage = (HtmlPage) WebCrawler.crawl(TV_MAO_URL);
		String city = station.getCity();
		if (city == null) {
			return null;
		}
		try {
			htmlPage = searchStation(htmlPage, station);
		} catch (Exception e) {
			logger.error("error occur while search station: " + stationName, e);
			return null;
		}

		if (htmlPage == null) {
			logger.debug(TV_MAO_URL + " is an empty page.");
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
					if (value.matches("\\)$")
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
		List<ProgramTable> ptList = parser.parseProgramTable(html);
		MyTvUtils.outputCrawlData(queryDate, html, queryDate
				+ Constant.UNDERLINE + stationName);
		return ptList;
	}

	@Override
	public boolean exists(TvStation station) {
		String city = station.getCity();
		if (city == null) {
			return false;
		}
		String tvMaoFile = Constant.CRAWL_FILE_PATH + getCrawlerName();
		File file = new File(tvMaoFile);
		if (file.exists()) {
			File[] files = file.listFiles();
			for (File f : files) {
				String html;
				try {
					html = MyTvUtils.readAsXml(f);
				} catch (DocumentException e) {
					continue;
				}
				Document doc = Jsoup.parse(html);
				Elements classifyElements = doc
						.select("div.chlsnav div.pbar b");
				String classify = classifyElements.get(0).text().trim();
				Elements channelElements = doc.select("div.chlsnav ul.r li");
				for (Element element : channelElements) {
					Element channel = element.child(0);
					String displayName = channel.text().trim();
					if (classify == null) {
						if (station.getClassify() != null) {
							continue;
						}
					} else if (displayName.equals(station.getName())) {
						return true;
					}
				}
			}
		}

		HtmlPage htmlPage = (HtmlPage) WebCrawler.crawl(TV_MAO_URL);
		try {
			if ((htmlPage = searchStation(htmlPage, station)) != null) {
				MyTvUtils.outputCrawlData(
						getCrawlerName(),
						htmlPage.asXml(),
						getCrawlerName() + Constant.UNDERLINE
								+ station.getClassify());
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
		List<?> elements = htmlPage
				.getByXPath("//div[@class='pgnav_wrap']/table[@class='pgnav']//a");
		int ssize = elements == null ? 0 : elements.size();
		for (int i = 0; i < ssize; i++) {
			final HtmlAnchor anchor = (HtmlAnchor) elements.get(i);
			if (!anchor.getAttribute("href").startsWith("/program/")) {
				continue;
			} else if (city.equals(anchor.getTextContent().trim())) {
				String href = anchor.getHrefAttribute();
				htmlPage = (HtmlPage) WebCrawler
						.crawl(TV_MAO_URL_PREFIX + href);
				break;
			}
		}
		List<?> classifyElements = htmlPage
				.getByXPath("//div[@class='chlsnav']//div[@class='plst']/parent::*");
		String classify = station.getClassify();
		for (int i = 0, size = classifyElements == null ? 0 : classifyElements
				.size(); i < size; i++) {
			HtmlAnchor anchor = (HtmlAnchor) elements.get(i);
			if (classify.equals(anchor.getTextContent().trim())) {
				String href = anchor.getHrefAttribute();
				htmlPage = (HtmlPage) WebCrawler
						.crawl(TV_MAO_URL_PREFIX + href);
				break;
			}
		}

		String stationName = station.getName();
		List<?> stationElements = htmlPage
				.getByXPath("//div[@class='chlsnav']//ul[@class='r']//li");
		for (int i = 0, size = stationElements == null ? 0 : stationElements
				.size(); i < size; i++) {
			DomElement element = ((DomElement) stationElements.get(i))
					.getFirstElementChild();
			if (stationName.equals(element.getTextContent().trim())) {
				if (element instanceof HtmlAnchor) {
					String href = ((HtmlAnchor) element).getHrefAttribute();
					return (HtmlPage) WebCrawler
							.crawl(TV_MAO_URL_PREFIX + href);
				}
				break;
			}
		}
		return null;
	}

}
