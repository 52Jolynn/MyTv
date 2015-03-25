package com.laudandjolynn.mytvlist.model;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 下午4:41:06
 * @copyright: www.laudandjolynn.com
 */
public class ProgramTable {
	private int id;
	private int station;
	private String program;
	private String airTime;
	private int week;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStation() {
		return station;
	}

	public void setStation(int station) {
		this.station = station;
	}

	public String getProgram() {
		return program;
	}

	public void setProgram(String program) {
		this.program = program;
	}

	public String getAirTime() {
		return airTime;
	}

	public void setAirTime(String airTime) {
		this.airTime = airTime;
	}

	public int getWeek() {
		return week;
	}

	public void setWeek(int week) {
		this.week = week;
	}

	@Override
	public String toString() {
		return "ProgramTable [id=" + id + ", station=" + station + ", program="
				+ program + ", airTime=" + airTime + ", week=" + week + "]";
	}

}
