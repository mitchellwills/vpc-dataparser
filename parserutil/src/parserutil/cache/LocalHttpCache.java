package parserutil.cache;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class LocalHttpCache {
	private final BasicCookieStore cookieStore = new BasicCookieStore();
	private final CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
	private final Path cachePath;
	private final Path cookieFile;
	private final long requestDelay;
	private boolean cacheOnly = false;
	
	public LocalHttpCache(long requestDelay) {
		this.requestDelay = requestDelay;
		try{
			cachePath = Files.createDirectories(Paths.get("webcache"));
			cookieFile = cachePath.resolve("cookies");
			if(!Files.exists(cookieFile))
				Files.createFile(cookieFile);
			loadCookies();
		} catch(IOException e){
			throw new RuntimeException("Error initializing cache", e);
		}
	}
	public void cacheOnly() {
		cacheOnly = true;
	}
	
	public String get(String url) throws IOException{
		return requestWithCache(urlToName(url), new HttpGet(url));
	}
	
	public Path getCacheFile(String name){
		return cachePath.resolve(nameToFileName(name));
	}
	
	public <T> T requestProcessAndCache(String name, HttpUriRequest request, ContentParser<T> parser) throws IOException{
		String content = requestWithCache(name, request);
		T result;
		try{
			result = parser.parse(content);
		} catch(Exception e){
        	removeCached(name);
			throw e;
		}
		return result;
	}
	
	private String requestWithCache(String name, HttpUriRequest request) throws IOException{
		Path localFile = getCacheFile(name);
		if(Files.exists(localFile))//file is cached
			return new String(Files.readAllBytes(localFile));
		if(cacheOnly)
			throw new IOException("running local only");

		String contents = executeAndClose(request);
		
		Files.write(localFile, contents.getBytes());
		
		return contents;
	}

	private void removeCached(String name) throws IOException{
		Files.delete(getCacheFile(name));
	}

	public CloseableHttpResponse execute(HttpUriRequest request) throws ClientProtocolException, IOException{
		try {
			Thread.sleep(requestDelay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		CloseableHttpResponse response = httpclient.execute(request);
		System.out.println(response.getStatusLine());
		return response;
	}
	
	public String executeAndClose(HttpUriRequest request) throws IOException{
		CloseableHttpResponse response = execute(request);
		String contents;
		try {
            HttpEntity entity = response.getEntity();
		    contents = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
    		if(response.getStatusLine().getStatusCode()==500)
    			System.out.println(contents);
        } finally {
        	response.close();
        }
		if(contents==null)
			throw new IOException("Null response contents...");
		return contents;
	}


	public void saveCookies() throws IOException {
		List<Cookie> cookies = cookieStore.getCookies();
    	try(CSVWriter cookieWriter = new CSVWriter(Files.newBufferedWriter(cookieFile, Charset.defaultCharset()))){
	        for (Cookie cookie:cookies) {
	        	cookieWriter.writeNext(new String[]{
	        			cookie.getName(),
	        			cookie.getValue(),
	        			Integer.toString(cookie.getVersion()),
	        			cookie.getDomain(),
	        			cookie.getPath()
	        			});
	        }
    	}
	}
	public void loadCookies() throws IOException{
		try(CSVReader cookieReader = new CSVReader(Files.newBufferedReader(cookieFile, Charset.defaultCharset()))){
			List<String[]> cookies = cookieReader.readAll();
			for(String[] cookieData:cookies){
				BasicClientCookie cookie = new BasicClientCookie(cookieData[0], cookieData[1]);
				cookie.setVersion(Integer.parseInt(cookieData[2]));
				cookie.setDomain(cookieData[3]);
				cookie.setPath(cookieData[4]);
				cookieStore.addCookie(cookie);
			}
		}
	}
	public List<Cookie> getCookies(){
		return cookieStore.getCookies();
	}
	
	
	private static String nameToFileName(String name){
		return name+".cache";
	}

	
	private static String urlToName(String url) {
		return url.replace(':', '_').replace('/', '_').replace('?', '-').replace('&', '-').replace('&', '-');
	}

}
