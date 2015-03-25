package com.laudandjolynn.mytvlist.service;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 下午6:18:24
 * @copyright: www.laudandjolynn.com
 */
public interface JolynnTv {
	/**
	 * 获取所有电视台
	 * 
	 * @return
	 */
	public String getTvStations();

	/**
	 * 获取指定电视台、日期的节目表
	 * 
	 * @param name
	 * @param date
	 * @return
	 */
	public String getProgramTable(String name, String date);
}
