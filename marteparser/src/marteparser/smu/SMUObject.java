package marteparser.smu;

import java.util.Map;

import marteparser.report.SMUObjectReportParser.SMUReport;

public class SMUObject {
	private final String objectId;
	private final String objectTypeName;
	private final Map<String, String> objectInfo;
	private final SMUReport objectReport;

	public SMUObject(String objectId, String objectTypeName, Map<String, String> objectInfo, SMUReport objectReport){
		this.objectId = objectId;
		this.objectTypeName = objectTypeName;
		this.objectInfo = objectInfo;
		this.objectReport = objectReport;
	}

	public String getObjectId() {
		return objectId;
	}

	public String getObjectTypeName() {
		return objectTypeName;
	}

	public Map<String, String> getObjectInfo() {
		return objectInfo;
	}

	public SMUReport getObjectReport() {
		return objectReport;
	}


	
	public static String getTypeId(String typeName){
		switch(typeName){
		case "segmenti":
			return "1";
		case "intersezioni":
			return "2";
		case "sponde":
			return "5";
		case "pavimentazioni":
			return "3";
		case "ponti":
			return "4";
		case "edificato":
			return "6";
		}
		throw new RuntimeException("Unknown type: "+typeName);
	}
	
}
