package datamerger.data.writer;

import java.io.*;

import datamerger.data.writer.DatabaseTableTableWriter.TableWriter;

public class WikimediaTableWriter implements TableWriter, Closeable{
	private final Writer writer;

	public WikimediaTableWriter(Writer writer) throws IOException{
		this.writer = writer;
		writer.append("{|class=\"wikitable sortable\"\n");
	}

	@Override
	public void writeHeader(String[] line) throws IOException {
		writer.append("|-\n");
		for(String header:line){
			writer.append("! ");
			if(header!=null)
				writer.append(header);
			writer.append("\n");
		}
	}

	@Override
	public void writeData(String[] line) throws IOException {
		writer.append("|-\n");
		for(String value:line){
			writer.append("| ");
			if(value!=null)
				writer.append(value);
			writer.append("\n");
		}
	}

	@Override
	public void close() throws IOException {
		writer.append("|}\n");
		writer.close();
	}
}