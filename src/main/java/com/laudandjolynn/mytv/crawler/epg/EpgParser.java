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

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.laudandjolynn.mytv.crawler.Parser;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;
import com.laudandjolynn.mytv.service.TvService;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月27日 上午10:40:10
 * @copyright: www.laudandjolynn.com
 */
class EpgParser implements Parser {
	private final static String CITY = "城市";
	TvService tvService = new TvService();

	/**
	 * 解析电视台列表
	 * 
	 * @param html
	 * @return
	 */
	@Override
	public List<TvStation> parseTvStation(String html) {
		Document doc = Jsoup.parse(html);
		Elements classifyElements = doc.select("ul.weishi a[href]");
		Elements stationElements = doc.select("div.md_left_right");
		List<TvStation> resultList = new ArrayList<TvStation>();
		int sequence = 10000;

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
				String displayName = stationTextElements.get(j).text().trim();
				String stationName = displayName;
				TvStation station = tvService.getStationByDisplayName(
						displayName, classify);
				if (station != null) {
					stationName = station.getName();
				}
				tv.setName(stationName);
				tv.setDisplayName(displayName);
				tv.setCity(null);
				tv.setClassify(classify);
				tv.setSequence(++sequence);
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
				String name = cityStationElements.get(j).text().trim();
				tv.setName(name);
				tv.setDisplayName(name);
				tv.setCity(cityElement.text().trim());
				tv.setClassify(CITY);
				tv.setSequence(++sequence);
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
	@Override
	public List<ProgramTable> parseProgramTable(String html) {
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
			if (tvService.isStationExists(stationName)) {
				pt.setStation(tvService.getStation(stationName).getId());
			} else {
				pt.setStation(tvService.getStation(stationName).getId());
			}
			pt.setStationName(stationName);
			pt.setWeek(week);
			resultList.add(pt);
		}
		return resultList;
	}

}
