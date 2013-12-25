package parserutil.datatable;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

public class CSVTableReader {

	private CSVTableReader(){}
	
	public static SimpleTable read(Reader in) throws IOException{
		try(CSVReader reader = new CSVReader(in)){
			List<String[]> allRows = reader.readAll();
			String[] columnHeaders = allRows.get(0);
			List<String[]> dataRows = allRows.subList(1, allRows.size());
			
			List<Map<String, String>> data = new ArrayList<Map<String, String>>();
			for(String[] row:dataRows){
				Map<String, String> rowData = new HashMap<String, String>();
				for(int i = 0; i<columnHeaders.length && i<row.length; ++i){
					String header = columnHeaders[i];
					String cellValue = row[i];
					rowData.put(header, cellValue);
				}
				data.add(rowData);
			}
			
			return new ReadDataTable(columnHeaders, data);
		}
	}
	
	private static class ReadDataTable implements SimpleTable{
		private final String[] columnHeaders;
		private final List<Map<String, String>> entriesValues;

		public ReadDataTable(String[] columnHeaders, List<Map<String, String>>entriesValues){
			this.columnHeaders = columnHeaders;
			this.entriesValues = entriesValues;
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
}
