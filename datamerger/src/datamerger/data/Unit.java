package datamerger.data;

public enum Unit{
	M("m", UnitType.Length), CM("cm", UnitType.Length),
	M2("m^2", UnitType.Area),
	M3("m^3", UnitType.Volume),
	Euro("Euro", UnitType.Cost),

	UNKNOWN("Unknown", null);
	

	private final String displayName;
	private final UnitType type;
	private Unit(String displayName, UnitType type){
		this.displayName = displayName;
		this.type = type;
	}
	public UnitType getType(){
		return type;
	}
	@Override
	public String toString(){
		return displayName;
	}
	
	public enum UnitType{
		Length, Area, Volume, Cost
	}
	
}