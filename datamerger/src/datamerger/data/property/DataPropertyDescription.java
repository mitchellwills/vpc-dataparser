package datamerger.data.property;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import com.google.common.collect.*;

import datamerger.data.*;

public class DataPropertyDescription {
	
	@SuppressWarnings("unchecked")
	public static Set<DataPropertyDescription> enumerate(Class<? extends DataObject> objectType){
		if(!objectType.isInterface())
			throw new RuntimeException("descriptions must be enumerated on an interface: "+objectType);
		
		Set<DataPropertyDescription> properties = Sets.newHashSet();
		for(Method method:objectType.getMethods()){
			DataPropertySpec spec = method.getAnnotation(DataPropertySpec.class);

			if(spec!=null){
				Type valueType = null;
				Type methodReturnType = method.getGenericReturnType();
				if(methodReturnType instanceof ParameterizedType){
					Class<?> rawType = (Class<?>)((ParameterizedType) methodReturnType).getRawType();
					if(DataObjectProperty.class.isAssignableFrom(rawType)){
						valueType = ((ParameterizedType) methodReturnType).getActualTypeArguments()[0];
					}
				}
				if(valueType==null)
					throw new RuntimeException("property '"+method.getName()+"' on type "+objectType+" must have a return type of DataObjectProperty<T>");

				
				ImmutableClassToInstanceMap.Builder<Annotation> annotationCollectionBuilder = ImmutableClassToInstanceMap.<Annotation>builder();
				for(Annotation annotation:method.getAnnotations()){
					annotationCollectionBuilder.put((Class<Annotation>)annotation.annotationType(), annotation);
				}
				properties.add(new DataPropertyDescription(method.getName(), objectType, valueType, annotationCollectionBuilder.build()));
			}
		}
		return properties;
	}
	public static Class<?> asRaw(Type t){
		if(t instanceof Class)
			return (Class<?>) t;
		if(t instanceof ParameterizedType)
			return (Class<?>)((ParameterizedType) t).getRawType();
		throw new RuntimeException("Cannot cast "+t+" to raw class");
	}
	
	
	
	public final String name;
	public final Class<? extends DataObject> objectType;
	public final Type propertyType;
	public final Class<?> propertyTypeClass;
	public final DataPropertySpec spec;
	private final ImmutableClassToInstanceMap<Annotation> annotations;
	
	private DataPropertyDescription(String name, Class<? extends DataObject> objectType, Type propertyType, ImmutableClassToInstanceMap<Annotation> annotations){
		this.name = name;
		this.objectType = objectType;
		this.propertyType = propertyType;
		propertyTypeClass = asRaw(propertyType);
		this.annotations = annotations;

		spec = getAnnotation(DataPropertySpec.class);

		if(Collection.class.isAssignableFrom(propertyTypeClass)){
			CollectionPropertySpec collectionSpec = getAnnotation(CollectionPropertySpec.class);
			if(collectionSpec==null)
				throw new RuntimeException(name+" collection in "+objectType+" does not have a CollectionPropertySpec attribute");
		}
	}

	public <T extends Annotation> T getAnnotation(Class<T> type) {
		T annotation = annotations.getInstance(type);
		if(annotation==null)
			throw new RuntimeException("Annotation: "+type+" is not present on '"+name+"' on type "+objectType);
		return annotation;
	}

	Object defaultValue() {
		if(Collection.class.isAssignableFrom(propertyTypeClass)){
			CollectionPropertySpec collectionSpec = getAnnotation(CollectionPropertySpec.class);
			if(collectionSpec.initWithEmptyCollection()){
				if(List.class.isAssignableFrom(propertyTypeClass))
					return Lists.newArrayList();
				else if(Set.class.isAssignableFrom(propertyTypeClass))
					return Sets.newHashSet();
				else
					throw new RuntimeException("Unsupported property collection type: "+propertyType);
			}
			return null;
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((objectType == null) ? 0 : objectType.hashCode());
		result = prime * result
				+ ((propertyType == null) ? 0 : propertyType.hashCode());
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
		DataPropertyDescription other = (DataPropertyDescription) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (objectType == null) {
			if (other.objectType != null)
				return false;
		} else if (!objectType.equals(other.objectType))
			return false;
		if (propertyType == null) {
			if (other.propertyType != null)
				return false;
		} else if (!propertyType.equals(other.propertyType))
			return false;
		return true;
	}
	
}
