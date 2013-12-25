package parserutil.datatable;

import java.util.List;
import java.util.Map;

public interface SimpleTable {
	String[] getColumnHeaders();
	List<Map<String, String>> getEntriesValues();
}
