package datamerger.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DatabaseTable<T extends DataObject> {
	private final String name;
	public DatabaseTable(String name){
		this.name = name;
	}
	
	private Map<String, T> objects = new HashMap<String, T>();
	public T create(Class<T> type, String id){
		if(hasItemWithId(id))
			throw new RuntimeException("There is already an object with the id '"+id+"' in "+name);
		T object = DataObjectFactory.newInstance(type, id);
		objects.put(id, object);
		return object;
	}
	public T createIfNotExist(Class<T> type, String id){
		T object = objects.get(id);
		if(object==null){
			object = DataObjectFactory.newInstance(type, id);
			objects.put(id, object);
		}
		return object;
	}
	
	public boolean hasItemWithId(String id){
		return objects.containsKey(id);
	}
	
	public T getById(String id){
		T o = objects.get(id);
		if(o==null)
			throw new TableQuery.BadQueryException("There is no object with id: '"+id+"' in "+name);
		return o;
	}
	
	public T queryOne(TableQuery<? super T> query){
		for(T o:objects.values()){
			if(query.matches(o))
				return o;
		}
		throw new TableQuery.BadQueryException("There is no object which matches the query '"+query+"' in "+name);
	}
	
	public Collection<T> query(TableQuery<? super T> query){
		Collection<T> items	= new ArrayList<T>();
		for(T o:objects.values()){
			if(query.matches(o))
				items.add(o);
		}
		return items;
	}
}
