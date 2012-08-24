package com.archermind.note.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
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
				String strResult = EntityUtils.toString(response.getEntity(),HTTP.UTF_8);
				//strResult = strResult.replace("\"", "");
				//Log.i("HttpUtils", "strResult:" + strResult);
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
	public static long getFileSize(String sURL) {
		int nFileLength = -1;
		try {
			URL url = new URL(sURL);
			HttpURLConnection httpConnection = (HttpURLConnection) url
					.openConnection();
			httpConnection.setConnectTimeout(30000);
			httpConnection.setReadTimeout(30000);
			int responseCode = httpConnection.getResponseCode();
			if (responseCode >= 400) {
				System.err.println("Error Code : " + responseCode);
				return -2; // -2 represent access is error
			}
			String sHeader;
			for (int i = 1;; i++) {
				sHeader = httpConnection.getHeaderFieldKey(i);
				if (sHeader != null) {
					if (sHeader.equals("Content-Length")) {
						nFileLength = Integer.parseInt(httpConnection
								.getHeaderField(sHeader));
						break;
					}
				}

				else
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(nFileLength);
		return nFileLength;
	}

	public static long DownloadFile( String sURL,String sFilepath) {
		long nStartPos = 0;
		int nRead = 0;
		String sName = sURL;
		File file = new File(sFilepath);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			}
		} else {
			nStartPos =file.length();//有一种情况就是程序装之前本地aMusic文件下面有没有下载完的临时文件。
			System.out.println("nStartPos=="+nStartPos);
		}		
		try {
			URL url = new URL(sURL);
			HttpURLConnection httpConnection = (HttpURLConnection) url
					.openConnection();
			long nEndPos = getFileSize(sURL);
			System.out.println("nEndPos=="+nEndPos);
			if(nEndPos <= 0){
				return -2;
			}
			httpConnection.setConnectTimeout(30000);
			httpConnection.setReadTimeout(30000);
			RandomAccessFile oSavedFile = new RandomAccessFile(sFilepath,
					"rw");
			String sProperty = "bytes=" + nStartPos + "-";
			// 文件长度，字节数
			long fileLength = oSavedFile.length();
			// 将写文件指针移到文件尾。
			oSavedFile.seek(fileLength);
			httpConnection.setRequestProperty("RANGE", sProperty);
			// System.out.println(sProperty);
			InputStream input = httpConnection.getInputStream();
			byte[] b = new byte[1024 * 8];
			while ((nRead = input.read(b, 0, 1024 * 8)) > 0
					&& nStartPos < nEndPos) {
				oSavedFile.write(b, 0, nRead);
				nStartPos += nRead;
			}
			// input.close();
			httpConnection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			return -3;
		}
		return 0;
	}
}