package parserutil.cache;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class HtmlContentParser<T> implements ContentParser<T>{

	@Override
	public final T parse(String content) {
		return parse(Jsoup.parse(content));
	}
	
	public abstract T parse(Document document);

}
