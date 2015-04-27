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
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.laudandjolynn.mytv.crawler.AbstractCrawler;
import com.laudandjolynn.mytv.event.AllTvStationCrawlEndEvent;
import com.laudandjolynn.mytv.event.CrawlEventListener;
import com.laudandjolynn.mytv.event.ProgramTableCrawlEndEvent;
import com.laudandjolynn.mytv.event.ProgramTableFoundEvent;
import com.laudandjolynn.mytv.event.TvStationFoundEvent;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;
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
public class EpgCrawler extends AbstractCrawler {
	private final static Logger logger = LoggerFactory
			.getLogger(EpgCrawler.class);
	// cntv节目表地址
	private final static String EPG_URL = "http://tv.cntv.cn/epg";
	private final static String EPG_NAME = "epg";
	private final static AtomicInteger SEQUENCE = new AtomicInteger(200000);
	private final static String CITY = "城市";

	@Override
	public String getCrawlerName() {
		return EPG_NAME;
	}

	@Override
	public String getUrl() {
		return EPG_URL;
	}

	@Override
	public List<TvStation> crawlAllTvStation() {
		String epgFile = getCrawlFilePath();
		File file = new File(epgFile);
		String html = null;
		if (file.exists()) {
			try {
				html = MyTvUtils.readAsHtml(epgFile);
				return parseTvStation(html);
			} catch (IOException e) {
				// do nothing
			}
			return null;
		}
		HtmlPage htmlPage = (HtmlPage) WebCrawler.crawl(getUrl());
		html = htmlPage.asXml();
		MyTvUtils.outputCrawlData(getCrawlerName(), html, getCrawlFileName());
		List<TvStation> stationList = parseTvStation(html);
		for (CrawlEventListener listener : listeners) {
			listener.crawlEnd(new AllTvStationCrawlEndEvent(this, stationList));
		}
		return stationList;
	}

	/**
	 * 根据电视台、日期获取电视节目表
	 * 
	 * @param date
	 *            日期，yyyy-MM-dd
	 * @param stationName
	 *            电视台名称
	 * @return
	 */
	@Override
	public List<ProgramTable> crawlProgramTable(String date, TvStation station) {
		if (station == null || date == null) {
			logger.debug("station name or date is null.");
			return null;
		}
		List<ProgramTable> ptList = crawlProgramTable(station, date);
		for (CrawlEventListener listener : listeners) {
			listener.crawlEnd(new ProgramTableCrawlEndEvent(this, ptList,
					station.getName(), date));
		}
		return ptList;
	}

	@Override
	public boolean exists(TvStation station) {
		String epgFile = getCrawlFilePath();
		File file = new File(epgFile);
		String city = station.getCity();
		String stationName = station.getName();
		if (file.exists()) {
			String html = null;
			try {
				html = MyTvUtils.readAsHtml(epgFile);
			} catch (IOException e) {
				return false;
			}
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
			return false;
		}
		HtmlPage htmlPage = (HtmlPage) WebCrawler.crawl(EPG_URL);
		MyTvUtils.outputCrawlData(getCrawlerName(), htmlPage.asXml(),
				getCrawlFileName());
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

	/**
	 * 抓取指定日期、电视台的节目表
	 * 
	 * @param station
	 *            电视台对象
	 * @param date
	 *            日期，yyyy-MM-dd
	 * @return
	 */
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
		HtmlPage htmlPage = (HtmlPage) WebCrawler.crawl(EPG_URL);
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
		MyTvUtils.outputCrawlData(queryDate, html, queryDate
				+ Constant.UNDERLINE + getCrawlerName() + Constant.UNDERLINE
				+ stationName);
		List<ProgramTable> ptList = parseProgramTable(html);
		return ptList;
	}

