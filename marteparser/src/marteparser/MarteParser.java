package marteparser;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import marteparser.report.SMUObjectReportParser;
import marteparser.report.SMUObjectReportParser.SMUJobData;
import marteparser.report.SMUObjectReportParser.SMUReport;
import marteparser.smu.AreaSMUListParser;
import marteparser.smu.AreaSMUObjectInfoParser;
import marteparser.smu.SMUDataTable;
import marteparser.smu.SMUObject;
import marteparser.utp.AreaUTPDataTable;
import marteparser.utp.AreaUTPInfoParser;
import marteparser.utp.AreaUTPItemInfoParser;
import marteparser.utp.AreaUTPJobItem;
import marteparser.utp.AreaUTPListParser;
import parserutil.cache.LocalHttpCache;
import parserutil.datatable.CSVTableWriter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class MarteParser {

	public static void main(String[] args) throws IOException {
		LocalHttpCache httpCache = new LocalHttpCache(333);
		try{
			if(!LoginUtil.isLoggedIn(httpCache)){
				System.out.println("Logging in...");
				LoginUtil.login(httpCache, "", "");//TODO Fill in with username & password
				httpCache.saveCookies();
			}
		} catch(Exception e){
			System.err.println("Error logging in: "+e.getMessage());
			System.out.println("Running local");
			httpCache.cacheOnly();
		}
		
		
		
		System.out.println("retrieving SMUs");
		Map<String, List<String>> objectLists = AreaSMUListParser.getLists(httpCache);
		List<SMUObject> smuObjects = new ArrayList<SMUObject>();

		for(Map.Entry<String, List<String>> objectListEntry:objectLists.entrySet()){
			String objectTypeName = objectListEntry.getKey();
			String objectTypeId = SMUObject.getTypeId(objectTypeName);
			List<String> objectList = objectListEntry.getValue();
			for(String objectId:objectList){
				Map<String, String> objectInfo = AreaSMUObjectInfoParser.getObjectInfo(httpCache, objectId, objectTypeId);
				SMUReport objectReport = SMUObjectReportParser.getObjectReport(httpCache, objectId, objectTypeId);
				smuObjects.add(new SMUObject(objectId, objectTypeName, objectInfo, objectReport));
			}
		}

		
		
		
		System.out.println("associating SMUs with jobs");
		Multimap<String, String> jobToSMU = HashMultimap.create();
		Multimap<String, String> jobItemToSMU = HashMultimap.create();
		for(SMUObject smu:smuObjects){
			List<SMUJobData> jobsData = smu.getObjectReport().getJobsData();
			for(SMUJobData jobData:jobsData){
				jobToSMU.put(jobData.getJobData().get("id commessa"), smu.getObjectId());
				for(Map<String, String> jobItemData:jobData.getAssociatedJobItems()){
					jobItemToSMU.put(jobItemData.get("id UTP"), smu.getObjectId());
				}
			}
		}

		
		
		
		
		System.out.println("retrieving jobs");
		Set<String> jobTypes = new HashSet<String>();
		List<AreaUTPJobItem> items = new ArrayList<AreaUTPJobItem>();
		Map<String, String> jobs = AreaUTPListParser.getList(httpCache);
		
		for(String jobId:jobs.keySet()){
			Map<String, String> jobInfo = AreaUTPInfoParser.getJobInfo(httpCache, jobId);
			Map<String, List<String>> jobList = AreaUTPInfoParser.getJobItemList(httpCache, jobId);
			jobTypes.addAll(jobList.keySet());
			for(Map.Entry<String, List<String>> jobTypeEntry:jobList.entrySet()){
				String jobTypeName = jobTypeEntry.getKey();
				String jobTypeId = AreaUTPInfoParser.getTypeId(jobTypeName);
				List<String> jobItems = jobTypeEntry.getValue();
				for(String jobItemId:jobItems){
					Map<String, String> jobItemInfo = AreaUTPItemInfoParser.getJobItemInfo(httpCache, jobId, jobItemId, jobTypeId);
					items.add(new AreaUTPJobItem(jobId, jobs.get(jobId), jobItemId, jobTypeName, jobInfo, jobItemInfo, jobToSMU.get(jobId), jobItemToSMU.get(jobItemId)));
				}
			}
			if(jobList.isEmpty()){
				items.add(new AreaUTPJobItem(jobId, jobs.get(jobId), "", "", jobInfo, Collections.<String, String>emptyMap(), jobToSMU.get(jobId), Collections.<String>emptySet()));
			}
		}


		
		
		
		
		System.out.println("writing files");

		for(String type:jobTypes){
			List<AreaUTPJobItem> typeItems = new ArrayList<AreaUTPJobItem>();
			for(AreaUTPJobItem item:items){
				if(item.getJobItemType().equals(type))
					typeItems.add(item);
			}
			CSVTableWriter.write(new FileWriter("utp-"+type+".csv"), new AreaUTPDataTable(typeItems));
		}
		for(String type:objectLists.keySet()){
			List<SMUObject> typeItems = new ArrayList<SMUObject>();
			for(SMUObject item:smuObjects){
				if(item.getObjectTypeName().equals(type))
					typeItems.add(item);
			}
			CSVTableWriter.write(new FileWriter("smu-"+type+".csv"), new SMUDataTable(typeItems));
		}

		CSVTableWriter.write(new FileWriter("smu.csv"), new SMUDataTable(smuObjects));
		CSVTableWriter.write(new FileWriter("utp.csv"), new AreaUTPDataTable(items));
		System.out.println("Wrote to file");
	}

}