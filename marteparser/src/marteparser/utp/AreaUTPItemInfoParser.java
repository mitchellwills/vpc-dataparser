package marteparser.utp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import marteparser.util.MarteDataTableParser;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import parserutil.cache.HtmlContentParser;
import parserutil.cache.LocalHttpCache;

public class AreaUTPItemInfoParser {
	public static Map<String, String> getJobItemInfo(LocalHttpCache httpCache, String jobId, String itemId, String type) throws IOException{
		HttpPost request = new HttpPost("http://marte.insula.it/area_utp_main.asp");
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        //nvps.add(new BasicNameValuePair("zoomMappa", "2234.13677951961"));
        //nvps.add(new BasicNameValuePair("centroXMappa", "2311117.10729809"));
        //nvps.add(new BasicNameValuePair("centroYMappa", "5034959.99370261"));
        //nvps.add(new BasicNameValuePair("id_area", "0"));
        
        nvps.add(new BasicNameValuePair("comando", "25"));
        nvps.add(new BasicNameValuePair("comandoPrecedente", "1000"));
        nvps.add(new BasicNameValuePair("iconaSelezionata", "5"));
        nvps.add(new BasicNameValuePair("id_commessa", jobId));
        nvps.add(new BasicNameValuePair("id", itemId));
        nvps.add(new BasicNameValuePair("idUtente", "116"));
        nvps.add(new BasicNameValuePair("TipoUtp", type));

        request.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

		return httpCache.requestProcessAndCache("area_utp_main-"+itemId.replace('/', '_'),
				request,
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
