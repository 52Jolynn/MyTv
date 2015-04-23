package com.laudandjolynn.mytv.event;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月23日 下午1:12:01
 * @copyright: www.laudandjolynn.com
 */
public class CrawlEndEvent<T> extends CrawlEvent {
	private static final long serialVersionUID = -4553907923853878662L;
	private T returnValue;

	public CrawlEndEvent(Object source, T returnValue) {
		super(source);
		this.returnValue = returnValue;
	}

	public T getReturnValue() {
		return returnValue;
	}
}
