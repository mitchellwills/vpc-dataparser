package marteparser.utp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


public class AreaUTPInfoParser {
	
	public static Map<String, String> getJobInfo(LocalHttpCache httpCache, String jobId) throws IOException{
		HttpPost request = new HttpPost("http://marte.insula.it/area_utp_top.asp");
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair("idCommessa", jobId));
		nvps.add(new BasicNameValuePair("idUtente", "116"));
		nvps.add(new BasicNameValuePair("aggiorna", "1"));
		request.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

		return httpCache.requestProcessAndCache("area_utp_top-"+jobId.replace('/', '_'), request,
				new HtmlContentParser<Map<String, String>>() {
					@Override
					public Map<String, String> parse(Document topInfoDocument) {
						Map<String, String> jobInfo = new HashMap<String, String>();
						Element dataTable = topInfoDocument.select("form[name=Commessa] table tbody").get(0);
						Elements rows = dataTable.select("tr");
						for (Element row : rows) {
							Elements columns = row.select("td");
							for (int i = 0; i < columns.size() - 1; i += 2) {
								Element keyElement = columns.get(i);
								String key = keyElement.text().replace('\u00a0', ' ').trim();
								if (key.equals("nome intervento"))// skip the selector
									continue;
								if (key.isEmpty())
									continue;
								Element valueElement = columns.get(i + 1);
								String value = valueElement.text().replace('\u00a0', ' ').trim();
								jobInfo.put(key, value);
							}
						}
						return jobInfo;
					}
				});
	}
	public static Map<String, List<String>> getJobItemList(LocalHttpCache httpCache, String jobId) throws IOException{
		HttpPost request = new HttpPost("http://marte.insula.it/area_utp_sx.asp?id_commessa="+jobId);
		
		return httpCache.requestProcessAndCache(
				"area_utp_sx-" + jobId.replace('/', '_'), request,
				new HtmlContentParser<Map<String, List<String>>>() {
					@Override
					public Map<String, List<String>> parse(
							Document topInfoDocument) {
						Map<String, List<String>> jobItems = new HashMap<String, List<String>>();
						Element dataTable = topInfoDocument.select("form[name=listautp] table table").get(0);
						Elements rows = dataTable.select("tr");
						for (int i = 0; i < rows.size() - 1; i += 2) {
							Element nameRow = rows.get(i);
							String name = nameRow.text().replace('\u00a0', ' ').trim();

							List<String> items = new ArrayList<String>();
							Element dataRow = rows.get(i + 1);
							Elements dataLists = dataRow.getElementsByTag("select");
							if (dataLists.isEmpty()) {

							} else {
								Element dataList = dataLists.get(0);
								Elements options = dataList.getElementsByTag("option");
								for (Element option : options) {
									items.add(option.attr("value"));
								}
							}
							if (items.size() > 0)
								jobItems.put(name, items);
						}
						return jobItems;
					}
				});
	}
	
	public static String getTypeId(String type){
		switch(type){
		case "sponde":
			return "4";
		case "pavimentazioni":
			return "2";
		case "ponti":
			return "3";
		case "segmenti e intersezioni":
			return "1";
		}
		throw new RuntimeException("Unknown type: "+type);
	}
}
