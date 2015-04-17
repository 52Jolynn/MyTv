package com.laudandjolynn.mytv.datasource;

import java.util.List;

import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月15日 下午12:28:15
 * @copyright: www.laudandjolynn.com
 */
public interface TvDao {
	/**
	 * 获取所有电视台分类
	 * 
	 * @return
	 */
	public List<String> getTvStationClassify();

	/**
	 * 获取指定分类下的所有电视台
	 * 
	 * @param classify
	 * @return
	 */
	public List<TvStation> getTvStationByClassify(String classify);

	/**
	 * 获取所有电视台
	 * 
	 * @return
	 */
	public List<TvStation> getAllStation();

	/**
	 * 根据电视台名称得到电视台对象
	 * 
	 * @param stationName
	 * @return
	 */
	public TvStation getStation(String stationName);

	/**
	 * 根据电视台显示名得到电视台对象
	 * 
	 * @param displayName
	 *            电视台显示名
	 * @param classify
	 *            电视台分类
	 * @return 只有id、name、displayName、classify信息
	 */
	public TvStation getStationByDisplayName(String displayName, String classify);

	/**
	 * 判断电视台是否已经存在，比较stationName，不比较displayName
	 * 
	 * @param stations
	 * @return
	 */
	public boolean[] isStationExists(TvStation... stations);

	/**
	 * 判断电视台是否已经存在
	 * 
	 * @param stationName
	 * @return
	 */
	public boolean isStationExists(String stationName);

	/**
	 * 保存电视台
	 * 
	 * @param stations
	 * @return
	 */
	public int[] save(TvStation... stations);

	/**
	 * 保存电视节目表
	 * 
	 * @param programTables
	 * @return
	 */
	public int[] save(ProgramTable... programTables);

	/**
	 * 根据电视台、日期获取电视节目表
	 * 
	 * @param stationName
	 * @param date
	 * @return
	 */
	public List<ProgramTable> getProgramTable(String stationName, String date);

	/**
	 * 判断指定电视台、日期的电视节目表是否已存在
	 * 
	 * @param stationName
	 * @param date
	 * @return
	 */
	public boolean isProgramTableExists(String stationName, String date);
}
