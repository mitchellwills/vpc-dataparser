package datamerger.data.property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataPropertySpec {
	public String displayName();
	public boolean optional() default false;
	public boolean unique() default false;
}
