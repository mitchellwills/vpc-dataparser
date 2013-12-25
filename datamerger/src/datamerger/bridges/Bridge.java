package datamerger.bridges;

import datamerger.data.*;
import datamerger.data.property.*;

public interface Bridge extends DataObject, AlternateIdDataObject {

	@DataPropertySpec(displayName="Name", unique=true)
	DataObjectProperty<String> name();

	@DataPropertySpec(displayName="Segment Id")
	DataObjectProperty<String> segmentId();

	@DataPropertySpec(displayName="Year Built")
	DataObjectProperty<Integer> yearBuilt();

	@DataPropertySpec(displayName="Numero Zucchetta", unique=true)
	DataObjectProperty<String> numeroZucchetta();

	@DataPropertySpec(displayName="Is Private")
	DataObjectProperty<Boolean> isPrivate();

	@DataPropertySpec(displayName="Arc Style")
	DataObjectProperty<BridgeArcStyle> arcStyle();

	@DataPropertySpec(displayName="Construction Material")
	DataObjectProperty<BridgeConstructionMaterial> constructionMaterial();
	
	@DataPropertySpec(displayName="Handicapped Accessible")
	DataObjectProperty<Boolean> handicappedAccessible();
	
	@DataPropertySpec(displayName="Additional Handrail")
	DataObjectProperty<Boolean> additionalHandrail();
	
	@DataPropertySpec(displayName="North Steps")
	DataObjectProperty<Integer> northSteps();
	
	@DataPropertySpec(displayName="South Steps")
	DataObjectProperty<Integer> southSteps();
	
	@DataPropertySpec(displayName="Total Steps")
	DataObjectProperty<Integer> totalSteps();
	
	@DataPropertySpec(displayName="Height")
	@MeasurementSpec(mismatchTolarance=0.1,unit=Unit.M)
	DataObjectProperty<Measurement> height();
	
	@DataPropertySpec(displayName="Minimum Step Width")
	@MeasurementSpec(mismatchTolarance=0.1,unit=Unit.M)
	DataObjectProperty<Measurement> minimumStepWidth();
	
	@DataPropertySpec(displayName="Overall Width")
	@MeasurementSpec(mismatchTolarance=0.1,unit=Unit.M)
	DataObjectProperty<Measurement> overallWidth();

	@DataPropertySpec(displayName="Decorations", optional=true)
	DataObjectProperty<String> decorations();
	
	@DataPropertySpec(displayName="Note", optional=true)
	DataObjectProperty<String> note();
	
}
