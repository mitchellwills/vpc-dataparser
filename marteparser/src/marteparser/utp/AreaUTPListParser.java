package marteparser.utp;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import parserutil.cache.LocalHttpCache;


public class AreaUTPListParser {
	public static Map<String, String> getList(LocalHttpCache httpCache) throws IOException{
		Map<String, String> jobs = new TreeMap<String, String>();
		Document topDocument = Jsoup.parse(httpCache.get("http://marte.insula.it/area_utp_top.asp"));
		Element itemList = topDocument.getElementsByAttributeValue("name", "listaCommesse").get(0);
		Elements options = itemList.select("option");
		for(Element option:options){
			String id = option.attr("value");
			if(id.equals("0"))
				continue;
			String name = option.text().replaceFirst(id+" - ", "");
			jobs.put(id, name);
		}
		return jobs;
	}
}
