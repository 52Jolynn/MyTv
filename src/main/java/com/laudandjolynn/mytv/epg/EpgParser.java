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
package com.laudandjolynn.mytv.epg;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;

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
		Elements classifyElements = doc.select("ul.weishi a[href]");
		Elements stationElements = doc.select("div.md_left_right");
		List<TvStation> resultList = new ArrayList<TvStation>();
		for (int i = 0, size = classifyElements == null ? 0 : classifyElements
				.size(); i < size; i++) {
			Element classifyElement = classifyElements.get(i);
			String classify = classifyElement.text();
			Element stationElement = stationElements.get(i);
			Elements stationTextElements = stationElement
					.select("dl h3 a[href]");
			for (int j = 0, ssize = stationTextElements == null ? 0
					: stationTextElements.size(); j < ssize; j++) {
				TvStation tv = new TvStation();
				tv.setName(stationTextElements.get(j).text());
				tv.setClassify(classify);
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
				week = i + 1;
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
