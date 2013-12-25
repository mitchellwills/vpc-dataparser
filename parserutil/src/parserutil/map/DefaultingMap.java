package parserutil.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class DefaultingMap<K, V> {
	public static <K, V> DefaultingMap<K, V> from(final DefaultingMap<K, V> defaultValues){
		return new DefaultingMap<K, V>(){
			@Override
			protected V createDefaultValue(K key) {
				return defaultValues.get(key);
			}
		};
	}
	
	private final Map<K, V> map = new HashMap<K, V>();
	
	public V get(K key){
		V value = map.get(key);
		if(value == null){
			value = createDefaultValue(key);
			map.put(key, value);
		}
		return value;
	}
	
	public Collection<V> items(){
		return map.values();
	}

	protected abstract V createDefaultValue(K key);
}
