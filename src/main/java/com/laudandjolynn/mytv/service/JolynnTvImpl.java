package com.laudandjolynn.mytv.service;

import java.rmi.RemoteException;
import java.util.List;

import org.json.JSONArray;

import com.laudandjolynn.mytv.epg.EpgService;
import com.laudandjolynn.mytv.epg.EpgTaskManager;
import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月8日 上午11:03:39
 * @copyright: www.laudandjolynn.com
 */
public class JolynnTvImpl implements JolynnTv {

	@Override
	public String getTvStationClassify() throws RemoteException {
		List<String> classifies = EpgService.getTvStationClassify();
		JSONArray array = new JSONArray(classifies);
		return array.toString();
	}

	@Override
	public String getAllTvStation() throws RemoteException {
		List<TvStation> stations = EpgService.getAllStation();
		JSONArray array = new JSONArray(stations);
		return array.toString();
	}

	@Override
	public String getProgramTable(String name, String date)
			throws RemoteException {
		List<ProgramTable> ptList = EpgTaskManager.getIntance()
				.queryProgramTable(name, date);
		JSONArray array = new JSONArray(ptList);
		return array.toString();
	}

	@Override
	public String getTvStationByClassify(String classify)
			throws RemoteException {
		List<TvStation> stationList = EpgService
				.getTvStationByClassify(classify);
		JSONArray array = new JSONArray(stationList);
		return array.toString();
	}
}
