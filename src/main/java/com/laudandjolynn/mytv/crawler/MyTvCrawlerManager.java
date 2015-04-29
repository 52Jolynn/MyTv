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

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月16日 下午3:01:20
 * @copyright: www.laudandjolynn.com
 */
public class MyTvCrawlerManager {
	private CrawlerFactory crawlerFactory = new MyTvCrawlerFactory();

	private MyTvCrawlerManager() {
	}

	/**
	 * 获取抓取器管理器实例，单例
	 * 
	 * @return
	 */
	public static MyTvCrawlerManager getInstance() {
		return CrawlerManagerSingleton.CRAWLER_MANAGER;
	}

	private final static class CrawlerManagerSingleton {
		private final static MyTvCrawlerManager CRAWLER_MANAGER = new MyTvCrawlerManager();
	}

	/**
	 * 设置抓取器生成工厂
	 * 
	 * @param crawlerFactory
	 */
	public void setCrawlerFactory(CrawlerFactory crawlerFactory) {
		this.crawlerFactory = crawlerFactory;
	}

	/**
	 * 创建新的抓取器
	 * 
	 * @return
	 */
	public Crawler newCrawler() {
		return this.crawlerFactory.createCrawler();
	}

}
