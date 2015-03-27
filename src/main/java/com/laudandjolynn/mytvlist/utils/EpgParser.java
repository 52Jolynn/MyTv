package com.laudandjolynn.mytvlist.utils;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.laudandjolynn.mytvlist.model.ProgramTable;
import com.laudandjolynn.mytvlist.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月27日 上午10:40:10
 * @copyright: www.laudandjolynn.com
 */
public class EpgParser {
	/**
	 * 解析电视台列表
	 * 
	 * @param html
	 * @return
	 */
	public static List<TvStation> parseTvStation(String html) {
		Document doc = Jsoup.parse(html);
		Elements ownerElements = doc.select("ul.weishi a[href]");
		Elements stationElements = doc.select("div.md_left_right");
		List<TvStation> resultList = new ArrayList<TvStation>();
		for (int i = 0, size = ownerElements == null ? 0 : ownerElements.size(); i < size; i++) {
			Element ownerElement = ownerElements.get(i);
			String owner = ownerElement.text();
			Element stationElement = stationElements.get(i);
			Elements stationTextElements = stationElement
					.select("dl h3 a[href]");
			for (int j = 0, ssize = stationTextElements == null ? 0
					: stationTextElements.size(); j < ssize; j++) {
				TvStation tv = new TvStation();
				tv.setName(stationTextElements.get(j).text());
				tv.setOwner(owner);
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
	public static List<ProgramTable> parseProgramTable(String html) {
		Document doc = Jsoup.parse(html);
		List<ProgramTable> resultList = new ArrayList<ProgramTable>();
		Elements channelElements = doc.select("#channelTitle");
		String channel = channelElements.get(0).text();
		Elements weekElements = doc.select("#week li[rel]");
		int week = 0;
		String date = null;
		for (int i = 0, size = weekElements == null ? 0 : weekElements.size(); i < size; i++) {
			Element element = weekElements.get(i);
			if (element.hasClass("cur")) {
				week = i;
				date = element.attr("rel");
				break;
			}
		}
		Elements programElemens = doc
				.select("#epg_list div.content_c dl dd a.p_name_a");
		for (int i = 0, size = programElemens == null ? 0 : programElemens
				.size(); i < size; i++) {
			Element programElement = programElemens.get(i);
			String programContent = programElement.text();
			String[] pc = programContent.split("\\s+");
			ProgramTable pt = new ProgramTable();
			pt.setAirTime(date + " " + pc[0] + ":00");
			pt.setProgram(pc[1]);
			pt.setStationName(channel);
			pt.setWeek(week);
			resultList.add(pt);
		}
		return resultList;
	}

}
