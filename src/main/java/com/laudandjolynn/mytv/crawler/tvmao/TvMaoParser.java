package com.laudandjolynn.mytv.crawler.tvmao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.laudandjolynn.mytv.crawler.Parser;
import com.laudandjolynn.mytv.exception.MyTvException;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;
import com.laudandjolynn.mytv.service.TvService;
import com.laudandjolynn.mytv.service.TvServiceImpl;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月15日 下午3:33:04
 * @copyright: www.laudandjolynn.com
 */
class TvMaoParser implements Parser {
	private TvService tvService = new TvServiceImpl();
	private final static AtomicInteger SEQUENCE = new AtomicInteger(300000);

	private enum Week {
		SUNDAY("星期日"), MONDAY("星期一"), TUESDAY("星期二"), WEDNESDAY("星期三"), THURSDAY(
				"星期四"), FRIDAY("星期五"), SATURDAY("星期六");

		private String value;

		private Week(String value) {
			this.value = value;
		}

	}

	@Override
	public List<TvStation> parseTvStation(String html) {
		Document doc = Jsoup.parse(html);
		Elements classifyElements = doc.select("div.chlsnav div.pbar b");
		String classify = classifyElements.get(0).text().trim();
		List<TvStation> resultList = new ArrayList<TvStation>();
		Elements channelElements = doc.select("div.chlsnav ul.r li");
		for (Element element : channelElements) {
			Element channel = element.child(0);
			TvStation tv = new TvStation();
			String displayName = channel.text().trim();
			String stationName = displayName;
			TvStation station = tvService.getStationByDisplayName(displayName,
					classify);
			if (station != null) {
				stationName = station.getName();
			}
			tv.setName(stationName);
			tv.setDisplayName(displayName);
			tv.setCity(null);
			tv.setClassify(classify);
			tv.setSequence(SEQUENCE.incrementAndGet());
			resultList.add(tv);
		}
		return resultList;
	}

	@Override
	public List<ProgramTable> parseProgramTable(String html) {
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
			for (; i < size; i++) {
				Node child = children.get(i);
				if (child instanceof Element
						&& "SPAN".equalsIgnoreCase(((Element) child).tagName())) {
					break;
				}
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

}
