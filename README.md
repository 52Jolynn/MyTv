# MyTv
这是一个抓取国内各大电视台节目表的服务端。同时使用Hessian提供REST API，供第三方应用调用，如android、iOS等。

# 代理配置(config.properties)
以,分隔，填入代理ip:port即可。

# API列表
1. 获取电视台分类
```java
String url = "http://localhost/epg";
HessianProxyFactory proxy = new HessianProxyFactory();
try {
    JolynnTv tv = (JolynnTv) proxy.create(JolynnTv.class, url);
    String classify = tv.getTvStationClassify();
    JSONArray array = new JSONArray(classify);
} catch (MalformedURLException e) {
    e.printStackTrace();
}
```

返回结果
```
["CETV","卫视","原创","城市","央视","数字"]
```

2. 获取所有电视台
```java
String url = "http://localhost/epg";
HessianProxyFactory proxy = new HessianProxyFactory();
try {
    JolynnTv tv = (JolynnTv) proxy.create(JolynnTv.class, url);
    String stations = tv.getAllTvStation();
    JSONArray array = new JSONArray(stations);
} catch (MalformedURLException e) {
    e.printStackTrace();
}
```

返回结果
```
[{"id":1,"classify":"央视","name":"CCTV-1 综合"},{"id":2,"classify":"央视","name":"CCTV-2 财经"},{"id":3,"classify":"央视","name":"CCTV-3 综艺"},{"id":4,"classify":"央视","name":"CCTV-4 (亚洲)"},{"id":5,"classify":"央视","name":"CCTV-4 (欧洲)"},{"id":6,"classify":"央视","name":"CCTV-4 (美洲)"},{"id":7,"classify":"央视","name":"CCTV-5 体育"},{"id":8,"classify":"央视","name":"CCTV-6 电影"},{"id":9,"classify":"央视","name":"CCTV-7 军事农业"},{"id":10,"classify":"央视","name":"CCTV-8 电视剧"},{"id":11,"classify":"央视","name":"CCTV-9 纪录"},{"id":12,"classify":"央视","name":"CCTV-9 纪录(英)"},{"id":13,"classify":"央视","name":"CCTV-10 科教"},{"id":14,"classify":"央视","name":"CCTV-11 戏曲"},{"id":15,"classify":"央视","name":"CCTV-12 社会与法"},{"id":16,"classify":"央视","name":"CCTV-13 新闻"},{"id":17,"classify":"央视","name":"CCTV-14 少儿"},{"id":18,"classify":"央视","name":"CCTV-15 音乐"},{"id":19,"classify":"央视","name":"CCTV-NEWS"},{"id":20,"classify":"央视","name":"CCTV-Français"},{"id":21,"classify":"央视","name":"CCTV-Español"},{"id":22,"classify":"央视","name":"CCTV-العربية"},{"id":23,"classify":"央视","name":"CCTV-Русский"},{"id":24,"classify":"央视","name":"CCTV体育赛事"},{"id":25,"classify":"卫视","name":"安徽卫视"},{"id":26,"classify":"卫视","name":"北京卫视"},{"id":27,"classify":"卫视","name":"兵团卫视"},{"id":28,"classify":"卫视","name":"重庆卫视"},{"id":29,"classify":"卫视","name":"东方卫视"},{"id":30,"classify":"卫视","name":"东南卫视"},{"id":31,"classify":"卫视","name":"广东卫视"},{"id":32,"classify":"卫视","name":"广西卫视"},{"id":33,"classify":"卫视","name":"甘肃卫视"},{"id":34,"classify":"卫视","name":"贵州卫视"},{"id":35,"classify":"卫视","name":"河北卫视"},{"id":36,"classify":"卫视","name":"河南卫视"},{"id":37,"classify":"卫视","name":"黑龙江卫视"},{"id":38,"classify":"卫视","name":"湖北卫视"},{"id":39,"classify":"卫视","name":"湖南卫视"},{"id":40,"classify":"卫视","name":"吉林卫视"},{"id":41,"classify":"卫视","name":"江苏卫视"},{"id":42,"classify":"卫视","name":"江西卫视"},{"id":43,"classify":"卫视","name":"辽宁卫视"},{"id":44,"classify":"卫视","name":"旅游卫视"},{"id":45,"classify":"卫视","name":"内蒙古卫视"},{"id":46,"classify":"卫视","name":"宁夏卫视"},{"id":47,"classify":"卫视","name":"青海卫视"},{"id":48,"classify":"卫视","name":"山东卫视"},{"id":49,"classify":"卫视","name":"山东教育台"},{"id":50,"classify":"卫视","name":"深圳卫视"},{"id":51,"classify":"卫视","name":"陕西卫视"},{"id":52,"classify":"卫视","name":"山西卫视"},{"id":53,"classify":"卫视","name":"四川卫视"},{"id":54,"classify":"卫视","name":"天津卫视"},{"id":55,"classify":"卫视","name":"西藏卫视"},{"id":56,"classify":"卫视","name":"厦门卫视"},{"id":57,"classify":"卫视","name":"新疆卫视"},{"id":58,"classify":"卫视","name":"香港卫视"},{"id":59,"classify":"卫视","name":"延边卫视"},{"id":60,"classify":"卫视","name":"云南卫视"},{"id":61,"classify":"卫视","name":"浙江卫视"},{"id":62,"classify":"数字","name":"CCTV 电影"},{"id":63,"classify":"数字","name":"CCTV世界地理"},{"id":64,"classify":"数字","name":"CCTV中学生"},{"id":65,"classify":"数字","name":"CCTV中视购物"},{"id":66,"classify":"数字","name":"CCTV发现之旅"},{"id":67,"classify":"数字","name":"CCTV国防军事"},{"id":68,"classify":"数字","name":"CCTV央视台球"},{"id":69,"classify":"数字","name":"CCTV央视文化精品"},{"id":70,"classify":"数字","name":"CCTV女性时尚"},{"id":71,"classify":"数字","name":"CCTV娱乐"},{"id":72,"classify":"数字","name":"CCTV怀旧剧场"},{"id":73,"classify":"数字","name":"CCTV戏曲"},{"id":74,"classify":"数字","name":"CCTV新科动漫"},{"id":75,"classify":"数字","name":"CCTV气象"},{"id":76,"classify":"数字","name":"CCTV电视指南"},{"id":77,"classify":"数字","name":"CCTV第一剧场"},{"id":78,"classify":"数字","name":"CCTV老故事"},{"id":79,"classify":"数字","name":"CCTV风云剧场"},{"id":80,"classify":"数字","name":"CCTV风云足球"},{"id":81,"classify":"数字","name":"CCTV风云音乐"},{"id":82,"classify":"数字","name":"CCTV高尔夫网球"},{"id":83,"classify":"数字","name":"DV生活"},{"id":84,"classify":"数字","name":"中国3D电视试验"},{"id":85,"classify":"数字","name":"书画"},{"id":86,"classify":"数字","name":"卫生健康"},{"id":87,"classify":"数字","name":"国学"},{"id":88,"classify":"数字","name":"天元围棋"},{"id":89,"classify":"数字","name":"宝贝家"},{"id":90,"classify":"数字","name":"彩民在线"},{"id":91,"classify":"数字","name":"快乐垂钓"},{"id":92,"classify":"数字","name":"摄影"},{"id":93,"classify":"数字","name":"文物宝库"},{"id":94,"classify":"数字","name":"早期教育"},{"id":95,"classify":"数字","name":"梨园"},{"id":96,"classify":"数字","name":"武术世界"},{"id":97,"classify":"数字","name":"汽摩"},{"id":98,"classify":"数字","name":"游戏竞技"},{"id":99,"classify":"数字","name":"环球奇观"},{"id":100,"classify":"数字","name":"现代女性"},{"id":101,"classify":"数字","name":"留学世界"},{"id":102,"classify":"数字","name":"英语辅导"},{"id":103,"classify":"数字","name":"茶频道"},{"id":104,"classify":"数字","name":"证券资讯"},{"id":105,"classify":"数字","name":"靓妆"},{"id":106,"classify":"数字","name":"高尔夫"},{"id":107,"classify":"CETV","name":"CETV-1"},{"id":108,"classify":"CETV","name":"CETV-2"},{"id":109,"classify":"CETV","name":"CETV-3"},{"id":110,"classify":"原创","name":"熊猫"},{"id":111,"classify":"城市","name":"BTV文艺","city":"北京"},{"id":112,"classify":"城市","name":"BTV科教","city":"北京"},{"id":113,"classify":"城市","name":"BTV影视","city":"北京"},{"id":114,"classify":"城市","name":"BTV财经","city":"北京"},{"id":115,"classify":"城市","name":"BTV体育","city":"北京"},{"id":116,"classify":"城市","name":"BTV生活","city":"北京"},{"id":117,"classify":"城市","name":"BTV青少","city":"北京"},{"id":118,"classify":"城市","name":"BTV新闻","city":"北京"},{"id":119,"classify":"城市","name":"BTV卡酷少儿","city":"北京"},{"id":120,"classify":"城市","name":"BTV纪实","city":"北京"},{"id":121,"classify":"城市","name":"BTV国际","city":"北京"},{"id":122,"classify":"城市","name":"天津1套","city":"天津"},{"id":123,"classify":"城市","name":"天津2套","city":"天津"},{"id":124,"classify":"城市","name":"滨海新闻综合","city":"天津"},{"id":125,"classify":"城市","name":"滨海都市生活","city":"天津"},{"id":126,"classify":"城市","name":"广西综艺","city":"广西"},{"id":127,"classify":"城市","name":"珠海一套","city":"广东"},{"id":128,"classify":"城市","name":"珠海二套","city":"广东"},{"id":129,"classify":"城市","name":"成都新闻综合","city":"四川"},{"id":130,"classify":"城市","name":"成都经济资讯服务","city":"四川"},{"id":131,"classify":"城市","name":"成都公共","city":"四川"},{"id":132,"classify":"城市","name":"辽宁都市","city":"辽宁"},{"id":133,"classify":"城市","name":"邢台综合","city":"河北"},{"id":134,"classify":"城市","name":"邢台生活","city":"河北"},{"id":135,"classify":"城市","name":"邢台公共","city":"河北"},{"id":136,"classify":"城市","name":"邢台沙河","city":"河北"},{"id":137,"classify":"城市","name":"宁波一套","city":"浙江"},{"id":138,"classify":"城市","name":"宁波二套","city":"浙江"},{"id":139,"classify":"城市","name":"宁波三套","city":"浙江"},{"id":140,"classify":"城市","name":"宁波四套","city":"浙江"},{"id":141,"classify":"城市","name":"宁波五套","city":"浙江"},{"id":142,"classify":"城市","name":"厦门一套","city":"福建"},{"id":143,"classify":"城市","name":"厦门二套","city":"福建"},{"id":144,"classify":"城市","name":"厦门三套","city":"福建"},{"id":145,"classify":"城市","name":"厦门四套","city":"福建"}]
```
3. 获取指定日期、电视台的节目单
```java
String url = "http://localhost/epg";
HessianProxyFactory proxy = new HessianProxyFactory();
try {
    JolynnTv tv = (JolynnTv) proxy.create(JolynnTv.class, url);
    String program = tv.getProgramTable("CCTV-1 综合", DateUtils.today());
    JSONArray array = new JSONArray(program);
} catch (MalformedURLException e) {
    e.printStackTrace();
}
```

