package marteparser.smu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import parserutil.cache.HtmlContentParser;
import parserutil.cache.LocalHttpCache;


public class AreaSMUListParser {
	public static Map<String, List<String>> getLists(LocalHttpCache httpCache) throws IOException{
		HttpPost request = new HttpPost("http://marte.insula.it/area_smu_sx.asp");
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();

        nvps.add(new BasicNameValuePair("segmenti", "1"));
        nvps.add(new BasicNameValuePair("sponde", "1"));
        nvps.add(new BasicNameValuePair("intersezioni", "1"));
        nvps.add(new BasicNameValuePair("pavimentazioni", "1"));
        nvps.add(new BasicNameValuePair("ponti", "1"));
        nvps.add(new BasicNameValuePair("Edificato", "1"));

        request.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

		return httpCache.requestProcessAndCache("area_smu_sx",
				request,
				new HtmlContentParser<Map<String, List<String>>>(){
			@Override
			public Map<String, List<String>> parse(Document topDocument) {
				Map<String, List<String>> objectNameLists = new TreeMap<String, List<String>>();
				Elements objectLists = topDocument.select("select[class=menutendina]");
				for(Element objectList:objectLists){
					List<String> objectNameList = new ArrayList<String>();
					String typeName = objectList.attr("name").substring(5);
					Elements options = objectList.select("option");
					for(Element option:options){
						objectNameList.add(option.attr("value"));
					}
					objectNameLists.put(typeName, objectNameList);
				}
				return objectNameLists;
			}
		});
	}
}
