package com.laudandjolynn.mytv.model;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月27日 上午11:33:34
 * @copyright: www.laudandjolynn.com
 */
public class MyTv {
	private long id;
	private String displayName;
	private String stationName;
	private String classify;
	private String channel;
	private int sequence;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	public String getClassify() {
		return classify;
	}

	public void setClassify(String classify) {
		this.classify = classify;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((classify == null) ? 0 : classify.hashCode());
		result = prime * result
				+ ((displayName == null) ? 0 : displayName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MyTv other = (MyTv) obj;
		if (classify == null) {
			if (other.classify != null)
				return false;
		} else if (!classify.equals(other.classify))
			return false;
		if (displayName == null) {
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MyTv [id=" + id + ", displayName=" + displayName
				+ ", stationName=" + stationName + ", classify=" + classify
				+ ", channel=" + channel + ", sequence=" + sequence + "]";
	}
}
