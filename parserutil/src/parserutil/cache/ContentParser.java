package parserutil.cache;

public interface ContentParser<T> {
	public T parse(String content);
}
