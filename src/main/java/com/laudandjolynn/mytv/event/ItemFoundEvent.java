package com.laudandjolynn.mytv.event;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月23日 上午9:30:39
 * @copyright: www.laudandjolynn.com
 */
public class ItemFoundEvent<T> extends CrawlEvent {
	private static final long serialVersionUID = -2993678038943272964L;
	private T item = null;

	public ItemFoundEvent(Object source, T item) {
		super(source);
		this.item = item;
	}

	public T getItem() {
		return item;
	}
}
