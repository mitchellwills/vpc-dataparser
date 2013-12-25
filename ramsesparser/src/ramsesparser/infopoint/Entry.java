package ramsesparser.infopoint;

import java.util.List;
import java.util.Map;

import parserutil.datatable.KeyValuePair;

public class Entry{
	private final KeyValuePair title;
	private final String imageUrl;
	private Map<String, List<KeyValuePair>> values;

	public Entry(KeyValuePair title, String imageUrl, Map<String, List<KeyValuePair>> values){
		this.title = title;
		this.imageUrl = imageUrl;
		this.values = values;
	}

	public KeyValuePair getTitle() {
		return title;
	}

	public String getImageUrl() {
		return imageUrl;
	}
	public Map<String, List<KeyValuePair>> valueSetMap(){
		return values;
	}
}