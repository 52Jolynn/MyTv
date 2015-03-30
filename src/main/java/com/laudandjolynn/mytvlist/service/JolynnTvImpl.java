package com.laudandjolynn.mytvlist.service;

import java.util.List;

import org.json.JSONArray;

import com.laudandjolynn.mytvlist.epg.EpgService;
import com.laudandjolynn.mytvlist.epg.EpgTaskManager;
import com.laudandjolynn.mytvlist.model.ProgramTable;
import com.laudandjolynn.mytvlist.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 下午6:20:06
 * @copyright: www.laudandjolynn.com
 */
public class JolynnTvImpl implements JolynnTv {

	@Override
	public String getTvStationClassify() {
		List<String> classifies = EpgService.getTvStationClassify();
		JSONArray array = new JSONArray(classifies);
		return array.toString();
	}

	@Override
	public String getAllTvStation() {
		List<TvStation> stations = EpgService.getAllStation();
		JSONArray array = new JSONArray(stations);
		return array.toString();
	}

	@Override
	public String getProgramTable(String name, String date) {
		List<ProgramTable> ptList = EpgTaskManager.getIntance()
				.queryProgramTable(name, date);
		JSONArray array = new JSONArray(ptList);
		return array.toString();
	}

}
