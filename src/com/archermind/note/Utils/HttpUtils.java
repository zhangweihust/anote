package com.archermind.note.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HttpUtils {

	public static String doPost(Map<String, String> parmas, String url) {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);

		ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		if (parmas != null) {
			Set<String> keys = parmas.keySet();
			for (Iterator<String> i = keys.iterator(); i.hasNext();) {
				String key = (String) i.next();
				pairs.add(new BasicNameValuePair(key, parmas.get(key)));
			}
		}

		try {
			UrlEncodedFormEntity p_entity = new UrlEncodedFormEntity(pairs,
					HTTP.UTF_8);

			httpPost.setEntity(p_entity);

			HttpResponse response = client.execute(httpPost);

			if (response.getStatusLine().getStatusCode() == 200) {
				String strResult = EntityUtils.toString(response.getEntity());
				strResult = strResult.replace("\"", "");
				Log.i("HttpUtils", "strResult:" + strResult);
				return strResult;
			}
			Log.e("HttpUtils", "Http Error Response: "+response.getStatusLine().getStatusCode());
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		return String.valueOf(ServerInterface.ERROR_SERVER_INTERNAL);
	}

}