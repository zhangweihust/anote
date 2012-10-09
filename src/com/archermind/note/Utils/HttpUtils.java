package com.archermind.note.Utils;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.EventTypes;
import com.archermind.note.Services.ServiceManager;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class HttpUtils {

	public static String httphead = "";
	public static String murl = "";

	public static String doPost(Map<String, String> parmas, String url) {
		DefaultHttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT, 30000);
		client.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 30000);
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
			if (httphead != null && !httphead.equals("")) {
				httphead = httphead.replace("\r\n", "");
			}
			String m_cookie = PreferencesHelper.getSharedPreferences(
					NoteApplication.getContext(), 0).getString(
					PreferencesHelper.XML_COOKIES, "");
			if (m_cookie != null && !m_cookie.equals("")) {
				m_cookie = m_cookie.replace("\r\n", "");
			}
			httpPost.setHeader("Cookie", "sid=" + m_cookie);
			HttpResponse response = client.execute(httpPost);

			if (response.getStatusLine().getStatusCode() == 200) {
				if (url.indexOf("login") > 0
						|| url.indexOf("check_bin_acc") > 0 || url.indexOf("register") > 0) {
					Header[] head = null;
					head = response.getHeaders("Set-Cookie");
					for (int i = 0; i < head.length; i++) {
						// if (head == null) {
						// break;
						// }
						httphead = httphead + "header:" + head[i] + "\r\n";
					}
					// System.out.println("xiaopashu-------:"+httphead);
					httphead = java.net.URLDecoder.decode(httphead, "utf8");
					// Pattern p = Pattern.compile("s:32:\"([^\"]+)\"");
					// Matcher m = p.matcher(httphead);
					// if(m.find()){
					// httphead =m.group();
					// httphead =httphead.replace("s:32:", "");
					// httphead =httphead.replace("\"", "");
					// }

					if (httphead.indexOf("sid=") > 0) {
						httphead = httphead
								.substring(httphead.indexOf("sid=") + 4);
						if (httphead.indexOf("header:") > 0) {
							httphead = httphead.substring(0,
									httphead.indexOf("header:"));
						}
						if (httphead != null && !httphead.equals("")) {
							httphead = httphead.replace("\r\n", "");
						}
					} else {
						httphead = "";
					}
					SharedPreferences sp = PreferencesHelper
							.getSharedPreferences(NoteApplication.getContext(),
									0);
					sp.edit()
							.putString(PreferencesHelper.XML_COOKIES, httphead)
							.commit();
					Log.e("HttpUtils", "cookies:" + httphead);
				}
				String strResult = EntityUtils.toString(response.getEntity(),
						HTTP.UTF_8);
				// strResult = strResult.replace("\"", "");
				Log.e("HttpUtils", "strResult:" + strResult);
				return strResult;
			} else {
				for (int i = 0; i < 2; i++) {
					response = client.execute(httpPost);
					if (response.getStatusLine().getStatusCode() == 200) {
						String strResult = EntityUtils.toString(
								response.getEntity(), HTTP.UTF_8);
						Log.e("HttpUtils", "strResult:" + strResult);
						return strResult;
					}
				}
			}
			Log.e("HttpUtils", "statuscode:" + response.getStatusLine().getStatusCode());
			return Integer.toString(response.getStatusLine().getStatusCode());

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return Integer.toString(ServerInterface.ERROR_SERVER_INTERNAL);
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

	public static void SetCookie(String cookie) {
		httphead = cookie;
	}

	public static String GetCookie() {
		return httphead;
	}

	public static boolean saveUrlAs(String Url, String fileName) {
	    //此方法只能用户HTTP协议
	        try {
	        	File file = new File(fileName);
	    		if (file.exists()){
	    			file.delete();
	    		}
	          URL url = new URL(Url);
	          HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	          DataInputStream in = new DataInputStream(connection.getInputStream());
	          DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
	          byte[] buffer = new byte[4096];
	          int count = 0;
	          while ((count = in.read(buffer)) > 0) {
	              out.write(buffer, 0, count);
	            }   
	          out.close();
	          in.close();
	          return true;
	        }catch (Exception e) {
	            return false;
	        }   
	    }   

	
	public static long DownloadFile(String sURL, String sFilepath) {
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
			nStartPos = file.length();// 有一种情况就是程序装之前本地aMusic文件下面有没有下载完的临时文件。
			System.out.println("nStartPos==" + nStartPos);
		}
		try {
			URL url = new URL(sURL);
			HttpURLConnection httpConnection = (HttpURLConnection) url
					.openConnection();
			long nEndPos = getFileSize(sURL);
			System.out.println("nEndPos==" + nEndPos);
			if (nEndPos <= 0) {
				if(saveUrlAs(sURL, sFilepath)){
					return 0;
				}
				return -2;
			}
			httpConnection.setConnectTimeout(30000);
			httpConnection.setReadTimeout(30000);
			RandomAccessFile oSavedFile = new RandomAccessFile(sFilepath, "rw");
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
			if(saveUrlAs(sURL, sFilepath)){
				return 0;
			}
			return -3;
		}
		return 0;
	}
}