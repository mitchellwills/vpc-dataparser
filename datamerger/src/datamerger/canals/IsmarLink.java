package datamerger.canals;

import java.util.Set;

import datamerger.data.*;
import datamerger.data.property.*;

public interface IsmarLink extends DataObject{
	
	@DataPropertySpec(displayName="Segment IDs")
	@CollectionPropertySpec
	DataObjectProperty<Set<String>> segmentIds();
	
	@DataPropertySpec(displayName="Node IDs")
	@CollectionPropertySpec(minSize=1, maxSize=2)
	DataObjectProperty<Set<String>> nodeIds();

	@DataPropertySpec(displayName="Cemented")
	DataObjectProperty<Boolean> cemented();

}
