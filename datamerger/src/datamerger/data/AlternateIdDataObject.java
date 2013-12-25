package datamerger.data;

import java.util.*;

import datamerger.data.property.*;

public interface AlternateIdDataObject extends DataObject{
	
	public class Queries{
		public static TableQuery<AlternateIdDataObject> byIdOrAlternate(final String id){
			return new TableQuery<AlternateIdDataObject>(){
				@Override
				public boolean matches(AlternateIdDataObject o) {
					return o.id().get().equals(id) || o.alternateIds().get().contains(id);
				}
				@Override
				public String toString(){
					return "ByIdOrAlternate: "+id;
				}
			};
		}
	}

	@DataPropertySpec(displayName="Alternate IDs")
	@CollectionPropertySpec
	DataObjectProperty<Set<String>> alternateIds();
	
}
