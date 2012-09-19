package com.archermind.note.dialog;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DialogCheckSignature {
	private Dialog dialogCheckSignature;
	private AlertDialog.Builder dialogBuild;
	private Dialog pDialog;

	public DialogCheckSignature(Context context) {

		// pDialog = new ProgressDialog(context);
		// pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// pDialog.setTitle(context.getString(R.string.dialog_check_signature_title));
		// pDialog.setMessage(context.getString(R.string.dialog_check_signature_checking));
		// pDialog.setIndeterminate(false);
		// pDialog.setCancelable(true);
		//
		// dialogBuild = new AlertDialog.Builder(context);
		// dialogBuild.setTitle(R.string.dialog_check_signature_title)
		// .setMessage(R.string.dialog_check_sinature_not_match)
		// .setPositiveButton(R.string.dialog_check_signature_ok,btn_ok_listener)
		// .setNegativeButton(R.string.dialog_check_signature_cancel,
		// btn_cancel_listener);
		// DialogCheckSignature = dialogBuild.create();
		pDialog = new Dialog(context, R.style.CustomDialog);
		pDialog.setContentView(R.layout.dialog_progress);
		TextView textView = (TextView) pDialog.findViewById(R.id.progress_msg);
		textView.setText(R.string.dialog_check_signature_checking);
		if (android.os.Build.VERSION.SDK_INT > 8) {
			Typeface type = Typeface.createFromAsset(context.getAssets(),
					"xdxwzt.ttf");
			textView.setTypeface(type);
		}

		dialogCheckSignature = new Dialog(context, R.style.CornerDialog);
		dialogCheckSignature.setContentView(R.layout.dialog_ok_cancel);
		TextView titleView = (TextView) dialogCheckSignature
				.findViewById(R.id.dialog_title);
		titleView.setText(R.string.dialog_title_tips);
		TextView msgView = (TextView) dialogCheckSignature
				.findViewById(R.id.dialog_message);
		msgView.setText(R.string.dialog_check_sinature_not_match);
		Button btn_ok = (Button) dialogCheckSignature
				.findViewById(R.id.dialog_btn_ok);
		btn_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialogCheckSignature.dismiss();
				Uri packageURI = Uri.parse("package:"
						+ NoteApplication.getContext().getPackageName());
				Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
						packageURI);
				uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				NoteApplication.getContext().startActivity(uninstallIntent);
			}
		});
		Button btn_cancel = (Button) dialogCheckSignature
				.findViewById(R.id.dialog_btn_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialogCheckSignature.dismiss();
			}
		});
	}

	public void show() {
		pDialog.show();
	}

	public void dismiss() {
		pDialog.dismiss();
	}

	public void changeDialog() {
		dialogCheckSignature.show();
		pDialog.dismiss();
	}

	private DialogInterface.OnClickListener btn_ok_listener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			dialogCheckSignature.dismiss();
			Uri packageURI = Uri.parse("package:"
					+ NoteApplication.getContext().getPackageName());
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
					packageURI);
			uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			NoteApplication.getContext().startActivity(uninstallIntent);
		}
	};

	private DialogInterface.OnClickListener btn_cancel_listener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			dialogCheckSignature.dismiss();
		}
	};

}
