package datamerger.data.writer;

import java.io.*;
import java.util.*;

import com.google.common.collect.*;

import datamerger.data.*;
import datamerger.data.property.*;

public class DatabaseTableTableWriter implements DatabaseTableWriter{
	
	private final TableWriter writer;
	
	private TableFilter filter = NULL_TABLE_FILTER;
	
	private final Set<DataColumn> columns = Sets.newLinkedHashSet();

	public DatabaseTableTableWriter(TableWriter writer){
		this.writer = writer;
	}
	
	public interface TableWriter{
		void writeHeader(String[] line) throws IOException;
		void writeData(String[] line) throws IOException;
	}
	
	public interface TableFilter{
		<T> Iterable<T> filter(Iterable<T> input);
	}
	public static TableFilter NULL_TABLE_FILTER = new TableFilter(){
		@Override
		public <T> Iterable<T> filter(Iterable<T> input) {
			return input;
		}
	};
	
	public DatabaseTableTableWriter filter(TableFilter filter){
		this.filter = filter;
		return this;
	}
	
	private Set<String> excludedFields = null;
	public DatabaseTableTableWriter withAllFieldsExcept(String... fields){
		if(excludedFields==null)
			excludedFields = Sets.newHashSet();
		
		for(String field:fields)
			excludedFields.add(field);
		
		return this;
	}
	
	public DatabaseTableTableWriter withColumn(DataColumn column){
		columns.add(column);
		return this;
	}

	@Override
	public <T extends DataObject> void write(DatabaseTable<T> table) throws IOException {
		Iterable<T> filteredItems = filter.filter(table.query(TableQuery.ALL));
		
		if(excludedFields!=null){
			for(T item:filteredItems){
				Collection<DataObjectProperty<?>> properties = item.getProperties();
				for(DataObjectProperty<?> property:properties){
					if(!(excludedFields.contains(property.name()) || property.name().equals("id"))){
						if(Location.class.isAssignableFrom(property.description().propertyTypeClass))
							columns.add(new LocationDataColumn(property.description().spec.displayName(), property.name()));
						else if(Collection.class.isAssignableFrom(property.description().propertyTypeClass))
							columns.add(new CollectionDataColumn(property.description().spec.displayName(), property.name()));
						else if(Measurement.class.isAssignableFrom(property.description().propertyTypeClass))
							columns.add(new MeasurementDataColumn(property.description(), property.name()));
						else
							columns.add(new ToStringDataColumn(property.description().spec.displayName(), property.name()));
					}
				}
			}
		}
		
		
		List<String> nameList = Lists.newArrayList();
		for(DataColumn column:columns)
			column.writeHeader(nameList);
		
		String[] headers = nameList.toArray(new String[nameList.size()]);
		
		writer.writeHeader(headers);
		
		String[] dataRow = new String[headers.length];
		for(T item:filteredItems){
			int i = 0;
			for(DataColumn column:columns){
				i = column.writeData(i, dataRow, item);
			}
			writer.writeData(dataRow);
		}
	}
	
	
	

	
	public static abstract class DataColumn{
		private final String columnId;
		public DataColumn(String columnId){
			this.columnId = columnId;
		}
		public abstract void writeHeader(List<String> headers);
		public abstract int writeData(int start, String[] dataRow, DataObject item);
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((columnId == null) ? 0 : columnId.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DataColumn other = (DataColumn) obj;
			if (columnId == null) {
				if (other.columnId != null)
					return false;
			} else if (!columnId.equals(other.columnId))
				return false;
			return true;
		}
	}
	public static abstract class SingleColumnDataColumn extends DataColumn{
		private final String header;
		public SingleColumnDataColumn(String header){
			super(header);
			this.header = header;
		}
		@Override
		public void writeHeader(List<String> headers) {
			headers.add(header);
		}
		@Override
		public final int writeData(int start, String[] dataRow, DataObject item) {
			dataRow[start] = createData(item);
			return start+1;
		}

		public abstract String createData(DataObject item);
	}
	public static abstract class SingleColumnDataColumnFromProperty extends SingleColumnDataColumn{
		private final String propertyName;
		public SingleColumnDataColumnFromProperty(String header, String propertyName){
			super(header);
			this.propertyName = propertyName;
		}

		@Override
		public final String createData(DataObject item){
			DataObjectProperty<Object> property = item.getProperty(propertyName);
			if(property==null || property.get()==null)
				return null;
			else
				return createDataFromProperty(property.get());
		}
		public abstract String createDataFromProperty(Object value);
	}
	public static class CollectionDataColumn extends SingleColumnDataColumnFromProperty{
		public CollectionDataColumn(String header, String key){
			super(header, key);
		}
		@Override
		public String createDataFromProperty(Object value) {
			Collection<?> collection = (Collection<?>)value;
			if(collection.isEmpty())
				return null;
			StringBuilder builder = new StringBuilder();
			Iterator<?> iterator = collection.iterator();
			while(iterator.hasNext()){
				Object entry = iterator.next();
				builder.append(Objects.toString(entry));
				if(iterator.hasNext())
					builder.append(",");
			}
			
			return builder.toString();
		}
	}
	public static class MeasurementDataColumn extends SingleColumnDataColumnFromProperty{
		public static MeasurementDataColumn forType(Class<? extends DataObject> type, String key){
			Set<DataPropertyDescription> properties = DataPropertyDescription.enumerate(type);
			for(DataPropertyDescription desc:properties){
				if(desc.name.equals(key))
					return new MeasurementDataColumn(desc, key);
			}
			throw new RuntimeException("Property not found");
		}
		public MeasurementDataColumn(DataPropertyDescription description, String key){
			super(description.spec.displayName()+" ("+description.getAnnotation(MeasurementSpec.class).unit()+")", key);
		}
		@Override
		public String createDataFromProperty(Object value) {
			Measurement measurement = (Measurement)value;
			return Double.toString(measurement.value);
		}
	}
	public static class ToStringDataColumn extends SingleColumnDataColumnFromProperty{
		public ToStringDataColumn(String header, String key){
			super(header, key);
		}
		@Override
		public String createDataFromProperty(Object value) {
			return value.toString();
		}
	}
	public static class LocationDataColumn extends DataColumn{
		private String lonHeader;
		private String latHeader;
		private final String key;
		public LocationDataColumn(String header, String key){
			this(header+"-lon", header+"-lat", key, header);
		}
		public LocationDataColumn(String lonHeader, String latHeader, String key){
			this(lonHeader, latHeader, key, lonHeader+"-"+latHeader);
		}
		public LocationDataColumn(String lonHeader, String latHeader, String key, String columnId){
			super(columnId);
			this.lonHeader = lonHeader;
			this.latHeader = latHeader;
			this.key = key;
		}
		@Override
		public void writeHeader(List<String> headers) {
			headers.add(lonHeader);
			headers.add(latHeader);
		}
		@Override
		public int writeData(int start, String[] dataRow, DataObject item) {
			DataObjectProperty<Object> property = item.getProperty(key);
			if(property==null || property.get()==null){
				dataRow[start] = null;
				dataRow[start+1] = null;
			}
			else{
				Location location = (Location)property.get();
				dataRow[start] = Double.toString(location.lon);
				dataRow[start+1] = Double.toString(location.lat);
			}
			return start+2;
		}
	}
	

}
