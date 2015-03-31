package com.laudandjolynn.mytv.model;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月27日 下午1:17:02
 * @copyright: www.laudandjolynn.com
 */
public class EpgTask {
	private String date = null;
	private String stationName = null;

	public EpgTask(String stationName, String date) {
		this.stationName = stationName;
		this.date = date;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result
				+ ((stationName == null) ? 0 : stationName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EpgTask other = (EpgTask) obj;
		if (date == null) {
			if (other.date != null) {
				return false;
			}
		} else if (!date.equals(other.date))
			return false;
		if (stationName == null) {
			if (other.stationName != null) {
				return false;
			}
		} else if (!stationName.equals(other.stationName)) {
			return false;
		}
		return true;
	}

}
