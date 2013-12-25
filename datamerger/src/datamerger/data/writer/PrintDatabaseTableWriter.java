package datamerger.data.writer;

import java.io.*;
import java.util.*;

import datamerger.data.*;
import datamerger.data.property.*;

public class PrintDatabaseTableWriter implements DatabaseTableWriter {
	private final PrintStream stream;

	public PrintDatabaseTableWriter(PrintStream stream){
		this.stream = stream;
	}
	
	@Override
	public <T extends DataObject> void write(DatabaseTable<T> table) {
		print(stream, table, new Prefix(""));
	}
	
	private static void print(PrintStream stream, DatabaseTable<?> data, Prefix prefix){
		for(Object item:data.query(TableQuery.ALL)){
			print(stream, item, prefix);
		}
	}
	private static void print(PrintStream stream, Collection<?> list, Prefix prefix){
		for(Object item:list){
			print(stream, item, prefix);
		}
	}
	
	private static void print(PrintStream stream, Object item, Prefix prefix){
		if(item == null){
			stream.println(prefix+"null");
		}
		else if(item instanceof DataObject){
			DataObject data = (DataObject) item;
			stream.println(prefix.toString()+data.getType());
			Prefix childPrefix = prefix.inc();
			for(DataObjectProperty<?> property:data.getProperties()){
				String name = property.description().spec.displayName();
				Object value = property.get();
				print(stream, name, value, childPrefix);
			}
		}
		else
			stream.println(prefix.toString()+item.toString());
	}
	private static void print(PrintStream stream, String key, Object value, Prefix prefix){
		if(value instanceof Collection){
			stream.println(prefix+key+": ");
			print(stream, (Collection<?>)value, prefix.inc());
		}
		else if(value!=null){
			stream.println(prefix+key+": "+value);
		}
		//don't print if no value is present
	}
	

	private static class Prefix{
		private final String prefix;
		
		public Prefix(String prefix){
			this.prefix = prefix;
		}

		public Prefix inc(){
			return new Prefix(prefix+"\t");
		}
		
		public String toString(){
			return prefix;
		}
	}

}
