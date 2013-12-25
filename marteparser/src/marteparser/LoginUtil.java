package marteparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import parserutil.cache.LocalHttpCache;

public class LoginUtil {
	public static void login(LocalHttpCache httpCache, String username, String password) throws IOException{
		HttpPost request = new HttpPost("http://marte.insula.it/login.asp");
		
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair("destinazione", "/area_smu_area.asp"));
        nvps.add(new BasicNameValuePair("login", username));
        nvps.add(new BasicNameValuePair("password", password));
        nvps.add(new BasicNameValuePair("inviologin", "conferma"));
        request.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
        
		httpCache.executeAndClose(request);
	}
	public static boolean isLoggedIn(LocalHttpCache httpCache) throws IOException{
		HttpGet request = new HttpGet("http://marte.insula.it/area_smu_area.asp");

		String response = httpCache.executeAndClose(request);
		if(response==null)
			return false;
		if(response.contains("<title>LOGIN</title>"))
			return false;
		return true;
	}
}
