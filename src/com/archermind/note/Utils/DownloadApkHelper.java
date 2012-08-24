package com.archermind.note.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Screens.HomeScreen;
import com.archermind.note.Views.CustomDialog;
import com.archermind.note.dialog.DialogCheckSignature;
import com.archermind.note.download.DownloadJob;
import com.archermind.note.download.DownloadTask;
import com.archermind.note.download.DownloadTaskManager;

public class DownloadApkHelper {
	private Activity instance;
	Dialog updateDialog = null;
	public static final int AUTO_UPDATE = 1;
	public static final int MANUAL_UPDATE = 2;
	private Handler handler;
	NotificationManager manager;
	SharedPreferences preferences;
	SimpleDateFormat sDateFormat;
	private static final int NOTIFICATION_DOWNLOADING = 1;
	private static final int NOTIFICATION_DOWNLOADED_SIGNATURES_OK = 2;
	private static final int NOTIFICATION_DOWNLOADED_SIGNATURES_ERROR = 3;
	private static final int TOAST_DOWNLOAD_BACKGROUND = 2;
	public static final String XML_NAME = "ApkUpdate";
	public static final String XML_KEY_TIME = "apk_update_time";
	public static final String XML_KEY_VERSION_CODE = "apk_update_version_code";
	public static final String XML_KEY_TOTAL_SIZE = "apk_update_total_size";
	private final int CurrentVerCode;

	class ApkInfo {
		private int newVerCode;
		private String newVerName;
		private String apkURL;
		private int fileSize;
		private String newVersionInfo;
	}

