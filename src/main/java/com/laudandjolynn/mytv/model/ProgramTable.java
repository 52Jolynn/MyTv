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
package com.laudandjolynn.mytv.model;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月25日 下午4:41:06
 * @copyright: www.laudandjolynn.com
 */
public class ProgramTable {
	private long id;
	private String stationName;
	private String program;
	private String airDate;
	private String airTime;
	private int week;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	public String getProgram() {
		return program;
	}

	public void setProgram(String program) {
		this.program = program;
	}

	public String getAirDate() {
		return airDate;
	}

	public void setAirDate(String airDate) {
		this.airDate = airDate;
	}

	public String getAirTime() {
		return airTime;
	}

	public void setAirTime(String airTime) {
		this.airTime = airTime;
	}

	/**
	 * 1-7分别代表星期一，星期二，星期三，星期四，星期五，星期六，星期日
	 * 
	 * @return
	 */
	public int getWeek() {
		return week;
	}

	/**
	 * 1-7分别代表星期一，星期二，星期三，星期四，星期五，星期六，星期日
	 * 
	 * @param week
	 */
	public void setWeek(int week) {
		this.week = week;
	}

	@Override
	public String toString() {
		return "ProgramTable [id=" + id + ", stationName=" + stationName
				+ ", program=" + program + ", airDate=" + airDate
				+ ", airTime=" + airTime + ", week=" + week + "]";
	}

}
