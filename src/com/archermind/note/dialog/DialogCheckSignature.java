package com.archermind.note.dialog;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


public class DialogCheckSignature {
	private Button btn_ok;
	private Button btn_cancel;
	private ProgressBar pb_checking;
	private TextView tv_info;
	private Dialog DialogCheckSignature;
	private LinearLayout ll_btn;
	private TextView tv_line;
	private TextView tv_title;
    
	public DialogCheckSignature(Context context) {
		DialogCheckSignature = new Dialog(context, R.style.CustomDialog);
		DialogCheckSignature.setContentView(R.layout.dialog_check_signature);
		init();
	}
	
	public DialogCheckSignature(String title, String info) {
		DialogCheckSignature = new Dialog(NoteApplication.getContext(), R.style.CustomDialog);
		DialogCheckSignature.setContentView(R.layout.dialog_check_signature);
		init();
		tv_info.setText(info);
		tv_title.setText(title);
	}

	private void init() {
		btn_ok = (Button) DialogCheckSignature.findViewById(R.id.btn_reinstall_ok);
		btn_cancel = (Button) DialogCheckSignature.findViewById(R.id.btn_reinstall_cancel);
		btn_ok.setOnClickListener(btn_ok_listener);
		btn_cancel.setOnClickListener(btn_cancel_listener);
		pb_checking = (ProgressBar) DialogCheckSignature.findViewById(R.id.pb_checking);
		tv_info = (TextView) DialogCheckSignature.findViewById(R.id.tv_content);
		ll_btn = (LinearLayout) DialogCheckSignature.findViewById(R.id.ll_btn);
		tv_line = (TextView) DialogCheckSignature.findViewById(R.id.tv_line);
		tv_title = (TextView) DialogCheckSignature.findViewById(R.id.tv_title);
	}

	public void show() {
		DialogCheckSignature.show();
	}

	public void dismiss() {
		DialogCheckSignature.dismiss();
	}
	
	public void changeText(){
		tv_info.setText(R.string.dialog_check_sinature_not_match);
		ll_btn.setVisibility(View.VISIBLE);
		pb_checking.setVisibility(View.GONE);
		tv_line.setVisibility(View.VISIBLE);
	}

	private View.OnClickListener btn_ok_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			DialogCheckSignature.dismiss();
			Uri packageURI = Uri.parse("package:" + NoteApplication.getContext().getPackageName());           
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI); 
			uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
			NoteApplication.getContext().startActivity(uninstallIntent);   
	 }
	};
   
	
	private View.OnClickListener btn_cancel_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			dismiss();
		}
	};
	

}
