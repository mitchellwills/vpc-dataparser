package ramsesparser.infopoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import parserutil.datatable.SimpleTable;
import parserutil.datatable.KeyValuePair;

public class InfoPointDataTable implements SimpleTable {
	private final String[] columnHeaders;
	private final List<Map<String, String>> entriesValues;
	public InfoPointDataTable(Collection<Entry> entries){
		Set<String> columnHeaderSet = new HashSet<String>();
		entriesValues = new ArrayList<Map<String, String>>();
		
		for(Entry entry:entries){
			Map<String, String> entryValues = new HashMap<String, String>();
			entryValues.put("!!IDName", entry.getTitle().getKey());
			columnHeaderSet.add("!!IDName");
			entryValues.put("!!ID", entry.getTitle().getValue());
			columnHeaderSet.add("!!ID");
			entryValues.put("!ImageUrl", entry.getImageUrl());
			columnHeaderSet.add("!ImageUrl");
			for(Map.Entry<String, List<KeyValuePair>> valueSet:entry.valueSetMap().entrySet()){
				String valueSetTitle = valueSet.getKey();
				for(KeyValuePair valueEntry:valueSet.getValue()){
					String key = valueEntry.getKey();
					String fullKey = valueSetTitle+"_"+key;
					String value = valueEntry.getValue();
					columnHeaderSet.add(fullKey);
					entryValues.put(fullKey, value);
				}
			}
			entriesValues.add(entryValues);
		}
		
		columnHeaders = new String[columnHeaderSet.size()];
		columnHeaderSet.toArray(columnHeaders);
		Arrays.sort(columnHeaders);
	}

	@Override
	public String[] getColumnHeaders() {
		return columnHeaders;
	}

	@Override
	public List<Map<String, String>> getEntriesValues() {
		return entriesValues;
	}

}
