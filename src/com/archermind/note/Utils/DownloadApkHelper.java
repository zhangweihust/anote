package com.archermind.note.Utils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Screens.AboutScreen;
import com.archermind.note.Screens.HomeScreen;
import com.archermind.note.dialog.DialogCheckSignature;
import com.archermind.upgrade.DownloadManager;
import com.archermind.upgrade.MessageTypes;
import com.archermind.upgrade.Update;
import com.archermind.upgrade.UpgradeManager;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadApkHelper {

	private static final int NOTIFICATION_DOWNLOADING = 1;
	private static final int NOTIFICATION_DOWNLOADED_SIGNATURES_OK = 2;
	private static final int NOTIFICATION_DOWNLOADED_SIGNATURES_ERROR = 3;
	
	private Context mContext;
	private Dialog updateDialog = null;
	private NotificationManager manager;
	private String downloadApkName = "";
	
	private  String URL = "http://note.archermind.com/ci/index.php/AppUpdate/getUpdateInfo";

	public DownloadApkHelper(Context context)
	{
		mContext = context;
		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	private Handler mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MessageTypes.DOWN_DATA_CHANGED:// 下载进度的更新
					updateActiveNotification((Integer)msg.obj);
					break;
				case MessageTypes.FILE_ALREADY_DOWNLOADED:// 文件已经下载完毕
				case MessageTypes.DOWN_SUCCESS://  下载成功
					updateActiveNotification(100);
					if (!TextUtils.isEmpty(downloadApkName))
					{
						if (checkSignatures(downloadApkName))
						{
							File apkfile = new File(downloadApkName);
							if (apkfile.exists())
							{
								installDownloadedApk(apkfile);
							}
						}
					}
					break;
				case MessageTypes.DOWN_FAIL://  下载失败
					NoteApplication.toastShow(mHandler, R.string.screen_update_download_failed);
					break;
				case MessageTypes.NO_NEED_TO_UPGRADE://  不需要更新
					if (mContext.getClass().getName().equals("com.archermind.note.Screens.AboutScreen"))
					{
						((AboutScreen) mContext).dismissProgress();
					}
					NoteApplication.toastShow(mHandler, R.string.screen_update_not_need_update);
					break;
				case MessageTypes.NEED_TO_UPGRADE://  需要更新
					if (mContext.getClass().getName().equals("com.archermind.note.Screens.AboutScreen"))
					{
						((AboutScreen) mContext).dismissProgress();
					}
					Update update = (Update)msg.obj;
					doNewVersionUpdate(update);
					break;
				case MessageTypes.ERROR://  有异常
					if (mContext.getClass().getName().equals("com.archermind.note.Screens.AboutScreen"))
					{
						((AboutScreen) mContext).dismissProgress();
					}
					NoteApplication.toastShow(mHandler, R.string.screen_update_exception);
					NoteApplication.LogD(DownloadApkHelper.class,
							"异常种类 : " + msg.obj);
					switch ((Integer) msg.obj) {
					case MessageTypes.ERROR_NO_SDCARD://  没有SD卡
						break;
					case MessageTypes.ERROR_IO_ERROR://  IO异常
						break;
					case MessageTypes.ERROR_PARSE_JSON_ERROR://  解析JSON异常
						break;
					case MessageTypes.ERROR_HTTP_DATA_ERROR://  网络数据交互异常
						break;
					case MessageTypes.ERROR_FILE_ERROR://  文件操作异常
						break;
					}
					break;
				}
			};
		};
	
		public void checkUpdate()
		{
			UpgradeManager.getInstance().checkAppUpdate(mContext, URL,
				      UpgradeManager.A_NOTE, mHandler);
		}
		
		private void doNewVersionUpdate(final Update update) {
			BigDecimal b = new BigDecimal((float) update.getVersionSize() / 1048576);
			float fileSize = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			StringBuffer sb = new StringBuffer();
			sb.append(mContext.getString(R.string.screen_update_version));
			sb.append(update.getVersionName() + "\n");
			sb.append(mContext.getString(R.string.screen_update_version_size));
			sb.append(fileSize + "M\n");
			sb.append(mContext.getString(R.string.screen_update_version_info)
					+ "\n");
			sb.append(update.getVersionInfo());
			
			updateDialog = new Dialog(mContext,R.style.CornerDialog);
			updateDialog.setContentView(R.layout.dialog_ok_cancel);
			TextView titleView = (TextView) updateDialog.findViewById(R.id.dialog_title);
			titleView.setText(R.string.screen_update_have_update);
			TextView msgView = (TextView) updateDialog.findViewById(R.id.dialog_message);
			msgView.setText(ToDBC(sb.toString()));
			Button btn_ok = (Button) updateDialog.findViewById(R.id.dialog_btn_ok);
			btn_ok.setText(R.string.screen_update_install_now);
			btn_ok.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					updateDialog.dismiss();
					DownloadManager.getInstance().downloadApk(update, true, NoteApplication.packagePath, mHandler);
					downloadApkName = NoteApplication.packagePath + update.getVersionName() + ".apk";
				}
			});
			Button btn_cancel = (Button) updateDialog
					.findViewById(R.id.dialog_btn_cancel);
			btn_cancel.setText(R.string.screen_update_install_later);
			btn_cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					updateDialog.dismiss();
				}
			});
			updateDialog.show();
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
		
		private void updateActiveNotification(Integer progress) {
			Notification n = new Notification();
			n.icon = android.R.drawable.stat_sys_download;
			n.flags |= Notification.FLAG_ONGOING_EVENT;
			RemoteViews expandedView = new RemoteViews("com.archermind.note",
					R.layout.status_bar_ongoing_event_progress_bar);
			expandedView.setTextViewText(R.id.title, mContext
					.getString(R.string.app_name));
			expandedView.setProgressBar(R.id.progress_bar, 100,
					progress, false);
			expandedView.setTextViewText(R.id.progress_text, progress + " %");
			expandedView.setImageViewResource(R.id.appIcon,
					android.R.drawable.stat_sys_download);
			n.contentView = expandedView;
			Intent intent = new Intent();
			intent.setClassName(mContext, HomeScreen.class.getName());
			n.contentIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
			manager.notify(NOTIFICATION_DOWNLOADING, n);
		}
		
		private void showNotificationDownloadOK(String saveApkPath) {
			manager.cancel(NOTIFICATION_DOWNLOADING);
			Intent installIntent = new Intent(Intent.ACTION_VIEW);
			installIntent.setDataAndType(Uri.fromFile(new File(saveApkPath)),
					"application/vnd.android.package-archive");
			Notification notification = new Notification(
					android.R.drawable.stat_sys_download_done, mContext
							.getString(R.string.screen_update_download_finished),
					System.currentTimeMillis());
			notification.setLatestEventInfo(mContext, mContext
					.getString(R.string.app_name), mContext
					.getString(R.string.screen_update_click_to_install),
					PendingIntent.getActivity(mContext, 0, installIntent, 0));
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			manager.notify(NOTIFICATION_DOWNLOADED_SIGNATURES_OK, notification);
		}

		private void showNotificationDownloadError(String saveApkPath) {
			manager.cancel(NOTIFICATION_DOWNLOADING);
			Uri packageURI = Uri.parse("package:" + mContext.getPackageName());
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
			Notification notification = new Notification(
					android.R.drawable.stat_sys_download_done, mContext
							.getString(R.string.screen_update_error), System
							.currentTimeMillis());
			notification
					.setLatestEventInfo(
							mContext,
							mContext
									.getString(R.string.dialog_check_sinature_not_match),
									mContext
									.getString(R.string.dialog_check_sinature_not_match_info),
							PendingIntent.getActivity(mContext, 0, uninstallIntent,
									0));
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			manager.notify(NOTIFICATION_DOWNLOADED_SIGNATURES_ERROR, notification);
		}

		private boolean checkSignatures(final String saveApkPath) 
		{
			boolean ret = false;
			File file = new File(saveApkPath);
			String fileSignature = showUninstallAPKSignatures(saveApkPath);
			NoteApplication.LogD(DownloadApkHelper.class,
					"------------------------------------"
							+ mContext.getPackageName());
			String onwerSignature = getSign(mContext);
			System.out.println("=CCC= fileSignature:"+fileSignature + " onwerSignature:"+onwerSignature);
			if (onwerSignature != null && fileSignature != null) {
				if (onwerSignature.equals(fileSignature)) {
					showNotificationDownloadOK(saveApkPath);
					ret = true;
				} else {
					showNotificationDownloadError(saveApkPath);
				}
			} else {
				showNotificationDownloadError(saveApkPath);
			}
			
			return ret;
	}
		
		private String getSign(Context context) {
			PackageManager pm = context.getPackageManager();
			List<PackageInfo> apps = pm
					.getInstalledPackages(PackageManager.GET_SIGNATURES);
			Iterator<PackageInfo> iter = apps.iterator();
			while (iter.hasNext()) {
				PackageInfo packageinfo = iter.next();
				String packageName = packageinfo.packageName;
				if (packageName.equals(mContext.getPackageName())) {
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
		
		
		private void installDownloadedApk(final File apkFile) {
			
			updateDialog = new Dialog(mContext,R.style.CornerDialog);
			updateDialog.setContentView(R.layout.dialog_ok_cancel);
			TextView titleView = (TextView) updateDialog.findViewById(R.id.dialog_title);
			titleView.setText(R.string.screen_update_have_update);
			TextView msgView = (TextView) updateDialog.findViewById(R.id.dialog_message);
			msgView.setText(R.string.screen_update_select_to_install);
			Button btn_ok = (Button) updateDialog.findViewById(R.id.dialog_btn_ok);
			btn_ok.setText(R.string.screen_update_install);
			btn_ok.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new Thread(new Runnable() {
						private DialogCheckSignature mDialogCheckSignature;

						@Override
						public void run() {
							((Activity) mContext).runOnUiThread(new Runnable() {
								@Override
								public void run() {
									mDialogCheckSignature = new DialogCheckSignature(mContext);
									mDialogCheckSignature.show();
								}
							});
							String fileSignature = showUninstallAPKSignatures(apkFile
									.getAbsolutePath());
							NoteApplication
									.LogD(
											DownloadApkHelper.class,
											"------------------------------------"
													+ mContext
															.getPackageName());
							String onwerSignature = getSign(mContext);
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
									mContext.startActivity(intent);
								} else {
									((Activity) mContext)
											.runOnUiThread(new Runnable() {
												@Override
												public void run() {
													mDialogCheckSignature
															.changeDialog();
												}
											});
								}
							} else {
								((Activity) mContext)
										.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												mDialogCheckSignature
														.changeDialog();
											}
										});
							}
						}
					}).start();
					updateDialog.dismiss();
				}
			});
			Button btn_cancel = (Button) updateDialog
					.findViewById(R.id.dialog_btn_cancel);
			btn_cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					updateDialog.dismiss();
				}
			});
			updateDialog.show();
		}
}
