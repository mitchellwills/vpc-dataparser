package datamerger.data.property;

import datamerger.data.*;

public interface DataObjectProperty<T> {
	String name();
	DataPropertyDescription description();
	DataObject dataObject();
	
	void set(T value, String source);
	T get();
	
	void validateValue();
}
