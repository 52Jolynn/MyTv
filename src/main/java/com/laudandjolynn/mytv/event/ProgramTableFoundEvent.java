package com.laudandjolynn.mytv.event;

import com.laudandjolynn.mytv.model.ProgramTable;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月23日 上午9:30:39
 * @copyright: www.laudandjolynn.com
 */
public class ProgramTableFoundEvent extends ItemFoundEvent<ProgramTable> {
	private static final long serialVersionUID = 734293143060487624L;

	public ProgramTableFoundEvent(Object source, ProgramTable item) {
		super(source, item);
	}

}
