package datamerger.canals;

import java.util.Set;

import datamerger.data.*;
import datamerger.data.property.*;

public interface Canal extends DataObject, AlternateIdDataObject{

	@DataPropertySpec(displayName="Name", unique=true)
	DataObjectProperty<String> name();
	
	@DataPropertySpec(displayName="Introduction (English)")
	DataObjectProperty<String> englishIntroduction();
	
	@DataPropertySpec(displayName="Introduzione (Italiano)")
	DataObjectProperty<String> italianIntroduction();
	
	@DataPropertySpec(displayName="GIS Id", unique=true)
	DataObjectProperty<String> gisId();
	
	@DataPropertySpec(displayName="Location", unique=true)
	DataObjectProperty<Location> location();
	
	@DataPropertySpec(displayName="Insula Number", unique=true)
	DataObjectProperty<Integer> insulaNumber();

	@DataPropertySpec(displayName="Length")
	@MeasurementSpec(mismatchTolarance=0.1,unit=Unit.M)
	DataObjectProperty<Measurement> length();

	@DataPropertySpec(displayName="Area")
	@MeasurementSpec(mismatchTolarance=0.1,unit=Unit.M2)
	DataObjectProperty<Measurement> area();

	@DataPropertySpec(displayName="Target Depth")
	@MeasurementSpec(mismatchTolarance=0.1,unit=Unit.CM)
	DataObjectProperty<Measurement> targetDepth();

	@DataPropertySpec(displayName="Average Depth")
	@MeasurementSpec(mismatchTolarance=0.1,unit=Unit.M)
	DataObjectProperty<Measurement> averageDepth();

	@DataPropertySpec(displayName="Segments")
	@CollectionPropertySpec(minSize=1)
	DataObjectProperty<Set<CanalSegment>> segments();
}