返回结果
```
[{"id":4698,"airDate":"2015-04-01","station":1,"program":"蓝鸽乐园-单词记忆王","airTime":"2015-04-01 06:00:00","stationName":"CCTV-1 综合","week":3},{"id":4699,"airDate":"2015-04-01","station":1,"program":"小学初级-丰收的季节","airTime":"2015-04-01 06:17:00","stationName":"CCTV-1 综合","week":3},{"id":4700,"airDate":"2015-04-01","station":1,"program":"蓝鸽故事会-海视博纳绘本故事","airTime":"2015-04-01 06:31:00","stationName":"CCTV-1 综合","week":3},{"id":4701,"airDate":"2015-04-01","station":1,"program":"蓝鸽帮帮忙-蓝鸽帮帮忙","airTime":"2015-04-01 06:47:00","stationName":"CCTV-1 综合","week":3},{"id":4702,"airDate":"2015-04-01","station":1,"program":"蓝鸽E课堂-水木学堂","airTime":"2015-04-01 06:59:00","stationName":"CCTV-1 综合","week":3},{"id":4703,"airDate":"2015-04-01","station":1,"program":"蓝鸽动画城-迪比狗","airTime":"2015-04-01 07:32:00","stationName":"CCTV-1 综合","week":3},{"id":4704,"airDate":"2015-04-01","station":1,"program":"英语速成-365天英语口语","airTime":"2015-04-01 08:00:00","stationName":"CCTV-1 综合","week":3},{"id":4705,"airDate":"2015-04-01","station":1,"program":"交际英语-跟老外学口语","airTime":"2015-04-01 08:49:00","stationName":"CCTV-1 综合","week":3},{"id":4706,"airDate":"2015-04-01","station":1,"program":"成人-文化走廊","airTime":"2015-04-01 09:38:00","stationName":"CCTV-1 综合","week":3},{"id":4707,"airDate":"2015-04-01","station":1,"program":"蓝鸽乐园-单词记忆王","airTime":"2015-04-01 10:01:00","stationName":"CCTV-1 综合","week":3},{"id":4708,"airDate":"2015-04-01","station":1,"program":"小学初级-Webby种蔬菜","airTime":"2015-04-01 10:17:00","stationName":"CCTV-1 综合","week":3},{"id":4709,"airDate":"2015-04-01","station":1,"program":"蓝鸽故事会-海视博纳绘本故事","airTime":"2015-04-01 10:32:00","stationName":"CCTV-1 综合","week":3},{"id":4710,"airDate":"2015-04-01","station":1,"program":"蓝鸽帮帮忙-蓝鸽帮帮忙","airTime":"2015-04-01 10:49:00","stationName":"CCTV-1 综合","week":3},{"id":4711,"airDate":"2015-04-01","station":1,"program":"蓝鸽E课堂-水木学堂","airTime":"2015-04-01 11:01:00","stationName":"CCTV-1 综合","week":3},{"id":4712,"airDate":"2015-04-01","station":1,"program":"蓝鸽动画城-迪比狗","airTime":"2015-04-01 11:30:00","stationName":"CCTV-1 综合","week":3},{"id":4713,"airDate":"2015-04-01","station":1,"program":"英语速成-365天英语口语","airTime":"2015-04-01 12:00:00","stationName":"CCTV-1 综合","week":3},{"id":4714,"airDate":"2015-04-01","station":1,"program":"交际英语-跟老外学口语","airTime":"2015-04-01 12:51:00","stationName":"CCTV-1 综合","week":3},{"id":4715,"airDate":"2015-04-01","station":1,"program":"成人-文化走廊","airTime":"2015-04-01 13:41:00","stationName":"CCTV-1 综合","week":3},{"id":4716,"airDate":"2015-04-01","station":1,"program":"蓝鸽E课堂-水木学堂","airTime":"2015-04-01 14:00:00","stationName":"CCTV-1 综合","week":3},{"id":4717,"airDate":"2015-04-01","station":1,"program":"蓝鸽动画城-迪比狗","airTime":"2015-04-01 14:33:00","stationName":"CCTV-1 综合","week":3},{"id":4718,"airDate":"2015-04-01","station":1,"program":"英语速成-365天英语口语","airTime":"2015-04-01 15:01:00","stationName":"CCTV-1 综合","week":3},{"id":4719,"airDate":"2015-04-01","station":1,"program":"交际英语-跟老外学口语","airTime":"2015-04-01 15:27:00","stationName":"CCTV-1 综合","week":3},{"id":4720,"airDate":"2015-04-01","station":1,"program":"成人-文化走廊","airTime":"2015-04-01 15:49:00","stationName":"CCTV-1 综合","week":3},{"id":4721,"airDate":"2015-04-01","station":1,"program":"蓝鸽乐园-单词记忆王","airTime":"2015-04-01 16:00:00","stationName":"CCTV-1 综合","week":3},{"id":4722,"airDate":"2015-04-01","station":1,"program":"小学初级-Webby种蔬菜","airTime":"2015-04-01 16:16:00","stationName":"CCTV-1 综合","week":3},{"id":4723,"airDate":"2015-04-01","station":1,"program":"蓝鸽故事会-海视博纳绘本故事","airTime":"2015-04-01 16:31:00","stationName":"CCTV-1 综合","week":3},{"id":4724,"airDate":"2015-04-01","station":1,"program":"蓝鸽帮帮忙-蓝鸽帮帮忙","airTime":"2015-04-01 16:48:00","stationName":"CCTV-1 综合","week":3},{"id":4725,"airDate":"2015-04-01","station":1,"program":"蓝鸽E课堂-水木学堂","airTime":"2015-04-01 17:00:00","stationName":"CCTV-1 综合","week":3},{"id":4726,"airDate":"2015-04-01","station":1,"program":"蓝鸽动画城-迪比狗","airTime":"2015-04-01 17:30:00","stationName":"CCTV-1 综合","week":3},{"id":4727,"airDate":"2015-04-01","station":1,"program":"蓝鸽乐园-单词记忆王","airTime":"2015-04-01 18:00:00","stationName":"CCTV-1 综合","week":3},{"id":4728,"airDate":"2015-04-01","station":1,"program":"小学初级-丰收的季节","airTime":"2015-04-01 18:16:00","stationName":"CCTV-1 综合","week":3},{"id":4729,"airDate":"2015-04-01","station":1,"program":"蓝鸽故事会-海视博纳绘本故事","airTime":"2015-04-01 18:30:00","stationName":"CCTV-1 综合","week":3},{"id":4730,"airDate":"2015-04-01","station":1,"program":"蓝鸽帮帮忙-蓝鸽帮帮忙","airTime":"2015-04-01 18:46:00","stationName":"CCTV-1 综合","week":3},{"id":4731,"airDate":"2015-04-01","station":1,"program":"蓝鸽E课堂-水木学堂","airTime":"2015-04-01 18:57:00","stationName":"CCTV-1 综合","week":3},{"id":4732,"airDate":"2015-04-01","station":1,"program":"蓝鸽动画城-迪比狗","airTime":"2015-04-01 19:31:00","stationName":"CCTV-1 综合","week":3},{"id":4733,"airDate":"2015-04-01","station":1,"program":"英语速成-365天英语口语","airTime":"2015-04-01 20:00:00","stationName":"CCTV-1 综合","week":3},{"id":4734,"airDate":"2015-04-01","station":1,"program":"交际英语-跟老外学口语","airTime":"2015-04-01 20:50:00","stationName":"CCTV-1 综合","week":3},{"id":4735,"airDate":"2015-04-01","station":1,"program":"成人-文化走廊","airTime":"2015-04-01 21:40:00","stationName":"CCTV-1 综合","week":3},{"id":4736,"airDate":"2015-04-01","station":1,"program":"蓝鸽帮帮忙-蓝鸽帮帮忙","airTime":"2015-04-01 22:00:00","stationName":"CCTV-1 综合","week":3},{"id":4737,"airDate":"2015-04-01","station":1,"program":"看电影学英语-茶花女","airTime":"2015-04-01 22:12:00","stationName":"CCTV-1 综合","week":3}]
```

