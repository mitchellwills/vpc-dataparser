package datamerger.data.property;

import java.lang.annotation.*;

import datamerger.data.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MeasurementSpec {
	public Unit unit();
	public double mismatchTolarance() default 0;
}
