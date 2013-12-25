package datamerger.data.writer;

import java.io.*;

import datamerger.data.*;

public interface DatabaseTableWriter {
	public <T extends DataObject> void write(DatabaseTable<T> table) throws IOException;
}
