package com.laudandjolynn.mytv.crawler;

import java.util.List;

import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月15日 下午3:35:15
 * @copyright: www.laudandjolynn.com
 */
public interface Parser {
	/**
	 * 解析电视台
	 * 
	 * @param html
	 * @return
	 */
	public List<TvStation> parseTvStation(String html);

	/**
	 * 解析电视节目表
	 * 
	 * @param html
	 * @return
	 */
	public List<ProgramTable> parseProgramTable(String html);
}
