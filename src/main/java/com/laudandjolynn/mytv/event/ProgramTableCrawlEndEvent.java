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
