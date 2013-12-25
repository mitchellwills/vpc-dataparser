package datamerger.data.writer;

import java.io.*;

import au.com.bytecode.opencsv.*;
import datamerger.data.writer.DatabaseTableTableWriter.*;

public class CSVTableWriter implements TableWriter, Closeable{
	private final CSVWriter writer;

	public CSVTableWriter(CSVWriter writer){
		this.writer = writer;
	}
	public CSVTableWriter(Writer writer){
		this(new CSVWriter(writer));
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}

	@Override
	public void writeHeader(String[] line) {
		writer.writeNext(line);
	}

	@Override
	public void writeData(String[] line) {
		writer.writeNext(line);
	}
}