package datamerger.canals;

import java.util.*;

import datamerger.data.*;
import datamerger.data.property.*;

public interface CanalSegment extends DataObject, AlternateIdDataObject{
	
	@DataPropertySpec(displayName="Canal ID")
	DataObjectProperty<String> canalId();
	
	@DataPropertySpec(displayName="GIS Total Id", unique=true)
	DataObjectProperty<String> gisTotalId();
	
	@DataPropertySpec(displayName="GIS Id", unique=true)
	DataObjectProperty<String> gisId();
	
	@DataPropertySpec(displayName="Location", unique=true)
	DataObjectProperty<Location> location();

	@DataPropertySpec(displayName="Cemented")
	DataObjectProperty<Boolean> cemented();

	@DataPropertySpec(displayName="Average Width")
	@MeasurementSpec(mismatchTolarance=0.5,unit=Unit.M)
	DataObjectProperty<Measurement> averageWidth();

	@DataPropertySpec(displayName="Minimum Width")
	@MeasurementSpec(unit=Unit.M)
	DataObjectProperty<Measurement> minWidth();

	@DataPropertySpec(displayName="Area")
	@MeasurementSpec(mismatchTolarance=0.5,unit=Unit.M2)
	DataObjectProperty<Measurement> area();

	@DataPropertySpec(displayName="Length")
	@MeasurementSpec(mismatchTolarance=0.1,unit=Unit.M)
	DataObjectProperty<Measurement> length();

	@DataPropertySpec(displayName="Job Items")
	@CollectionPropertySpec
	DataObjectProperty<List<JobItem>> jobItems();
	
	@DataPropertySpec(displayName="ISMAR Links")
	@CollectionPropertySpec
	DataObjectProperty<List<IsmarLink>> ismarLinks();

	
}