	/**
	 * 解析电视台列表
	 * 
	 * @param html
	 * @return
	 */
	private List<TvStation> parseTvStation(String html) {
		Document doc = Jsoup.parse(html);
		Elements classifyElements = doc.select("ul.weishi a[href]");
		Elements stationElements = doc.select("div.md_left_right");
		List<TvStation> resultList = new ArrayList<TvStation>();

		for (int i = 0, size = classifyElements == null ? 0 : classifyElements
				.size(); i < size; i++) {
			Element classifyElement = classifyElements.get(i);
			String classify = classifyElement.text().trim();
			if (CITY.equals(classify)) {
				continue;
			}
			Element stationElement = stationElements.get(i);
			Elements stationTextElements = stationElement
					.select("dl h3 a.channel");
			for (int j = 0, ssize = stationTextElements == null ? 0
					: stationTextElements.size(); j < ssize; j++) {
				TvStation tv = new TvStation();
				String stationName = stationTextElements.get(j).text().trim();
				tv.setName(stationName);
				tv.setCity(null);
				tv.setClassify(classify);
				tv.setSequence(SEQUENCE.incrementAndGet());
				for (CrawlEventListener listener : listeners) {
					listener.itemFound(new TvStationFoundEvent(this, tv));
				}
				resultList.add(tv);
			}
		}
		Elements cityElements = stationElements.select("dl#cityList dd");
		for (int i = 0, size = cityElements == null ? 0 : cityElements.size(); i < size; i++) {
			Element cityElement = cityElements.get(i).select("h3 a[href]")
					.get(0);
			Elements cityStationElements = cityElements.get(i).select(
					"div.lv3 p a.channel");
			for (int j = 0, ssize = cityStationElements == null ? 0
					: cityStationElements.size(); j < ssize; j++) {
				TvStation tv = new TvStation();
				String stationName = cityStationElements.get(j).text().trim();
				tv.setName(stationName);
				tv.setCity(cityElement.text().trim());
				tv.setClassify(CITY);
				tv.setSequence(SEQUENCE.incrementAndGet());
				for (CrawlEventListener listener : listeners) {
					listener.itemFound(new TvStationFoundEvent(this, tv));
				}
				resultList.add(tv);
			}
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
		List<ProgramTable> resultList = new ArrayList<ProgramTable>();
		Elements channelElements = doc.select("#channelTitle");
		String stationName = channelElements.get(0).text().trim();
		Elements weekElements = doc.select("#week li[rel]");
		int week = 0;
		String date = null;
		for (int i = 0, size = weekElements == null ? 0 : weekElements.size(); i < size; i++) {
			Element element = weekElements.get(i);
			if (element.hasClass("cur")) {
				week = i + 1;
				date = element.attr("rel").trim();
				break;
			}
		}
		Elements programElemens = doc.select("#epg_list div.content_c dl dd")
				.select("a.p_name_a, a.p_name");
		for (int i = 0, size = programElemens == null ? 0 : programElemens
				.size(); i < size; i++) {
			Element programElement = programElemens.get(i);
			String programContent = programElement.text().trim();
			String[] pc = programContent.split("\\s+");
			ProgramTable pt = new ProgramTable();
			pt.setAirDate(date);
			pt.setAirTime(date + " " + pc[0] + ":00");
			pt.setProgram(pc[1]);
			pt.setStationName(stationName);
			pt.setWeek(week);
			for (CrawlEventListener listener : listeners) {
				listener.itemFound(new ProgramTableFoundEvent(this, pt));
			}
			resultList.add(pt);
		}
		return resultList;
	}

	/**
	 * 取得将被存储的抓取文件路径
	 * 
	 * @return
	 */
	private String getCrawlFilePath() {
		return Constant.CRAWL_FILE_PATH + getCrawlerName() + File.separator
				+ getCrawlFileName();
	}

	/**
	 * 取得将被存储的抓取文件名
	 * 
	 * @return
	 */
	private String getCrawlFileName() {
		return getCrawlerName();
	}

}
