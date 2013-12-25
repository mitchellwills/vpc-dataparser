package marteparser.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.google.common.collect.Lists;

import parserutil.cache.HtmlContentParser;
import parserutil.cache.LocalHttpCache;

public class SMUObjectReportParser {
	public static class SMUReport{

		private final List<SMUJobData> jobsData;

		public SMUReport(List<SMUJobData> jobsData){
			this.jobsData = jobsData;
		}

		public List<SMUJobData> getJobsData() {
			return jobsData;
		}
		
	}
	public static class SMUJobData{
		private final Map<String, String> jobData;
		private final List<Map<String, String>> associatedJobItems;

		public SMUJobData(Map<String, String> jobData, List<Map<String, String>> associatedJobItems){
			this.jobData = jobData;
			this.associatedJobItems = associatedJobItems;
		}

		public Map<String, String> getJobData() {
			return jobData;
		}

		public List<Map<String, String>> getAssociatedJobItems() {
			return associatedJobItems;
		}
	}
	public static SMUReport getObjectReport(LocalHttpCache httpCache, String objectId, String typeId) throws IOException{
		HttpPost request = new HttpPost("http://marte.insula.it/web_admin/area_report_smu_main.asp");
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();

        nvps.add(new BasicNameValuePair("comando", "10"));
        nvps.add(new BasicNameValuePair("Tiposmu", typeId));
        nvps.add(new BasicNameValuePair("idsmu", objectId));

        request.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
        
        List<String> type6BadReports = Arrays.asList("29791", "30440", "30475", "30476", "30956", "30970",
        		"31087", "31162", "31264", "31688", "31698", "31699", "31730", "31832", "32538", "32541", "32564",
        		"32578", "32805", "33012", "33436", "33440", "33593", "33763");
        if((type6BadReports.contains(objectId) && typeId.equals("6")) || ("16414".equals(objectId) && typeId.equals("3"))){//FIXME
        	System.err.println("Skipped SMUReport "+objectId+" (type: "+typeId+")");
        	return new SMUReport(Lists.<SMUJobData>newArrayList());
        }

		return httpCache.requestProcessAndCache("area_report_smu_main-"+typeId+"-"+objectId.replace('/', '_'),
				request,
				new HtmlContentParser<SMUReport>(){
					@Override
					public SMUReport parse(Document document) {
						Element rootTable = document.select("form[name=mappa] table").get(0);
						Element rootTableBody = rootTable.select("tbody").get(0);
						Elements rows = rootTableBody.children();
						
						List<SMUJobData> jobsData = new ArrayList<SMUJobData>();
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
							jobsData.add(new SMUJobData(jobData, associatedJobItems));
						}
						return new SMUReport(jobsData);
					}
		});
	}
	
}
