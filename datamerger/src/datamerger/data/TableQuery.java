package datamerger.data;

public interface TableQuery<T> {
	public static final TableQuery<DataObject> ALL = new TableQuery<DataObject>(){
		@Override
		public boolean matches(DataObject o) {
			return true;
		}
		@Override
		public String toString(){
			return "All";
		}
	};
	public static final TableQuery<DataObject> NONE = new TableQuery<DataObject>(){
		@Override
		public boolean matches(DataObject o) {
			return false;
		}
		@Override
		public String toString(){
			return "None";
		}
	};
	
	boolean matches(T o);
	
	public class BadQueryException extends RuntimeException{
		public BadQueryException(String message){
			super(message);
		}
	}
}
