package parserutil.datatable;

public class KeyValuePair{
	private final String key;
	private final String value;

	public KeyValuePair(String key, String value){
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "KeyValuePair [key=" + key + ", value=" + value + "]";
	}
	
}