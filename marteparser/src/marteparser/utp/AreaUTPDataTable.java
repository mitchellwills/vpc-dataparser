package marteparser.utp;

import java.util.Collection;
import java.util.Map;

import parserutil.datatable.AbstractTable;


public class AreaUTPDataTable extends AbstractTable<AreaUTPJobItem> {
	public AreaUTPDataTable(Collection<AreaUTPJobItem> entries){
		super(entries);
	}

	@Override
	protected void processEntry(AreaUTPJobItem entry, AbstractTable.EntryDataCollection entryValues) {
		entryValues.put("!!Job Item Id", entry.getJobItemId());
		entryValues.put("!!Job Item Type", entry.getJobItemType());
		entryValues.put("!Job Id", entry.getJobId());
		entryValues.put("!Job Name", entry.getJobName());

		entryValues.put("job_SMUs", entry.getJobSMUs().toString());
		entryValues.put("SMUs", entry.getJobItemSMUs().toString());

		for(Map.Entry<String, String> valueEntry:entry.getJobInfo().entrySet()){
			String key = "job_"+valueEntry.getKey();
			String value = valueEntry.getValue();
			entryValues.put(key, value);
		}
		entryValues.putAll(entry.getJobItemInfo());
	}

}