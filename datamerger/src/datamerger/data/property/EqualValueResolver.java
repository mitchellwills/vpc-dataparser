package datamerger.data.property;

import java.util.*;

public class EqualValueResolver<T> implements MultipleValueResolver<T> {

	@Override
	public T resolveValue(Map<String, T> values) {
		if(values.isEmpty())
			return null;
		return values.values().iterator().next();
	}
	
	@Override
	public void validateValues(DataObjectProperty<T> property, Map<String, T> values) {
		if(values.isEmpty())
			return;
		boolean allEqual = true;
		Iterator<Map.Entry<String, T>> valueIterator = values.entrySet().iterator();
		T first = valueIterator.next().getValue();
		while(valueIterator.hasNext()){
			T value = valueIterator.next().getValue();
			if(!first.equals(value))
				allEqual = false;
		}
		
		if(!allEqual){
			String id = property.dataObject().id().get();
			System.err.println("Conflicting values for "+property.name()+" on "+property.description().objectType.getSimpleName()+", id=\""+id+"\"");
			for(Map.Entry<String, T> value:values.entrySet())
				System.err.println("\t"+value.getValue()+" <"+value.getKey()+">");
		}
	}


}
