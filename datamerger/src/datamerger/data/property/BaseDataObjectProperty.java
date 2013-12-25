package datamerger.data.property;

import java.util.*;

import com.google.common.collect.*;

import datamerger.data.*;


public class BaseDataObjectProperty<T> implements DataObjectProperty<T>{

	private final DataPropertyDescription description;
	private final DataObject o;
	
	public String name(){return description.name;}
	public DataPropertyDescription description(){return description;}
	public DataObject dataObject(){return o;}
	
	
	private final MultipleValueResolver<T> valueResolver;
	
	@SuppressWarnings("unchecked")
	public BaseDataObjectProperty(DataPropertyDescription description, DataObject o){
		this.description = description;
		this.o = o;
		value = (T)description.defaultValue();
		if(Measurement.class.isAssignableFrom(description.propertyTypeClass))
			valueResolver = (MultipleValueResolver<T>) new MeasurementValueResolver(description.getAnnotation(MeasurementSpec.class));
		else
			valueResolver = new EqualValueResolver<>();
	}

	private final Map<String, T> values = Maps.newHashMap();
	private T value;

	@Override
	public void set(T newValue, String source){
		if(newValue==null)
			return;
		T oldValueFromSource = values.get(source);
		if(oldValueFromSource!=null && !oldValueFromSource.equals(newValue)){
			System.err.println("Multiple values from <"+source+"> for property '"+name()+"' on: "+dataObject());
			System.err.println("\tFirst Value: "+oldValueFromSource);
			System.err.println("\tNew Value: "+newValue);
		}
		else
			values.put(source, newValue);
		value = valueResolver.resolveValue(values);
	}

	@Override
	public T get(){
		return value;
	}
	
	@Override
	public void validateValue() {
		valueResolver.validateValues(this, values);
	}
}