package datamerger.canals;

import java.util.Set;

import datamerger.data.*;
import datamerger.data.property.*;

public interface IsmarNode extends DataObject{

	@DataPropertySpec(displayName="Link IDs")
	@CollectionPropertySpec(minSize=1)
	DataObjectProperty<Set<String>> linkIds();

	@DataPropertySpec(displayName="Location")
	DataObjectProperty<Location> location();

}
