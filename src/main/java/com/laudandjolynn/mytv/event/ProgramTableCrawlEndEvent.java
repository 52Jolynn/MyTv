package com.laudandjolynn.mytv.event;

import java.util.List;

import com.laudandjolynn.mytv.model.ProgramTable;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月23日 下午1:12:01
 * @copyright: www.laudandjolynn.com
 */
public class ProgramTableCrawlEndEvent extends
		CrawlEndEvent<List<ProgramTable>> {
	private static final long serialVersionUID = 8031335081624277839L;
	private String stationName = null;
	private String date = null;

	public ProgramTableCrawlEndEvent(Object source,
			List<ProgramTable> returnValue, String stationName, String date) {
		super(source, returnValue);
		this.stationName = stationName;
		this.date = date;
	}

	public String getStationName() {
		return stationName;
	}

	public String getDate() {
		return date;
	}

}
