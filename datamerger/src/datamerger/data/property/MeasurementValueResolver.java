package datamerger.data.property;

import java.util.*;

import datamerger.data.*;

public class MeasurementValueResolver implements MultipleValueResolver<Measurement> {

	private final MeasurementSpec spec;

	public MeasurementValueResolver(MeasurementSpec spec){
		this.spec = spec;
	}
	
	@Override
	public Measurement resolveValue(Map<String, Measurement> values) {//TODO handle different units
		if(values.isEmpty())
			return null;
		Iterator<Map.Entry<String, Measurement>> valueIterator = values.entrySet().iterator();
		Measurement longest = valueIterator.next().getValue();
		while(valueIterator.hasNext()){//TODO actually figure out which is better
			Measurement next = valueIterator.next().getValue();
			if(Double.toString(longest.value).length() < Double.toString(next.value).length())
				longest = next;
		}
		return longest;
	}
	
	@Override
	public void validateValues(DataObjectProperty<Measurement> property, Map<String, Measurement> values) {
		double mean = 0;//TODO handle different units
		for(Measurement m:values.values())
			mean += m.value;
		mean /= values.size();
		
		boolean valuesOutsideTolerance = false;
		for(Measurement m:values.values()){
			if(Math.abs(m.value - mean) > spec.mismatchTolarance())
				valuesOutsideTolerance = true;
		}
		if(valuesOutsideTolerance){
			String id = property.dataObject().id().get();
			System.err.println("Values outside tolerance ("+spec.mismatchTolarance()+") for '"+property.name()+"' on "+property.description().objectType.getSimpleName()+", id=\""+id+"\"");
			for(Map.Entry<String, Measurement> value:values.entrySet())
				System.err.println("\t"+value.getValue()+" <"+value.getKey()+">");
		}
		for(Map.Entry<String, Measurement> value:values.entrySet()){//TODO don't do this check if unit conversion implemented
			if(value.getValue().unit!=spec.unit())
				System.out.println("Unit mismatch "+value.getValue()+" <"+value.getKey()+"> does not match "+spec.unit()+" for '"+property.name()+"' on "+property.description().objectType.getSimpleName());
		}
	}

}
