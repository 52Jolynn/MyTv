# MyTv
这是一个抓取国内各大电视台节目表的服务端。同时使用Hessian提供REST API，供第三方应用调用，如android、iOS等。

# API列表
1. 获取电视台分类
```java
String url = "http://localhost/epg";
HessianProxyFactory proxy = new HessianProxyFactory();
try {
JolynnTv tv = (JolynnTv) proxy.create(JolynnTv.class, url);
String classify = tv.getTvStationClassify();
JSONArray array = new JSONArray(classify);
assertTrue(array.length() == 6);
} catch (MalformedURLException e) {
}
```
2. 获取所有电视台
3. 获取指定日期、电视台的节目单
