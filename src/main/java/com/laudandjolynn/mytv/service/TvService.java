package com.laudandjolynn.mytv.service;

import java.util.List;

import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月17日 上午9:31:17
 * @copyright: www.laudandjolynn.com
 */
public interface TvService {
	/**
	 * 保存电视台
	 * 
	 * @param stations
	 */
	public void save(TvStation... stations);

	/**
	 * 判断电视台是否存在
	 * 
	 * @param stationName
	 * @return
	 */
	public boolean isStationExists(String stationName);

	/**
	 * 保存电视节目表
	 * 
	 * @param programTables
	 */
	public void save(ProgramTable... programTables);

	/**
	 * 获取所有电视台分类
	 * 
	 * @return
	 */
	public List<String> getTvStationClassify();

	/**
	 * 获取所有电视台
	 * 
	 * @return
	 */
	public List<TvStation> getAllStation();

	/**
	 * 根据名称获取电视台对象
	 * 
	 * @param stationName
	 * @return
	 */
	public TvStation getStation(String stationName);

	/**
	 * 根据显示名称获取电视台对象
	 * 
	 * @param displayName
	 * @param classify
	 * @return
	 */
	public TvStation getStationByDisplayName(String displayName, String classify);

	/**
	 * 根据电视台名称，日期获取电视节目表
	 * 
	 * @param stationName
	 *            电视台名称
	 * @param date
	 *            日期, yyyy-MM-dd
	 * @return
	 */
	public List<ProgramTable> getProgramTable(String stationName, String date);

	/**
	 * 根据电视台分类获取电视台
	 * 
	 * @param classify
	 * @return
	 */
	public List<TvStation> getTvStationByClassify(String classify);

	/**
	 * 判断指定电视台、日期的电视节目表是否已存在
	 * 
	 * @param stationName
	 *            电视台名称
	 * @param date
	 *            日期, yyyy-MM-dd
	 * @return
	 */
	public boolean isProgramTableExists(String stationName, String date);

	/**
	 * 抓取所有电视台
	 * 
	 * @return
	 */
	public List<TvStation> crawlAllTvStation();

	/**
	 * 根据名称、日期抓取电视节目表
	 * 
	 * @param stationName
	 *            电视台名称
	 * @param date
	 *            日期, yyyy-MM-dd
	 * @return
	 */
	public List<ProgramTable> crawlProgramTable(String stationName, String date);

	/**
	 * 删除指定日期的电视节目表
	 * 
	 * @param date
	 * @return
	 */
	public boolean deleteProgramTable(String date);

	/**
	 * 删除指定日期、名称的电视节目表
	 * 
	 * @param stationName
	 * @param date
	 * @return
	 */
	public boolean deleteProgramTable(String stationName, String date);
}
