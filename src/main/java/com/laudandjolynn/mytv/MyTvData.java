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
package com.laudandjolynn.mytv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laudandjolynn.mytv.exception.MyTvException;
import com.laudandjolynn.mytv.utils.Constant;
import com.laudandjolynn.mytv.utils.DateUtils;
import com.laudandjolynn.mytv.utils.FileUtils;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年3月30日 下午1:41:30
 * @copyright: www.laudandjolynn.com
 */
public class MyTvData {
	private final static Logger logger = LoggerFactory
			.getLogger(MyTvData.class);
	private boolean dbInited = false;
	private boolean programTableOfTodayCrawled = false;
	private Set<String> programTableCrawled = new HashSet<String>();

	private MyTvData() {
	}

	public static MyTvData getInstance() {
		return MyTvDataSingltonHolder.MY_TV_DATA;
	}

	private final static class MyTvDataSingltonHolder {
		private final static MyTvData MY_TV_DATA = new MyTvData();
	}

	/**
	 * 加载应用数据
	 */
	public void loadData() {
		logger.debug("load data from my tv data file: "
				+ Constant.MY_TV_DATA_FILE_PATH);
		File file = new File(Constant.MY_TV_DATA_FILE_PATH);
		if (!file.exists()) {
			this.dbInited = false;
			this.programTableOfTodayCrawled = false;
			return;
		}
		SAXReader reader = new SAXReader();
		try {
			Document xmlDoc = reader.read(new File(
					Constant.MY_TV_DATA_FILE_PATH));
			List<?> nodes = xmlDoc.selectNodes("//" + Constant.XML_TAG_DB);
			if (nodes != null && nodes.size() > 0) {
				this.dbInited = Boolean.valueOf(((Element) nodes.get(0))
						.getText());
			}
			nodes = xmlDoc.selectNodes("//"
					+ Constant.XML_TAG_PROGRAM_TABLE_DATE);
			int size = nodes == null ? 0 : nodes.size();
			for (int i = 0; i < size; i++) {
				Element node = (Element) nodes.get(i);
				this.programTableCrawled.add(node.getText());
				if (node.getText().equals(DateUtils.today())) {
					this.programTableOfTodayCrawled = true;
				}
			}
		} catch (DocumentException e) {
			logger.debug("can't parse xml file.  -- "
					+ Constant.MY_TV_DATA_FILE_PATH);
			this.dbInited = false;
			this.programTableOfTodayCrawled = false;
			file.deleteOnExit();
		}
	}

	public void writeData(String parent, String tag, String value) {
		logger.debug("write data to my tv data file: "
				+ Constant.MY_TV_DATA_FILE_PATH);
		File file = new File(Constant.MY_TV_DATA_FILE_PATH);
		if (!file.exists()) {
			Document doc = DocumentHelper.createDocument();
			doc.addElement(Constant.APP_NAME);
			try {
				FileUtils.writeWithNIO(doc.asXML().getBytes(),
						Constant.MY_TV_DATA_FILE_PATH);
			} catch (IOException e) {
				throw new MyTvException(
						"error occur while write data to file. -- "
								+ Constant.MY_TV_DATA_FILE_PATH);
			}
		}
		SAXReader reader = new SAXReader();
		try {
			Document xmlDoc = reader.read(file);
			Element parentElement = xmlDoc.getRootElement();
			List<?> nodes = xmlDoc.selectNodes("//" + parent);
			if (nodes != null && nodes.size() > 0) {
				parentElement = (Element) nodes.get(0);
			}
			parentElement.addElement(tag).setText(value);
			try {
				XMLWriter writer = new XMLWriter(new FileWriter(file));
				writer.write(xmlDoc);
				writer.close();
			} catch (IOException e) {
				throw new MyTvException(
						"error occur while write data to file. -- "
								+ Constant.MY_TV_DATA_FILE_PATH);
			}
		} catch (DocumentException e) {
			String msg = "can't parse xml file. -- "
					+ Constant.MY_TV_DATA_FILE_PATH;
			throw new MyTvException(msg);
		}
	}

	/**
	 * 判断db是否已经初始化
	 * 
	 * @return
	 */
	public boolean isDbInited() {
		return this.dbInited;

	}

	/**
	 * 判断今天的节目表是否已经抓取过
	 * 
	 * @return
	 */
	public boolean isProgramTableOfTodayCrawled() {
		return this.programTableOfTodayCrawled;
	}

	/**
	 * 指定日期的节目表是否已经抓取过
	 * 
	 * @param date
	 * @return
	 */
	public boolean isProgramTableCrawled(String date) {
		return this.programTableCrawled.contains(date);
	}
}
