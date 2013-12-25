package datamerger.data.property;

import java.util.*;

public interface MultipleValueResolver<T> {
	T resolveValue(Map<String, T> values);
	void validateValues(DataObjectProperty<T> property, Map<String, T> values);
}