	public DownloadApkHelper(final Activity instance, Looper looper) {
		CurrentVerCode = VersionUtil.getVerCode(instance);
		this.instance = instance;
		sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		manager = (NotificationManager) instance
				.getSystemService(Context.NOTIFICATION_SERVICE);
		preferences = instance.getSharedPreferences(XML_NAME,
				Context.MODE_WORLD_WRITEABLE);
		handler = new Handler(looper) {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == DownloadTask. MESSAGE_WHAT_DOWNLOADING) {
					return;
				}
				else if (msg.what == DownloadTask.MESSAGE_WHAT_DOWNLOADED_ERROR) {
					Toast
							.makeText(
									instance,
									instance
											.getString(R.string.screen_update_download_error),
									Toast.LENGTH_SHORT).show();
					manager.cancel(NOTIFICATION_DOWNLOADING);
					return;
				}
				Bundle b = msg.getData();
				updateActiveNotification(b.getLong("totalBytes"), b
						.getLong("currentBytes"), b.getString("filePath"));
			}
		};
	}

	public void updateApk(final int type, final Handler screenVersionHandler) {
		if (NoteApplication.getInstance().isDownloadApkFlag()) {// 后台有在线升级正在运行
			toastShow(TOAST_DOWNLOAD_BACKGROUND);
			if (screenVersionHandler != null) {
				Message msg = screenVersionHandler.obtainMessage(1, "flash");
				screenVersionHandler.sendMessage(msg);
			}
			return;
		}
		String saveTime = preferences.getString(XML_KEY_TIME, null);
		if (saveTime != null
				&& sDateFormat.format(new java.util.Date()).equals(saveTime)
				&& type == AUTO_UPDATE) {
			if (screenVersionHandler != null) {
				Message msg = screenVersionHandler.obtainMessage(1, "flash");
				screenVersionHandler.sendMessage(msg);
			}
			return;
		}
		if (checkLocalPackage(type, screenVersionHandler)) {//本地的安装包是符合要求的，提示用户安装
			// ，不需要去检测服务器。
			return;
		}
		// 本地APK文件检测失败，链接服务器请求新版本信息
		final ApkInfo apkInfo = getServerVersionInfo(type);
		if (type == MANUAL_UPDATE) {
			screenVersionHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					Message msg = screenVersionHandler
							.obtainMessage(1, "flash");
					screenVersionHandler.sendMessage(msg);
					if (apkInfo != null) {
						if (apkInfo.newVerCode > CurrentVerCode) {
							// 更新
							doNewVersionUpdate(apkInfo);
						} else {
							String date = sDateFormat
									.format(new java.util.Date());
							preferences.edit().putString(XML_KEY_TIME, date)
									.commit();
							// 当前版本是最新的，那么删除本地的升级目录文件
							delFolder(NoteApplication.packagePath);
							notNewVersionShow();
						}
					}
				}
			}, 1000);
		} else {
			if (apkInfo != null) {
				System.out.println("=CCC= CurrentVerCode " + CurrentVerCode); 
				if (apkInfo.newVerCode > CurrentVerCode) {
					// 更新
					doNewVersionUpdate(apkInfo);
				} else {
					String date = sDateFormat.format(new java.util.Date());
					preferences.edit().putString(XML_KEY_TIME, date).commit();
					// 当前版本是最新的，那么删除本地的升级目录文件
					delFolder(NoteApplication.packagePath);
				}
			}
		}
	}

	private void showNotificationDownloadOK(String saveApkPath) {
		manager.cancel(NOTIFICATION_DOWNLOADING);
		Intent installIntent = new Intent(Intent.ACTION_VIEW);
		installIntent.setDataAndType(Uri.fromFile(new File(saveApkPath)),
				"application/vnd.android.package-archive");
		Notification notification = new Notification(
				android.R.drawable.stat_sys_download_done, instance
						.getString(R.string.screen_update_download_finished),
				System.currentTimeMillis());
		notification.setLatestEventInfo(instance, instance
				.getString(R.string.app_name), instance
				.getString(R.string.screen_update_click_to_install),
				PendingIntent.getActivity(instance, 0, installIntent, 0));
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		manager.notify(NOTIFICATION_DOWNLOADED_SIGNATURES_OK, notification);
	}

	private void showNotificationDownloadError(String saveApkPath) {
		manager.cancel(NOTIFICATION_DOWNLOADING);
		Uri packageURI = Uri.parse("package:" + instance.getPackageName());
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		Notification notification = new Notification(
				android.R.drawable.stat_sys_download_done, instance
						.getString(R.string.screen_update_error), System
						.currentTimeMillis());
		notification
				.setLatestEventInfo(
						instance,
						instance
								.getString(R.string.dialog_check_sinature_not_match_title),
						instance
								.getString(R.string.dialog_check_sinature_not_match_info),
						PendingIntent.getActivity(instance, 0, uninstallIntent,
								0));
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		manager.notify(NOTIFICATION_DOWNLOADED_SIGNATURES_ERROR, notification);
	}

	private void checkSignatures(final String saveApkPath) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String fileSignature = showUninstallAPKSignatures(saveApkPath);
				NoteApplication.LogD(DownloadApkHelper.class,
						"------------------------------------"
								+ instance.getPackageName());
				String onwerSignature = getSign(instance);
				System.out.println("=CCC= fileSignature:"+" onwerSignature:"+onwerSignature);
				if (onwerSignature != null && fileSignature != null) {
					if (onwerSignature.equals(fileSignature)) {
						showNotificationDownloadOK(saveApkPath);
					} else {
						showNotificationDownloadError(saveApkPath);
					}
				} else {
					showNotificationDownloadError(saveApkPath);
				}
			}
		}).start();
	}

	private void updateActiveNotification(long totalBytes, long currentBytes,
			String filePath) {
		if (totalBytes == currentBytes) {
			// showNotification(filePath);
			checkSignatures(filePath);
			return;
		}
		Notification n = new Notification();
		n.icon = android.R.drawable.stat_sys_download;
		n.flags |= Notification.FLAG_ONGOING_EVENT;
		RemoteViews expandedView = new RemoteViews("com.archermind.note",
				R.layout.status_bar_ongoing_event_progress_bar);
		expandedView.setTextViewText(R.id.title, instance
				.getString(R.string.app_name));
		expandedView.setProgressBar(R.id.progress_bar, (int) totalBytes,
				(int) currentBytes, totalBytes == -1);
		expandedView.setTextViewText(R.id.progress_text, getDownloadingText(
				totalBytes, currentBytes));
		expandedView.setImageViewResource(R.id.appIcon,
				android.R.drawable.stat_sys_download);
		n.contentView = expandedView;
		Intent intent = new Intent();
		intent.setClassName(instance, HomeScreen.class.getName());
		n.contentIntent = PendingIntent.getBroadcast(instance, 0, intent, 0);
		manager.notify(NOTIFICATION_DOWNLOADING, n);
	}

	private String getDownloadingText(long totalBytes, long currentBytes) {
		if (totalBytes <= 0) {
			return "";
		}
		long progress = currentBytes * 100 / totalBytes;
		StringBuilder sb = new StringBuilder();
		sb.append(progress);
		sb.append('%');
		return sb.toString();
	}

	private boolean checkLocalPackage(int type,
			final Handler screenVersionHandler) {
		String path = NoteApplication.packagePath;
		File file = new File(path);
		if (!file.exists()) {
			return false;
		}
		if (!file.isDirectory()) {
			return false;
		}
		String[] tempList = file.list();
		if (tempList.length != 1) {
			delFolder(NoteApplication.packagePath);// 保证package目录下面只有一个升级包
			preferences.edit().clear();
			return false;
		}
		final File temp;
		if (path.endsWith(File.separator)) {
			temp = new File(path + tempList[0]);
		} else {
			temp = new File(path + File.separator + tempList[0]);
		}
		if (temp.isFile()) {
			// MediaApplication.logD(DownloadApk.class,
			// "LocalPackageFileName : "
			// + temp.getName());
			int verCode = Integer.parseInt(temp.getName().substring(0,
					temp.getName().lastIndexOf(".")));
			if (CurrentVerCode >= verCode) {// 本地的升级包版本太低
				delFolder(NoteApplication.packagePath);
				preferences.edit().clear();
				return false;
			} else {
				int xmlVerCode = preferences.getInt(XML_KEY_VERSION_CODE, -1);
				int xmlTotalSize = preferences.getInt(XML_KEY_TOTAL_SIZE, -2);
				// MediaApplication.logD(DownloadApk.class,
				// "XML_KEY_VERSION_CODE : "
				// + xmlVerCode + "  XML_KEY_TOTAL_SIZE :" +xmlTotalSize);
				if (xmlVerCode == verCode && xmlTotalSize == temp.length()) {
					// 本地升级包是完整的，提示安装。
					if (type == MANUAL_UPDATE) {
						screenVersionHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								Message msg = screenVersionHandler
										.obtainMessage(1, "flash");
								screenVersionHandler.sendMessage(msg);
								installDownloadedApk(temp);
							}
						}, 1000);
					} else {
						installDownloadedApk(temp);
					}
					return true;
				} else {
					if (temp.length() > xmlTotalSize) {// 本地的升级包文件大小不一致
						delFolder(NoteApplication.packagePath);
						preferences.edit().clear();
						return false;
					}
					// 本地升级文件存在，但是没有下载完成，需要请求断点续传。
					return false;
				}
			}
		} else {// 如果package目录下面的不是一个文件
			delFolder(NoteApplication.packagePath);
			preferences.edit().clear();
			return false;
		}
	}

	private ApkInfo getServerVersionInfo(int type) {
		ApkInfo apkInfo = new ApkInfo();
		try {
			String json = ServerInterface.get_version_info();
			if (json == null) {
				// if (type == MANUAL_UPDATE) {
				// toastShow(TOAST_ERROR_NETWORK);
				// }
				return null;
			}
			//System.out.println("=CCC= apkInfo json:" + json);
			JSONArray array = new JSONArray("["+json+"]");

			if (array.length() > 0) {
				JSONObject obj = array.getJSONObject(0);
				try {
					apkInfo.newVerCode = Integer.parseInt(obj
							.getString("verCode"));
					apkInfo.apkURL = obj.getString("apkURL");
					apkInfo.newVerName = obj.getString("verName");
					apkInfo.fileSize = Integer.parseInt(obj.getString("size"));
					apkInfo.newVersionInfo = obj.getString("info");
					System.out.println("=CCC= newVerCode " + apkInfo.newVerCode);
				} catch (Exception e) {
					return null;
				}
			} else {
				return null;
			}
		} catch (Exception e) {
		}
		return apkInfo;
	}

	private void notNewVersionShow() {
		CustomDialog.Builder customBuilder = new CustomDialog.Builder(instance);
		customBuilder
				.setTitle(instance.getString(R.string.app_name))
				.setWhichViewVisible(CustomDialog.contentIsTextView)
				.setMessage(
						instance
								.getString(R.string.screen_update_not_need_update))
				.setPositiveButton(
						instance.getString(R.string.screen_update_ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
		updateDialog = customBuilder.create();
		updateDialog.show();
	}

	private void installDownloadedApk(final File apkFile) {
		CustomDialog.Builder customBuilder = new CustomDialog.Builder(instance);
		customBuilder
				.setTitle(
						instance.getString(R.string.screen_update_have_update))
				.setWhichViewVisible(CustomDialog.contentIsTextView)
				.setMessage(
						instance
								.getString(R.string.screen_update_select_to_install))
				.setPositiveButton(
						instance.getString(R.string.screen_update_install_now),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								new Thread(new Runnable() {
									private DialogCheckSignature mDialogCheckSignature;

									@Override
									public void run() {
										instance.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												mDialogCheckSignature = new DialogCheckSignature(instance.getParent());
												mDialogCheckSignature.show();
											}
										});
										String fileSignature = showUninstallAPKSignatures(apkFile
												.getAbsolutePath());
										NoteApplication
												.LogD(
														DownloadApkHelper.class,
														"------------------------------------"
																+ instance
																		.getPackageName());
										String onwerSignature = getSign(instance);
										System.out.println("=CCC="+ apkFile.getAbsolutePath());
										if (onwerSignature != null
												&& fileSignature != null) {
											if (onwerSignature
													.equals(fileSignature)) {
												mDialogCheckSignature.dismiss();
												Intent intent = new Intent(
														Intent.ACTION_VIEW);
												intent
														.setDataAndType(
																Uri
																		.fromFile(apkFile),
																"application/vnd.android.package-archive");
												instance.startActivity(intent);
											} else {
												instance
														.runOnUiThread(new Runnable() {
															@Override
															public void run() {
																mDialogCheckSignature
																		.changeText();
															}
														});
											}
										} else {
											instance
													.runOnUiThread(new Runnable() {
														@Override
														public void run() {
															mDialogCheckSignature
																	.changeText();
														}
													});
										}
									}
								}).start();
								dialog.dismiss();
							}
						})
				.setNegativeButton(
						instance
								.getString(R.string.screen_update_install_later),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
		updateDialog = customBuilder.create();
		updateDialog.show();
		String date = sDateFormat.format(new java.util.Date());
		preferences.edit().putString(XML_KEY_TIME, date).commit();
	}

	private String getSign(Context context) {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> apps = pm
				.getInstalledPackages(PackageManager.GET_SIGNATURES);
		Iterator<PackageInfo> iter = apps.iterator();
		while (iter.hasNext()) {
			PackageInfo packageinfo = iter.next();
			String packageName = packageinfo.packageName;
			if (packageName.equals(instance.getPackageName())) {
				NoteApplication.LogD(DownloadApkHelper.class,
						packageinfo.signatures[0].toCharsString());
				return packageinfo.signatures[0].toCharsString();
			}
		}
		return null;
	}

	private String showUninstallAPKSignatures(String apkPath) {
		String PATH_PackageParser = "android.content.pm.PackageParser";
		try {
			// apk包的文件路径
			// 这是一个Package 解释器, 是隐藏的
			// 构造函数的参数只有一个, apk文件的路径
			// PackageParser packageParser = new PackageParser(apkPath);
			Class pkgParserCls = Class.forName(PATH_PackageParser);
			Class[] typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);
			Object[] valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			Object pkgParser = pkgParserCt.newInstance(valueArgs);
			NoteApplication.LogD(DownloadApkHelper.class, "pkgParser:"
					+ pkgParser.toString());
			// 这个是与显示有关的, 里面涉及到一些像素显示等等, 我们使用默认的情况
			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();
			// PackageParser.Package mPkgInfo = packageParser.parsePackage(new
			// File(apkPath), apkPath,
			// metrics, 0);
			typeArgs = new Class[4];
			typeArgs[0] = File.class;
			typeArgs[1] = String.class;
			typeArgs[2] = DisplayMetrics.class;
			typeArgs[3] = Integer.TYPE;
			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod(
					"parsePackage", typeArgs);
			valueArgs = new Object[4];
			valueArgs[0] = new File(apkPath);
			valueArgs[1] = apkPath;
			valueArgs[2] = metrics;
			valueArgs[3] = PackageManager.GET_SIGNATURES;
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser,
					valueArgs);

			typeArgs = new Class[2];
			typeArgs[0] = pkgParserPkg.getClass();
			typeArgs[1] = Integer.TYPE;
			Method pkgParser_collectCertificatesMtd = pkgParserCls
					.getDeclaredMethod("collectCertificates", typeArgs);
			valueArgs = new Object[2];
			valueArgs[0] = pkgParserPkg;
			valueArgs[1] = PackageManager.GET_SIGNATURES;
			pkgParser_collectCertificatesMtd.invoke(pkgParser, valueArgs);
			// 应用程序信息包, 这个公开的, 不过有些函数, 变量没公开
			Field packageInfoFld = pkgParserPkg.getClass().getDeclaredField(
					"mSignatures");
			Signature[] info = (Signature[]) packageInfoFld.get(pkgParserPkg);
			NoteApplication.LogD(DownloadApkHelper.class, "size:" + info.length);
			NoteApplication.LogD(DownloadApkHelper.class, info[0].toCharsString());
			return info[0].toCharsString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void doNewVersionUpdate(final ApkInfo apkInfo) {
		BigDecimal b = new BigDecimal((float) apkInfo.fileSize / 1048576);
		float fileSize = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
		StringBuffer sb = new StringBuffer();
		sb.append(instance.getString(R.string.screen_update_version));
		sb.append(apkInfo.newVerName + "\n");
		sb.append(instance.getString(R.string.screen_update_version_size));
		sb.append(fileSize + "M\n");
		sb.append(instance.getString(R.string.screen_update_version_info)
				+ "\n");
		sb.append(apkInfo.newVersionInfo);
		CustomDialog.Builder customBuilder = new CustomDialog.Builder(instance);
		customBuilder
				.setTitle(
						instance.getString(R.string.screen_update_have_update))
				.setWhichViewVisible(CustomDialog.contentIsTextView)
				.setMessage(ToDBC(sb.toString()))
				.setPositiveButton(
						instance.getString(R.string.screen_update_install_now),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								new Thread() {
									@Override
									public void run() {
										String filePath = NoteApplication.packagePath
												+ apkInfo.newVerCode
												+ DownloadTask.PACKAGE_SUFFIX;
										File file = new File(filePath);
										int totalSize = getFileSizeFromServer(apkInfo.apkURL);
										//MediaApplication.logD(DownloadApk.class
										// , "apkInfo.fileSize :" +
										// apkInfo.fileSize + "  serverSize : "
										// + totalSize);
										if (totalSize == -1) {
											// toastShow(TOAST_ERROR_NETWORK);
											return;
										}
										if (file.exists()) {// 本地存在升级文件
											int xmlTotalSize = preferences
													.getInt(XML_KEY_TOTAL_SIZE,
															-2);
											if (xmlTotalSize != totalSize) {// 本地XML存储的文件大小和服务器返回的不一致
												delFolder(NoteApplication.packagePath);
												preferences.edit().clear();
												// if(!downloadApk(0, totalSize,
												// apkInfo.apkURL,
												// apkInfo.newVerCode)){
												// delFolder(mediaService.
												// getPackagePath());
												// preferences.edit().clear();
												//toastShow(TOAST_ERROR_NETWORK)
												// ;
												// };
												downloadApk(apkInfo.apkURL, 0,
														filePath,
														apkInfo.newVerCode);
											}
											if (totalSize == file.length()) {// 文件下载完成
												Intent intent = new Intent(
														Intent.ACTION_VIEW);
												intent
														.setDataAndType(
																Uri
																		.fromFile(file),
																"application/vnd.android.package-archive");
												instance.startActivity(intent);
											} else {// 文件没有下载完成,开始断点续传了
												toastShow(TOAST_DOWNLOAD_BACKGROUND);
												//if(!downloadApk(file.length(),
												// totalSize, apkInfo.apkURL,
												// apkInfo.newVerCode)){
												// delFolder(mediaService.
												// getPackagePath());
												// preferences.edit().clear();
												//toastShow(TOAST_ERROR_NETWORK)
												// ;
												// };
												downloadApk(apkInfo.apkURL,
														file.length(),
														filePath,
														apkInfo.newVerCode);

											}
										} else {// 本地升级文件不存在
											toastShow(TOAST_DOWNLOAD_BACKGROUND);
											delFolder(NoteApplication.packagePath);
											preferences.edit().clear();
											// if(!downloadApk(0, totalSize,
											// apkInfo.apkURL,
											// apkInfo.newVerCode)){
											// delFolder(mediaService.
											// getPackagePath());
											// preferences.edit().clear();
											// toastShow(TOAST_ERROR_NETWORK);
											// };
											downloadApk(apkInfo.apkURL, 0,
													filePath,
													apkInfo.newVerCode);
										}
									};
								}.start();
								dialog.dismiss();
							}
						})
				.setNegativeButton(
						instance
								.getString(R.string.screen_update_install_later),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
		updateDialog = customBuilder.create();
		updateDialog.show();
		String date = sDateFormat.format(new java.util.Date());
		preferences.edit().putString(XML_KEY_TIME, date).commit();
	}

	/**
	 * 删除文件夹
	 * 
	 * @param filePathAndName
	 *            String 文件夹路径及名称 如c:/fqf
	 * @param fileContent
	 *            String
	 * @return boolean
	 */
	public void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete(); // 删除空文件夹

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	/**
	 * 删除文件夹里面的所有文件
	 * 
	 * @param path
	 *            String 文件夹路径 如 c:/fqf
	 */
	public void delAllFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);// 再删除空文件夹
			}
		}
	}

	/**
	 * # * 半角转换为全角 # * # * @param input # * @return #
	 */
	public static String ToDBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375)
				c[i] = (char) (c[i] - 65248);
		}
		return new String(c);
	}

	private int getFileSizeFromServer(String apkUrl) {
		URL url;
		int totalSize = -1;
		try {
			url = new URL(apkUrl);
			HttpURLConnection httpConnection = (HttpURLConnection) url
					.openConnection();
			httpConnection
					.setRequestProperty(
							"User-Agent",
							"Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
			totalSize = httpConnection.getContentLength();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return totalSize;
	}

	private void toastShow(int type) {
		if (type == TOAST_DOWNLOAD_BACKGROUND) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(NoteApplication.getContext(),
							R.string.screen_version_download_update_background,
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	private void downloadApk(String url, long startPos, String path,
			int versionCode) {
		final DownloadJob mJob = new DownloadJob();
		mJob.setDownloadUrl(url);
		mJob.setDownloadStartPos(startPos);
		mJob.setPath(path);
		mJob.setVersionCode(versionCode);
		mJob.setDownloadType(DownloadTask.DOWNLOAD_START_ON_APK);
		final DownloadJob.OnDownloadStateChangeListener listener = new DownloadJob.OnDownloadStateChangeListener() {

			@Override
			public void OnDownloadStateChange(DownloadJob job, int oldState,
					int newState) {
				// TODO Auto-generated method stub
				Message msg = new Message();
				switch (newState) {
				case DownloadTask.STATE_BEGIN:
					msg.what = DownloadTask.MESSAGE_WHAT_DOWNLOADING;
					NoteApplication.getInstance().setDownloadApkFlag(true);
					preferences = NoteApplication.getContext()
							.getSharedPreferences(XML_NAME,
									Context.MODE_WORLD_WRITEABLE);
					preferences.edit().putInt(DownloadApkHelper.XML_KEY_TOTAL_SIZE,
							(int) mJob.getDownloadTotalSize()).commit();
					preferences.edit().putInt(DownloadApkHelper.XML_KEY_VERSION_CODE,
							mJob.getVersionCode()).commit();
					break;

				case DownloadTask.STATE_FINISHED:
					NoteApplication.getInstance().setDownloadApkFlag(false);
					msg.what = DownloadTask.MESSAGE_WHAT_DOWNLOADED;
					Bundle data = new Bundle();
					data.putLong("totalBytes", job.getDownloadTotalSize());
					data.putLong("currentBytes", job.getDownloadCurrentSize());
					data.putString("filePath", job.getPath());
					System.out.println("=CCC= + filePath:"+job.getPath());
					if (!new File(job.getPath()).exists()) {
						try{
						throw new FileNotFoundException();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
					msg.setData(data);
					job.removeOnDownloadStateChangeListener(this);
					break;

				case DownloadTask.STATE_ERROR:
				case DownloadTask.STATE_PAUSE:
				default:
					msg.what = DownloadTask.MESSAGE_WHAT_DOWNLOADED_ERROR;
					job.removeOnDownloadStateChangeListener(this);
					break;
				}
				handler.sendMessage(msg);
			}
		};
		mJob.addOnDownloadStateChangeListener(listener);
		if (DownloadTaskManager.getInstance().getContext() == null) {
			DownloadTaskManager.getInstance().setContext(NoteApplication.getContext());
		}
		DownloadTaskManager.getInstance().addNewTask(mJob);
	}
}
