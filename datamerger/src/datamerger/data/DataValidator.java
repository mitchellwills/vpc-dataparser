package datamerger.data;

import java.util.*;

import com.google.common.collect.*;

import datamerger.data.property.*;


public class DataValidator {
	private static final boolean IGNORE_NON_OPTIONAL_VALUES = true;
	
	
	public static <T extends DataObject> void validateTable(DatabaseTable<T> data){
		Set<DataPropertyDescription> properties = Sets.newHashSet();
		Collection<T> allValues = data.query(TableQuery.ALL);
		for(DataObject o:allValues){
			validate(o);
			properties.addAll(DataPropertyDescription.enumerate(o.getType()));
		}
		for(DataPropertyDescription property:properties){
			if(property.spec.unique()){
				Multimap<Object, DataObject> values = HashMultimap.create();
				for(DataObject o:allValues){
					if(property.propertyTypeClass.isInstance(o))
						values.put(o.getProperty(property.name).get(), o);
				}
				for(Object value:values.keySet()){
					if(value==null)
						continue;
					Collection<DataObject> dataObjects = values.get(value);
					if(dataObjects.size()>1)
						System.err.println("Multiple objects have '"+value+"' for "+property.name+" - "+dataObjects);
				}
			}
		}
	}
	
	public static void validate(DataObject o){
		for(DataObjectProperty<?> property:o.getProperties()){
			Object value = property.get();
			
			if(!IGNORE_NON_OPTIONAL_VALUES){
				if(!property.description().spec.optional() && value==null)
					System.err.println(o+" is missing non-optional value: "+property.name());
			}
			
			property.validateValue();
			
			if(Collection.class.isAssignableFrom(property.description().propertyTypeClass)){
				Collection<?> collection = (Collection<?>)value;
				CollectionPropertySpec collectionSpec = property.description().getAnnotation(CollectionPropertySpec.class);
				if(collection.size() < collectionSpec.minSize())
					System.err.println(o+" property "+property.name()+" is too small minSize: "+collectionSpec.minSize()+", is "+collection.size());
				if(collection.size() > collectionSpec.maxSize())
					System.err.println(o+" property "+property.name()+" is too large maxSize: "+collectionSpec.maxSize()+", is "+collection.size());
			}
		}
	}
}
