package datamerger.canals;

import java.util.*;

import org.joda.time.*;

import datamerger.data.*;
import datamerger.data.property.*;


public interface JobItem extends DataObject{
	public enum JobItemState{
		Finished, InProgress
	}

	@DataPropertySpec(displayName="State")
	DataObjectProperty<JobItemState> state();
	@DataPropertySpec(displayName="Note", optional=true)
	DataObjectProperty<String> note();

	@DataPropertySpec(displayName="Start Date", optional=true)
	DataObjectProperty<LocalDate> startDate();
	@DataPropertySpec(displayName="End Date", optional=true)
	DataObjectProperty<LocalDate> endDate();

	@DataPropertySpec(displayName="Average Depth", optional=true)
	@MeasurementSpec(unit=Unit.CM)
	DataObjectProperty<Measurement> averageDepth();

	@DataPropertySpec(displayName="Target Depth")
	@MeasurementSpec(unit=Unit.CM)
	DataObjectProperty<Measurement> targetDepth();
	
	@DataPropertySpec(displayName="Landfill Cost", optional=true)
	@MeasurementSpec(unit=Unit.Euro)
	DataObjectProperty<Measurement> landfillCost();
	
	@DataPropertySpec(displayName="Dry Dredge Cost", optional=true)
	@MeasurementSpec(unit=Unit.Euro)
	DataObjectProperty<Measurement> dryDredgeCost();
	
	@DataPropertySpec(displayName="Wet Dredge Cost", optional=true)
	@MeasurementSpec(unit=Unit.Euro)
	DataObjectProperty<Measurement> wetDredgeCost();
	
	@DataPropertySpec(displayName="Dry Dredged", optional=true)
	@MeasurementSpec(unit=Unit.M3)
	DataObjectProperty<Measurement> dryDredged();
	
	@DataPropertySpec(displayName="Wet Dredged", optional=true)
	@MeasurementSpec(unit=Unit.M3)
	DataObjectProperty<Measurement> wetDredged();
	
	@DataPropertySpec(displayName="Mud Volume", optional=true)
	@MeasurementSpec(unit=Unit.M3)
	DataObjectProperty<Measurement> mudVolume();

	@DataPropertySpec(displayName="Segment IDs")
	@CollectionPropertySpec
	DataObjectProperty<List<String>> segmentIds();

}
