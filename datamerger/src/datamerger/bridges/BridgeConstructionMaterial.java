package datamerger.bridges;

import java.util.*;

import com.google.common.collect.*;

public enum BridgeConstructionMaterial {
	Legno(4, "Legno"),
	LegnoLamellare(5, "Legno Lamellare"),
	Metallo(6, "Metallo"),
	CementoArmato(9, "Cemento Armato"),
	Calcestruzzo(10, "Calcestruzzo"),
	MattoniEPietraDIstria(14, "Mattoni e Pietra d'Istria"),
	MattoniIntonacoEPietraDIstria(15, "Mattoni, Intonaco e Pietra d'Istria"),
	MattoniEMetallo(16, "Mattoni e Metallo"),
	LegnoEMetallo(17, "Legno e Metallo"),
	MetalloEPietraDIstria(19, "Metallo e Pietra d'Istria"),
	MetalloPietraDIstriaELegno(20, "Metallo, Pietra d'Istria e Legno"),
	MetalloEAsfalto(21, "Metallo e Asfalto"),
	MetalloMattoniAsfaltoEPietraDIstria(22, "Metallo, Mattoni, Asfalto e Pietra d'Istria"),
	MetalloPietraDIstriaEMattoni(23, "Metallo, Pietra d'Istria e Mattoni"),
	MetalloECementoArmato(24, "Metallo e Cemento Armato"),
	MetalloECalcestruzzo(26, "Metallo e Calcestruzzo");
	

	
	private static Map<Integer, BridgeConstructionMaterial> idToMaterialMap = Maps.newHashMap();
	static{
		for(BridgeConstructionMaterial material:values())
			idToMaterialMap.put(material.id, material);
	}
	public static BridgeConstructionMaterial fromId(Integer id){
		if(id==null)
			return null;
		BridgeConstructionMaterial style = idToMaterialMap.get(id);
		if(style==null)
			throw new RuntimeException("Could not resolve "+id+" to bridge construction material");
		return style;
	}
	
	public final int id;
	public final String italianName;

	private BridgeConstructionMaterial(int id, String italianName){
		this.id = id;
		this.italianName = italianName;
	}
}
