package com.laudandjolynn.mytvlist.utils;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.rmi.CORBA.Util;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.laudandjolynn.mytvlist.model.ProgramTable;
import com.laudandjolynn.mytvlist.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月27日 下午1:17:02
 * @copyright: www.laudandjolynn.com
 */
public class EpgTask implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(EpgTask.class);
	private String date = null;
	private String stationName = null;

	public EpgTask(String stationName, String date) {
		this.stationName = stationName;
		this.date = date;
	}

	@Override
	public void run() {
		Page page = Crawler.crawl(Constant.EPG_URL);
		if (page.isHtmlPage()) {
			Date date = DateUtils.string2Date(this.date, "yyyy-MM-dd");
			if (date == null) {
				return;
			}
			String searchValue = DateFormatUtils.format(date, "yyyy-MM-dd");
			logger.info("crawl program table of " + searchValue);
			if (Utils.isProgramTableExists(stationName, searchValue)) {
				logger.debug("the TV station's program table of " + stationName
						+ " have been saved in db.");
				return;
			}
			TvStation station = Utils.getStation(this.stationName);
			if (station == null) {
				logger.debug("the TV station " + stationName + " isn't exists.");
				return;
			}
			HtmlPage htmlPage = (HtmlPage) page;
			List<?> ownerElements = htmlPage.getByXPath("//ul[@class='weishi']/li/a");
			
			DomElement element = htmlPage.getElementById("date");
			element.setNodeValue(searchValue);
			element.setTextContent(searchValue);
			List<?> list = htmlPage.getByXPath("//div[@id='search_1']/a");
			HtmlAnchor anchor = (HtmlAnchor) list.get(0);
			try {
				HtmlPage specPage = anchor.click();
				String html = specPage.asXml();
				List<ProgramTable> ptList = EpgParser.parseProgramTable(html);
				ProgramTable[] ptArray = new ProgramTable[ptList.size()];
				Utils.save(ptList.toArray(ptArray));
				Utils.outputCrawlData(searchValue, html);
			} catch (IOException e) {
				throw new MyTvListException(
						"error occur while search program table at spec date: "
								+ this.date, e);
			}
		}
	}
}