# 主要开源软件
1. htmlunit-2.15
2. jsoup-1.8.1
3. hessian-4.07
4. fastjson-1.2.5
5. jetty

# 扩展接口
```java
package com.laudandjolynn.mytv.crawler;

import java.util.List;

import com.laudandjolynn.mytv.model.ProgramTable;
import com.laudandjolynn.mytv.model.TvStation;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月15日 下午3:36:28
 * @copyright: www.laudandjolynn.com
 */
public interface Crawler {
	/**
	 * 抓取所有电视台
	 * 
	 * @return
	 */
	public List<TvStation> crawlAllTvStation();

	/**
	 * 根据电视台名称、日期抓取电视节目表
	 * 
	 * @param date
	 * @param station
	 * @return
	 */
	public List<ProgramTable> crawlProgramTable(String date, TvStation station);

	/**
	 * 判断指定电视台是否可抓取
	 * 
	 * @param station
	 * @return
	 */
	public boolean exists(TvStation station);

	/**
	 * 获取抓取器名称
	 * 
	 * @return
	 */
	public String getCrawlerName();

	/**
	 * 获取抓取地址
	 * 
	 * @return
	 */
	public String getUrl();

}
```
支持自定义抓取器工厂，实现CrawlerFactory接口，并设置到MyTvCrawlerManager即可。
```java
package com.laudandjolynn.mytv.crawler;

import com.laudandjolynn.mytv.crawler.epg.EpgCrawlerFactory;
import com.laudandjolynn.mytv.crawler.tvmao.TvMaoCrawlerFactory;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月16日 下午2:57:53
 * @copyright: www.laudandjolynn.com
 */
public class MyTvCrawlerFactory implements CrawlerFactory {
	@Override
	public Crawler createCrawler() {
		CrawlerGroup cralwerGroup = new CrawlerGroup();
		cralwerGroup.addCrawler(new EpgCrawlerFactory().createCrawler());
		cralwerGroup.addCrawler(new TvMaoCrawlerFactory().createCrawler());
		return cralwerGroup;
	}

}
```

```java
CrawlerFactory factory = new MyTvCrawlerFactory();
MyTvCrawlerManager.getInstance().setCrawlerFactory(factory);
```
