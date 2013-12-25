package datamerger.data.property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CollectionPropertySpec {
	int minSize() default 0;
	int maxSize() default Integer.MAX_VALUE;
	boolean initWithEmptyCollection() default true;
}
