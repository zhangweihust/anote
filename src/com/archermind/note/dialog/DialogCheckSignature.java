package com.archermind.note.dialog;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

public class DialogCheckSignature {
	private Dialog DialogCheckSignature;
	private AlertDialog.Builder dialogBuild;
	private ProgressDialog pDialog;
    
	public DialogCheckSignature(Context context) {
		
		pDialog = new ProgressDialog(context);  
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.setTitle(context.getString(R.string.dialog_check_signature_title));
		pDialog.setMessage(context.getString(R.string.dialog_check_signature_checking)); 
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(true);
		
		dialogBuild = new AlertDialog.Builder(context);
		dialogBuild.setTitle(R.string.dialog_check_signature_title)
		.setMessage(R.string.dialog_check_sinature_not_match)
		.setPositiveButton(R.string.dialog_check_signature_ok,btn_ok_listener)
		.setNegativeButton(R.string.dialog_check_signature_cancel, btn_cancel_listener);
		DialogCheckSignature = dialogBuild.create();
	}
	

	public void show() {
		pDialog.show();
	}

	public void dismiss() {
		pDialog.dismiss();
	}
	
	public void changeDialog(){
		DialogCheckSignature.show();
		pDialog.dismiss();
	}

	private DialogInterface.OnClickListener btn_ok_listener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			DialogCheckSignature.dismiss();
			Uri packageURI = Uri.parse("package:" + NoteApplication.getContext().getPackageName());           
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI); 
			uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
			NoteApplication.getContext().startActivity(uninstallIntent);
		}
	};
   
	
	private DialogInterface.OnClickListener btn_cancel_listener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			DialogCheckSignature.dismiss();
		}
	};
	

}
