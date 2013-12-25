package datamerger.data;

import java.lang.reflect.*;
import java.util.*;

import javassist.util.proxy.*;
import javassist.util.proxy.Proxy;

import com.google.common.collect.*;

import datamerger.data.property.*;

public class DataObjectFactory {
	
	@SuppressWarnings("unchecked")
	public static <T extends DataObject> T newInstance(Class<T> type, String id) {
		ProxyFactory f = new ProxyFactory();
		f.setInterfaces(new Class[] { type });
		f.setSuperclass(DataObjectBase.class);

		f.setFilter(new MethodFilter() {
			public boolean isHandled(Method m) {
				return m.getAnnotation(DataPropertySpec.class) != null;
			}
		});

		try {
			T instance = (T)f.create(new Class<?>[]{String.class, Class.class}, new Object[]{id, type});

			MethodHandler mi = new MethodHandler() {
				public Object invoke(Object self, Method m, Method proceed, Object[] args) throws Throwable {
					DataObjectBase<?> _this = (DataObjectBase<?>)self;
					String propertyName = m.getName();
					return _this.getProperty(propertyName);
				}
			};
			
			((Proxy) instance).setHandler(mi);
			
			return instance;
		} catch (NoSuchMethodException | IllegalArgumentException
				| InstantiationException | IllegalAccessException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public static abstract class DataObjectBase<T extends DataObject> implements DataObject{
		
		private final Class<T> type;
		public DataObjectBase(String id, Class<T> type){
			this.type = type;
			
			Set<DataPropertyDescription> propertyDescriptions = DataPropertyDescription.enumerate(type);
			
			properties = Maps.newHashMap();
			for(DataPropertyDescription propDesc:propertyDescriptions){
				properties.put(propDesc.name, new BaseDataObjectProperty<Object>(propDesc, this));
			}
			getProperty("id").set(id, "Creation ID");//Can't call property functions until after constructor completes
		}

		private final Map<String, DataObjectProperty<?>> properties;
		@Override
		public Class<T> getType(){
			return type;
		}
		@Override
		public Collection<DataObjectProperty<?>> getProperties(){
			return properties.values();
		}
		@SuppressWarnings("unchecked")
		@Override
		public <PT> DataObjectProperty<PT> getProperty(String name){
			DataObjectProperty<PT> property = (DataObjectProperty<PT>)properties.get(name);
			if(property==null)
				throw new RuntimeException("Proptery "+name+" does not exist for "+type);
			return property;
		}
		

		
		@Override
		public final String toString() {
			return getType().getSimpleName()+" [id=" + id().get() + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id() == null) ? 0 : id().hashCode());
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
			DataObject other = (DataObject) obj;
			if (id() == null) {
				if (other.id() != null)
					return false;
			} else if (!id().equals(other.id()))
				return false;
			return true;
		}
		
	}
}
