package datamerger.data;

import java.util.*;

import datamerger.data.property.*;


public interface DataObject {

	public class Queries {
	
		public static TableQuery<DataObject> byId(final String id){
			return new TableQuery<DataObject>(){
				@Override
				public boolean matches(DataObject o) {
					return o.id().get().equals(id);
				}
				@Override
				public String toString(){
					return "ById: "+id;
				}
			};
		}
	}

	@DataPropertySpec(displayName="id")
	public DataObjectProperty<String> id();

	Class<? extends DataObject> getType();
	Collection<DataObjectProperty<?>> getProperties();
	<T> DataObjectProperty<T> getProperty(String name);

}
