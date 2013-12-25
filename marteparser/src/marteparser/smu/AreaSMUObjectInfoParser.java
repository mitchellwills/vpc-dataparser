package marteparser.smu;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import marteparser.util.MarteDataTableParser;

import org.apache.http.client.methods.HttpGet;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import parserutil.cache.HtmlContentParser;
import parserutil.cache.LocalHttpCache;

public class AreaSMUObjectInfoParser {
	public static Map<String, String> getObjectInfo(LocalHttpCache httpCache, String objectId, String typeId) throws IOException{
		return httpCache.requestProcessAndCache("area_smu_main-"+typeId+"-"+objectId.replace('/', '_'),
				new HttpGet("http://marte.insula.it/area_smu_main.asp?comando=10&idsmu="+objectId+"&tiposmu="+typeId),
				new HtmlContentParser<Map<String, String>>(){
					@Override
					public Map<String, String> parse(Document topInfoDocument) {
				        Map<String, String> itemInfo = new HashMap<String, String>();
						String x = topInfoDocument.select("input[name=centroXMappa]").get(0).val();
						String y = topInfoDocument.select("input[name=centroYMappa]").get(0).val();
						String zoom = topInfoDocument.select("input[name=zoomMappa]").get(0).val();
				
						itemInfo.put("X", x);
						itemInfo.put("Y", y);
						itemInfo.put("zoom", zoom);
						
						Elements dataTables = topInfoDocument.select("form[name=mappa] table");
						itemInfo.putAll(MarteDataTableParser.parseKeyValueTables(dataTables));
						return itemInfo;
					}
		});
	}
	
}
