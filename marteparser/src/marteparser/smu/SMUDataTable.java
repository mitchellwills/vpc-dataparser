package marteparser.smu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import marteparser.report.SMUObjectReportParser.SMUJobData;
import parserutil.datatable.AbstractTable;


public class SMUDataTable extends AbstractTable<SMUObject> {
	public SMUDataTable(Collection<SMUObject> entries){
		super(entries);
	}

	@Override
	protected void processEntry(SMUObject entry, AbstractTable.EntryDataCollection entryValues) {
		entryValues.put("!!id", entry.getObjectId());
		entryValues.put("!!type", entry.getObjectTypeName());
		
		entryValues.putAll(entry.getObjectInfo());

		List<String> jobs = new ArrayList<String>();
		List<String> jobItems = new ArrayList<String>();
		for(SMUJobData job:entry.getObjectReport().getJobsData()){
			jobs.add(job.getJobData().get("id commessa"));
			for(Map<String, String> jobItemData:job.getAssociatedJobItems())
				jobItems.add(jobItemData.get("id UTP"));
		}
		entryValues.put("jobs", jobs.toString());
		entryValues.put("jobItems", jobItems.toString());
	}

}