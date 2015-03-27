package com.laudandjolynn.mytvlist.epg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.laudandjolynn.mytvlist.Crawler;
import com.laudandjolynn.mytvlist.exception.MyTvListException;
import com.laudandjolynn.mytvlist.model.ProgramTable;
import com.laudandjolynn.mytvlist.model.TvStation;
import com.laudandjolynn.mytvlist.utils.Constant;
import com.laudandjolynn.mytvlist.utils.DateUtils;
import com.laudandjolynn.mytvlist.utils.Utils;

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
	 * 获取指定日期的所有电视台节目表
	 * 
	 * @param date
	 *            日期，yyyy-MM-dd
	 * @return
	 */
	public static List<ProgramTable> crawlAllProgramTable(String date) {
		List<TvStation> stations = Utils.getAllStation();
		List<ProgramTable> resultList = new ArrayList<ProgramTable>();
		for (TvStation station : stations) {
			resultList.addAll(crawlProgramTable(station, date));
		}
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
		TvStation station = Utils.getStation(stationName);
		return crawlProgramTable(station, date);
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
		if (page.isHtmlPage()) {
			Date dateObj = DateUtils.string2Date(date, "yyyy-MM-dd");
			if (dateObj == null) {
				return null;
			}
			String searchValue = DateFormatUtils.format(dateObj, "yyyy-MM-dd");
			logger.info("crawl program table of " + searchValue);
			String stationName = station.getName();
			if (Utils.isProgramTableExists(stationName, searchValue)) {
				logger.debug("the TV station's program table of " + stationName
						+ " have been saved in db.");
				return null;
			}

			HtmlPage htmlPage = (HtmlPage) page;
			List<?> classifyElements = htmlPage
					.getByXPath("//ul[@class='weishi']/li/a");
			for (Object element : classifyElements) {
				HtmlAnchor anchor = (HtmlAnchor) element;
				if (station.getClassify().equals(anchor.getTextContent())) {
					try {
						htmlPage = anchor.click();
					} catch (IOException e) {
						throw new MyTvListException(
								"error occur while search program table of "
										+ stationName + " at spec date: "
										+ date, e);
					}
					break;
				}
			}
			DomElement element = htmlPage.getElementById("date");
			element.setNodeValue(searchValue);
			element.setTextContent(searchValue);
			List<?> list = htmlPage.getByXPath("//div[@id='search_1']/a");
			HtmlAnchor anchor = (HtmlAnchor) list.get(0);
			try {
				HtmlPage specPage = anchor.click();
				String html = specPage.asXml();
				List<ProgramTable> ptList = EpgParser.parseProgramTable(html);
				Utils.outputCrawlData(searchValue, html);
				return ptList;
			} catch (IOException e) {
				throw new MyTvListException(
						"error occur while search program table of "
								+ stationName + " at spec date: " + date, e);
			}
		}
		return null;
	}
}
