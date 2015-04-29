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
package com.laudandjolynn.mytv.crawler;

import java.util.ArrayList;
import java.util.List;

import com.laudandjolynn.mytv.event.CrawlEventListener;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月16日 下午12:16:12
 * @copyright: www.laudandjolynn.com
 */
public abstract class AbstractCrawler implements Crawler {
	protected List<CrawlEventListener> listeners = new ArrayList<CrawlEventListener>();

	@Override
	public void registerCrawlEventListener(CrawlEventListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeCrawlEventListener(CrawlEventListener listener) {
		this.listeners.remove(listener);
	}
}
