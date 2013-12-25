package marteparser.report;

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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import parserutil.cache.HtmlContentParser;
import parserutil.cache.LocalHttpCache;

public class UTPReportParser {//TODO make this work
	public static Object getObjectReport(LocalHttpCache httpCache, String jobId) throws IOException{
		HttpPost request = new HttpPost("http://marte.insula.it/web_admin/area_report_main.asp");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		nvps.add(new BasicNameValuePair("id_commessa", jobId));
		nvps.add(new BasicNameValuePair("comando", "25"));
		nvps.add(new BasicNameValuePair("tipo_sintesi", "110"));

        request.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

		return httpCache.requestProcessAndCache("area_report_main-"+jobId.replace('/', '_'),
				request,
				new HtmlContentParser<Object>(){
					@Override
					public Object parse(Document document) {
						Element rootTable = document.select("form[name=mappa] table").get(0);
						Element rootTableBody = rootTable.select("tbody").get(0);
						Elements rows = rootTableBody.children();
						
						List<Object> jobsData = new ArrayList<Object>();
						//job data rows start at index 4
						for(int i = 4; i<rows.size(); i+=2){
							Element associatedJobDataRow = rows.get(i);
							Element associatedJobItemsDataRow = rows.get(i+1);

							Map<String, String> jobData = MarteDataTableParser.parseKeyValueTables(associatedJobDataRow.select("table"));

							List<Map<String, String>> associatedJobItems = new ArrayList<Map<String, String>>();
							if(!associatedJobItemsDataRow.text().equals("non ci sono UTP associati")){//if table has entries
								Element associatedJobItemsTable = associatedJobItemsDataRow.select("table").get(0);
								Elements associatedJobItemRows = associatedJobItemsTable.select("tr");
								//first row is header label
								Elements associatedJobItemColumnHeaders = associatedJobItemRows.get(1).select("td");
								String[] headers = new String[associatedJobItemColumnHeaders.size()];
								for(int columnIndex = 0; columnIndex<associatedJobItemColumnHeaders.size(); ++columnIndex)
									headers[columnIndex] = associatedJobItemColumnHeaders.get(columnIndex).text();
								
								for(int rowIndex = 2; rowIndex<associatedJobItemRows.size()-2; ++rowIndex){//last 2 rows are empty
									Elements rowData = associatedJobItemRows.get(rowIndex).select("td");
									if(rowData.size()<=1)//blank rows
										continue;
									if(rowData.size()!=headers.length){
										throw new RuntimeException("data row columns did not match header column size ("+rowData.size()+"!="+headers.length+")");
									}
									Map<String, String> associatedJobItem = new HashMap<String, String>();
									for(int columnIndex = 0; columnIndex<associatedJobItemColumnHeaders.size(); ++columnIndex)
										associatedJobItem.put(headers[columnIndex], rowData.get(columnIndex).text());
									associatedJobItems.add(associatedJobItem);
								}
							}
							//jobsData.add(new SMUJobData(jobData, associatedJobItems));
						}
						return null;
					}
		});
	}
	
}
