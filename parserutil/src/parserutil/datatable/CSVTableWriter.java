package parserutil.datatable;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVWriter;

public class CSVTableWriter {
	private CSVTableWriter(){}

	public static void write(Writer out, SimpleTable table) throws IOException{
		write(out, table.getColumnHeaders(), table.getEntriesValues());
	}
	public static void write(Writer out, String[] columnHeaders, List<Map<String, String>> entriesValues) throws IOException{
		try (CSVWriter writer = new CSVWriter(out)) {
			writer.writeNext(columnHeaders);

			String[] data = new String[columnHeaders.length];
			for (Map<String, String> entryValues : entriesValues) {
				for (int i = 0; i < columnHeaders.length; ++i) {
					String key = columnHeaders[i];
					String value = entryValues.get(key);
					data[i] = value;
				}
				writer.writeNext(data);
			}
		}
	}
}
