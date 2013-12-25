package parserutil.datatable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractTable<T> implements SimpleTable{
	private final String[] columnHeaders;
	private final List<Map<String, String>> entriesValues;
	public interface EntryDataCollection{
		void put(String key, String value);
		void putAll(Map<String, String> data);
	}
	public AbstractTable(Collection<T> entries){
		final List<String> columnHeaderList = new ArrayList<String>();
		final Set<String> columnHeaderSet = new HashSet<String>();
		entriesValues = new ArrayList<Map<String, String>>();
		for(T entry:entries){
			final Map<String, String> entryValues = new HashMap<String, String>();
			EntryDataCollection dataCollection = new EntryDataCollection() {
				@Override
				public void put(String key, String value) {
					if(key==null)
						System.out.println("Got null key = "+value);
					entryValues.put(key, value);
					if(columnHeaderSet.add(key))
						columnHeaderList.add(key);
				}

				@Override
				public void putAll(Map<String, String> data) {
					for(Map.Entry<String, String> entry:data.entrySet())
						put(entry.getKey(), entry.getValue());
				}
			};
			processEntry(entry, dataCollection);
			entriesValues.add(entryValues);
		}
		
		columnHeaders = new String[columnHeaderList.size()];
		columnHeaderList.toArray(columnHeaders);
	}
	
	protected abstract void processEntry(T entry, EntryDataCollection entryValues);

	@Override
	public String[] getColumnHeaders() {
		return columnHeaders;
	}

	@Override
	public List<Map<String, String>> getEntriesValues() {
		return entriesValues;
	}
}
