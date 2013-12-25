package datamerger.bridges;

import java.util.*;

import com.google.common.collect.*;

public enum BridgeArcStyle {
	ATuttoSesto(1, "A tutto Sesto"),
	Piano(2, "Piano"),
	Policentrico(3, "Policentrico"),
	SestoAcuto(4, "Sesto Acuto"),
	SestoRibassato(6, "Sesto Ribassato"),
	SestoSuperRibassato(7, "Sesto Super Ribassato"),
	Trapezoidale(8, "Trapezoidale"),
	Irregolare(10, "Irregolare");
	

	
	private static Map<Integer, BridgeArcStyle> idToStyleMap = Maps.newHashMap();
	static{
		for(BridgeArcStyle style:values())
			idToStyleMap.put(style.id, style);
	}
	public static BridgeArcStyle fromId(Integer id){
		if(id==null)
			return null;
		BridgeArcStyle style = idToStyleMap.get(id);
		if(style==null)
			throw new RuntimeException("Could not resolve "+id+" to bridge arc style");
		return style;
	}
	
	public final int id;
	public final String italianName;

	private BridgeArcStyle(int id, String italianName){
		this.id = id;
		this.italianName = italianName;
	}
}
