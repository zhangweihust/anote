package com.archermind.note.Screens;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Adapter.FaceAdapter;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.EventTypes;
import com.archermind.note.Events.IEventHandler;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.DensityUtil;
import com.archermind.note.Utils.GenerateName;
import com.archermind.note.Utils.ServerInterface;
import com.archermind.note.Utils.SetSystemProperty;
import com.archermind.note.Views.ColorFullRectView;
import com.archermind.note.editnote.ColorPickerDialog;
import com.archermind.note.editnote.ColorPickerDialog.OnColorChangedListener;
import com.archermind.note.editnote.MyEditText;
import com.archermind.note.editnote.NoteSaveDialog;
import com.archermind.note.gesture.AmGesture;
import com.archermind.note.gesture.AmGestureLibraries;
import com.archermind.note.gesture.AmGestureLibrary;
import com.archermind.note.gesture.AmGestureOverlayView;

public class EditNoteScreen extends Screen implements OnClickListener,
		IEventHandler {

	private AmGestureOverlayView gestureview = null;
	private MyEditText myEdit = null;

	// 最底下一排的四个按钮
	private ImageButton edit_insert = null; // 插入方式（图像，表情等）
	private ImageButton edit_delete = null; // 删除按钮
	private ImageButton edit_input_type = null; // 输入模式（手写，涂鸦，阅读）
	private ImageButton edit_setting = null; // 设置按钮（粗细，颜色）
	private LinearLayout mEditLayout; // 包括以上4个按钮的编辑框部分
	private ScrollView mScrollView = null;

	private ImageView mWeatherImg = null; // 天气按钮

	private FaceAdapter faceAdapter = null; // 表情adapter

	private SeekBar thickness_seekbar = null; // 粗细seekbar

	private ColorFullRectView mColorFullRectView = null; // 手写时的边框

	private AmGesture mGesture; // 当前一个的手势结构
	private static final float LENGTH_THRESHOLD = 10.0f; // 一个手势滑动的最小临界值

	private LinearLayout bitmap_rect_linearlayout;// 日记保存图像区域

	private boolean isgraffit_erase = false;// 标志删除按钮当前是否是在涂鸦模式的橡皮擦功能。

	private Dialog thickness_dialog = null;// 手势粗细对话框
	private Dialog picchoose_dialog = null;// 图片选择对话框
	// private Dialog fontchoose_dialog = null;
	private Dialog facechoose_dialog = null;// 表情选择对话框
	private PopupWindow mWeatherPopupWindow = null;// 天气选择popwindow
	private PopupWindow mEditTypePopupWindow = null;// 输入模式(手写，涂鸦，阅读）popwindow
	private PopupWindow mInsertTypePopupWindow = null;// 插入类型（图片，表情等）popwindow
	private PopupWindow mSetTypePopupWindow = null;// 设置popwindow

	private NoteSaveDialog save_dialog = null;// 保存对话框

	// 六种状态，不过目前用到的只有GRAFFITINSERTSTATE,HANDWRITINGSTATE,READNOTESTATE三种
	public static final int PICINSERTSTATE = 1;
	public static final int FACEINSERTSTATE = 2;
	public static final int SOFTINPUTSTATE = 3;
	public static final int GRAFFITINSERTSTATE = 4;
	public static final int HANDWRITINGSTATE = 5;
	public static final int READNOTESTATE = 6;

	private static final int BOTTOMOFFSET = 6;

	private final int CAMERA_RESULT = 8888;
	private final int ALBUM_RESULT = 9999;

	private String imageFilePath = null;// 当前插入图片的路径

	public static int mState = HANDWRITINGSTATE;// 当前的状态值

	private GesturesProcessorHandWrite handWriteListener;// 手势监听器

	private final int MIN_STROKEWIDTH = 12;// 手势最小宽度
	private final int MAX_STROKEWIDTH = 30;// 手势最大宽度
	private float mStrokeWidth = MIN_STROKEWIDTH;// 当前手势宽度
	private int mFingerColor = 0xffff0000;// 当前手势颜色

	private LinearLayout mWeatherLayout = null;// 天气图标和天气文本的一个LinearLayout

	private TextView weather_tv = null;// 天气文本信息

	private Handler handler = new Handler();// 主线程handler

	private GenerateName gestureName = new GenerateName();// 产生手势的名字，后面的数字是递增的
	private GenerateName picName = new GenerateName();// 产生图片的名字，后面数字是递增的。

	private File gestureFile = null;// 手势文件

	private AmGestureLibrary mStore = null;// 手势存储管理

	private LinkedHashMap<String, String> mPicMap = null;// 保存图片名字-路径的map

	private ArrayList<String> mStrList = null;// 保存edittext中内容的一个List。
	private boolean isNeedSaveChange = true;// 标志EditText是佛需要改变
	private int mCurPage = 0;// 当前的页数
	private int mLastPageEnd = 0;// 上一页结束位置在mStrList的索引Index
	private int mTotalPage = 0;// 总页数
	private boolean isInsert = false;// 标志是否是插入状态，如：在翻页时有清除操作，但此时不应是插入状态

	private String mDiaryPath = "";// 笔记保存地址

	private String mWeather = "sunny";// 天气信息

	private boolean hasChanged = false;// 笔记是否被改变

	public ArrayList<String> mPicPathList = null;// 保存笔记转换为图片的图片地址List

	private ViewFlipper flipper = null;// 翻页时动画信息

	private byte[] thread_lock = new byte[0];// 笔记转换为图片时的线程锁

	private int mCurSavePage = 0;// 笔记转换为图片时，当前已经保存了页数

	private boolean isPicSaveOver = false; // 图片是否已经转换完成

	private boolean isNoteTobeShare = false; // 是否到了可以分享的时候

	private boolean isNoteSaveOver = false; // 笔记是否已经保存完成

	//private boolean isRemoveEdit = false; // EditText是否在布局中被移除

	private String mShareNoteId = ""; // 笔记数据库id
	private String mShareNoteTitle = ""; // 笔记名称
	private String mShareNoteAction = ""; // 分享笔记操作('A','M')
	private String mShareNoteSid = ""; // 分享笔记的网络id

	private String preffix = NoteApplication.savePath + "diary/"; // 路径前缀

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.edit_note);

		// 手势视图，并添加手势监听器
		gestureview = (AmGestureOverlayView) findViewById(R.id.gestureview);
		if (handWriteListener == null) {
			handWriteListener = new GesturesProcessorHandWrite();
			gestureview.removeAllOnGestureListeners();
			gestureview.addOnGestureListener(handWriteListener);
		}

		// 手写模式时的边框
		mColorFullRectView = (ColorFullRectView) findViewById(R.id.colorfull_rect);
		// EditText
		myEdit = (MyEditText) findViewById(R.id.editText_view);
		mScrollView = (ScrollView)findViewById(R.id.sv_outside_editview);

		// 最底下一排的四个按钮
		edit_insert = (ImageButton) findViewById(R.id.edit_insert);
		edit_delete = (ImageButton) findViewById(R.id.edit_delete);
		edit_input_type = (ImageButton) findViewById(R.id.edit_inputtype);
		edit_setting = (ImageButton) findViewById(R.id.edit_setting);
		mEditLayout = (LinearLayout) findViewById(R.id.bottom);

		edit_insert.setOnClickListener(this);
		edit_delete.setOnClickListener(this);
		edit_input_type.setOnClickListener(this);
		edit_setting.setOnClickListener(this);

		// 天气图标
		weather_tv = (TextView) findViewById(R.id.edit_weather_textview);
		mWeatherLayout = (LinearLayout) findViewById(R.id.edit_weather_linearlayout);
		mWeatherLayout.setOnClickListener(this);
		mWeatherImg = (ImageView) findViewById(R.id.edit_weather_image);
		initPopupWindow();

		mStrList = new ArrayList<String>();
		mPicPathList = new ArrayList<String>();

		// 返回按钮
		Button backButton = (Button) findViewById(R.id.screen_top_play_control_back);
		backButton.setOnClickListener(this);

		// 删除本地已经存在了的加压文件
		deleteDefaultFiles();
		String notePath = getIntent().getStringExtra("notePath");
		if (notePath != null) {// 旧笔记编辑，则重新加载
			mDiaryPath = notePath;
			reload(mDiaryPath + ".note");
		} else {
			notePath = getIntent().getStringExtra("filePath");// 网络下载的笔记
			if(notePath != null){
				mDiaryPath = notePath;
				reload(notePath + ".note");
			}
		}

		int id = getIntent().getIntExtra("noteID", 0);
		if (id > 0) {
			initWeatherAndDate(id);// 若是旧笔记，初始化天气日期信息
		} else {
			initNewWeatherAndDate();// 若是新建笔记，初始化天气日期信息
		}

		// 下面这个布局是笔记转换为图片的视图。即将下面布局中的内容保存为图片
		bitmap_rect_linearlayout = (LinearLayout) findViewById(R.id.bitmap_rect_linearlayout);

		// 监听EditText，监听文本的增删以及图片的增加
		myEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

				ImageSpan[] imgspans = myEdit.getText().getSpans(start,
						start + count, ImageSpan.class);
				if (count == 0) {
					imgspans = new ImageSpan[0];
				}

				if (!isNeedSaveChange) { // 如果不许要对mStrList修改，比如翻页前的清除文本框操作
					isNeedSaveChange = true;
					return;
				}

				hasChanged = true;

				int position = findstartPosition(start);
				if (imgspans.length == 0) {// 没有插入图片，即为文本改变（保存文本插入和删除，由于禁用了文本选择，故不存在文本替换）
					String strItem = "";
					if (mStrList.size() > mLastPageEnd + position) {// 获取当前光标所在位置处映射到mStrList的内容
						strItem = mStrList.get(mLastPageEnd + position);
					}
					try {
						String newString = getNewString(start);
						if (newString.length() != 0) {// 有文本插入
							if (strItem.startsWith("str:")) {// 若光标所在位置以前本来就有文本，则将原来的文本替换为新文本
								strItem = mStrList.set(mLastPageEnd + position,
										"str:" + newString);
							} else if (strItem.endsWith("")) {// 若光标所在位置以前没有文本，则插入已句文本
								mStrList.add(mLastPageEnd + position, "str:"
										+ newString);
							}
						} else {// 没有文本插入
							if (strItem.startsWith("str:")) {// 若光标所在位置以前有文本，则删除那已项
								mStrList.remove(mLastPageEnd + position);
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {// 插入的是图片，若图片在文字中间，则将文字分割。
					ImageSpan span = imgspans[0];
					int start_index = myEdit.getText().getSpanStart(span); // ImageSpan起始Index
					int end_index = myEdit.getText().getSpanEnd(span); // ImageSpan结束Index
					String spanStr = myEdit.getText()
							.subSequence(start_index, end_index).toString();
					String[] arrayStr = spanStr.split("_");
					String beforeStr = "";
					String afterStr = getNewString(end_index);
					if (afterStr.length() != 0 && findEndIndex(start) < start) {// 若end_index后面有文本
						beforeStr = getNewString(start_index); // start_index前方的文本,若长度不为零，则插入ImageSpan把以前的文本分割
					}
					int picindex = mLastPageEnd + position;// ImageSpan在mStrList的映射Index

					if (findEndIndex(start) < start && afterStr.length() == 0) {
						picindex = mLastPageEnd + position + 1;
					}

					if (arrayStr.length == 2) {
						if (arrayStr[0].equals("hw")) {
							mStrList.add(picindex, "hw:" + spanStr);
							if (beforeStr.length() != 0) {
								mStrList.add(picindex, "str:" + beforeStr);
								String str1 = mStrList.get(mLastPageEnd
										+ position + 2);// 2代表前面两次add的操作
								if (str1.startsWith("str:")) {
									mStrList.set(mLastPageEnd + position + 2,
											"str:" + afterStr);
								}
							}
						} else if (arrayStr[0].equals("pic")) {
							mStrList.add(picindex, "pic:" + spanStr);
							if (beforeStr.length() != 0) {
								mStrList.add(picindex, "str:" + beforeStr);
								String str1 = mStrList.get(mLastPageEnd
										+ position + 2);
								if (str1.startsWith("str:")) {
									mStrList.set(mLastPageEnd + position + 2,
											"str:" + afterStr);
								}
							}
						} else if (arrayStr[0].equals("face")) {
							mStrList.add(picindex, "face:" + spanStr);
							if (beforeStr.length() != 0) {
								mStrList.add(picindex, "str:" + beforeStr);
								String str1 = mStrList.get(mLastPageEnd
										+ position + 2);
								if (str1.startsWith("str:")) {
									mStrList.set(mLastPageEnd + position + 2,
											"str:" + afterStr);
								}
							}
						}
					}
				}
			}

			// 下面主要是监听图片的删除
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				if (!isNeedSaveChange) {
					return;
				}
				ImageSpan[] imgspans = myEdit.getText().getSpans(start,
						start + count, ImageSpan.class);
				if (count == 0) {
					imgspans = new ImageSpan[0];
				}
				int position = findstartPosition(start);
				if (imgspans.length != 0) {// 删除的是图片。

					int picindex = mLastPageEnd + position;

					if (findEndIndex(start) < start) {
						picindex = mLastPageEnd + position + 1;
					}

					if (mStrList.size() > picindex && mStrList.size() > 0) {// 删除ImageSpan在mStrList中的一项
						mStrList.remove(picindex);
					}

					if (mStrList.size() > picindex && picindex > mLastPageEnd
							&& mStrList.get(picindex - 1).startsWith("str:")
							&& mStrList.get(picindex).startsWith("str:")) {
						// 若删除ImageSpan位置前后都有文本，则应将文本合并
						String tempStr = mStrList.get(picindex);
						tempStr = tempStr.substring(tempStr.indexOf(":") + 1);
						mStrList.set(picindex - 1, mStrList.get(picindex - 1)
								+ tempStr);
						mStrList.remove(picindex);
					}
				}
			}

			// 实现文本改变时翻页的下过
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if (isInsert) {
					return;
				}
				int totalLine = myEdit.getLineCount();
				int i = countLinesHeight(totalLine);

				if (i != totalLine) { // 超出视图边界
					int lineStart = myEdit.getLayout().getLineStart(i + 1);
					int textLength = myEdit.getText().length();
					if (lineStart < textLength) { // 超出部分

						processWhenOutofbounds(i, lineStart, textLength);

						int curLine = myEdit.getLayout().getLineForOffset(
								myEdit.getSelectionStart());
						if (curLine >= i + 1) {// 若光标在最后，则翻到下一页
							moveNextPage(false);
						} else {// 若光标不在最后，则删除EditText中超出部分的内容
							myEdit.getText().delete(lineStart, textLength);
						}
					}
				} else { // 未超出视图边界
					if (mCurPage < mTotalPage) {
						processWhenInBounds();
					}
				}
			}
		});

		if (Integer.parseInt(Build.VERSION.SDK) >= 9) {
			Typeface type = Typeface.createFromAsset(getAssets(), "xdxwzt.ttf");
			myEdit.setTypeface(type);
		}

		myEdit.setEditNote(this);

		MainScreen.eventService.add(this);

		flipper = (ViewFlipper) findViewById(R.id.flipper);
		flipper.removeAllViews();
	}

	/**
	 * 计算编辑框的高度，若超过一页高度则返回超出的那一行行号
	 * 
	 * @param totalLine
	 *            总行数
	 * @return 返回超出边界行的行号，若没超出边界则返回行数。
	 */
	private int countLinesHeight(int totalLine) {
		int totalHeight = 0;
		Rect rc = new Rect();
		int i = 0;
		for (i = 0; i < totalLine; i++) {
			myEdit.getLineBounds(i, rc);
			int curLineHeight = rc.height();
			totalHeight += curLineHeight;
			if (totalHeight > myEdit.getHeight()) {
				return i;
			}
		}
		return totalLine;
	}

	/**
	 * 当超出一页边界时的逻辑处理
	 * 
	 * @param i
	 *            行数
	 * @param lineStart
	 *            那一行的起始文字所在位置
	 * @param textLength
	 *            文本的长度。
	 */
	private void processWhenOutofbounds(int i, int lineStart, int textLength) {
		isNeedSaveChange = false;

		int position = findstartPosition(lineStart);
		String strItem = "";
		if (mStrList.size() > mLastPageEnd + position) {
			strItem = mStrList.get(mLastPageEnd + position);
		}

		String newString = getNewString(lineStart);
		if (newString.length() != 0) {
			if (strItem.startsWith("str:")) {// 超出部分本来就是字符串，则将字符串分割，一部分在当前页，一部分放在下一页。
				String strTemp = myEdit.getText()
						.subSequence(findEndIndex(lineStart), lineStart)
						.toString();
				strTemp = strTemp.replace("\n", "\\n");
				mStrList.set(mLastPageEnd + position, "str:" + strTemp);
				int nextPageIndex = findNextPage(mLastPageEnd + position);
				if (nextPageIndex != -1) { // 将新一页的标志清掉。然后重新插入新一页的标志
					mStrList.remove(nextPageIndex);
					mTotalPage--;
				}
				mStrList.add(mLastPageEnd + position + 1, "gft:" + "page"
						+ String.valueOf(mCurPage));
				mTotalPage++;
				// 本页超出的本分
				int excedeIndex = findStartIndex(lineStart);
				if (excedeIndex > lineStart) {
					mStrList.add(
							mLastPageEnd + position + 2,
							"str:"
									+ myEdit.getText()
											.subSequence(lineStart,
													findStartIndex(lineStart))
											.toString().replace("\n", "\\n"));
					mergerSentence(mLastPageEnd + position + 2);
				}
			} else {
				int nextPageIndex = findNextPage(position);
				if (nextPageIndex != -1) { // 将新一页的标志清掉。然后重新插入新一页的标志
					mStrList.remove(nextPageIndex);
					mTotalPage--;
				}
				mStrList.add(mLastPageEnd + position,
						"gft:" + "page" + String.valueOf(mCurPage));
				mTotalPage++;
				mStrList.add(mLastPageEnd + position + 1, "str:" + newString);
				mergerSentence(mLastPageEnd + position + 1);
			}
		} else {
			int nextPageIndex = findNextPage(position);
			// 将新一页的标志清掉。然后重新插入新一页的标志
			if (nextPageIndex != -1) {
				mStrList.remove(nextPageIndex);
				mTotalPage--;
			}
			mStrList.add(mLastPageEnd + position,
					"gft:" + "page" + String.valueOf(mCurPage));
			mTotalPage++;
		}

	}

	/**
	 * 当输入时，当前页的文本未超出视图边界， 则将后面一页的内容填充至当前页直至当前页的内容超出边界
	 */
	private void processWhenInBounds() {
		int textLength = myEdit.getText().length();
		int position = findstartPosition(textLength);

		int pageStart = findNextPage(mLastPageEnd + position);
		String pageStr = mStrList.get(pageStart);
		if (pageStart == -1) {
			return;
		}
		pageStart = pageStart + 1;
		int pageEnd = findNextPage(pageStart);
		if (pageEnd == -1) {
			pageEnd = mStrList.size();
		}

		for (int i = pageStart; i < pageEnd; i++) {
			isInsert = true;
			addItemOfEditText(i);
			int totalLine = myEdit.getLineCount();
			int j = countLinesHeight(totalLine);
			wrapItemOfList(mStrList.indexOf(pageStr), i);

			if (j != totalLine) {
				int lineStart = myEdit.getLayout().getLineStart(j + 1);
				int textLength2 = myEdit.getText().length();
				if (lineStart < textLength2) { // 超出本分
					processWhenOutofbounds(i, lineStart, textLength);
					myEdit.getText().delete(lineStart, textLength);
				}

				isInsert = false;
				break;
			}
			isInsert = false;
		}

		if (pageStart > 1) {
			mergerSentence(pageStart - 2);
		}
	}

	/**
	 * 交换正文链表中的两个元素
	 * 
	 * @param i
	 * @param j
	 */
	private void wrapItemOfList(int i, int j) {
		String str1 = mStrList.get(i);
		String str2 = mStrList.get(j);
		mStrList.set(i, str2);
		mStrList.set(j, str1);
	}

	/**
	 * 将正文中的第i个元素插入到编辑框中
	 * 
	 * @param i
	 */
	private void addItemOfEditText(int i) {
		String tempString = mStrList.get(i);
		isNeedSaveChange = false;
		if (tempString.startsWith("str:")) {
			String str = tempString.substring("str:".length()).replace("\\n",
					"\n");
			myEdit.getEditableText().append(str);
		} else if (tempString.startsWith("hw:")) {
			String value = tempString.substring("hw:".length(),
					tempString.length());

			if (mStore == null) {
				if (gestureFile == null) {
					gestureFile = new File(preffix + "gesture");
				}

				if (!gestureFile.exists()) {
					return;
				}
				mStore = AmGestureLibraries.fromFile(gestureFile);
				mStore.load(false);
			}

			if (mStore != null && mStore.getGestures(value) != null) {
				AmGesture gesture = mStore.getGestures(value).get(0);
				if (gesture == null) {
					return;
				}
				int height = 581;
				int width = 480;
				Bitmap bmp = Bitmap.createBitmap(dip2px(48), dip2px(48
						* height / width),
						Bitmap.Config.ARGB_8888);
				bmp.eraseColor(0x00000000);
				Canvas canvas = new Canvas(bmp);
				Bitmap gestrueBmp = gesture.toBitmap(dip2px(48), dip2px(48
						* height / width), 0,
						gesture.getGesturePaintColor(),
						height, width);
				if (gestrueBmp == null || gestrueBmp.isRecycled()) {
					return;
				}
				canvas.drawBitmap(gestrueBmp, 0, 0, null);
				gestrueBmp.recycle();
				Drawable drawable = new BitmapDrawable(bmp);
				drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight());
				ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
				SpannableString spanStr = new SpannableString(value);
				spanStr.setSpan(span, 0, spanStr.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				myEdit.getEditableText().append(spanStr);
			}
		} else if (tempString.startsWith("pic:")) {
			String value = tempString.substring("pic:".length(),
					tempString.length());
			String path = mPicMap.get(value);
			if (path == null) {
				return;
			}
			if (mPicMap == null) {
				mPicMap = new LinkedHashMap<String, String>();
			}
			Bitmap bmp = decodeFile(new File(path), myEdit.getWidth(),
					myEdit.getHeight());
			ImageSpan span = new ImageSpan(bmp);
			SpannableString spanStr = new SpannableString(value);
			spanStr.setSpan(span, 0, spanStr.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			myEdit.getText().append(spanStr);
		} else if (tempString.startsWith("face:")) {
			String value = tempString.substring("face:".length(),
					tempString.length());
			if (value.endsWith("face_0")) {
				appendFace(R.drawable.face_0);
			} else if (value.endsWith("face_1")) {
				appendFace(R.drawable.face_1);
			} else if (value.endsWith("face_2")) {
				appendFace(R.drawable.face_2);
			} else if (value.endsWith("face_3")) {
				appendFace(R.drawable.face_3);
			} else if (value.endsWith("face_4")) {
				appendFace(R.drawable.face_4);
			} else if (value.endsWith("face_5")) {
				appendFace(R.drawable.face_5);
			} else if (value.endsWith("face_6")) {
				appendFace(R.drawable.face_6);
			} else if (value.endsWith("face_7")) {
				appendFace(R.drawable.face_7);
			} else if (value.endsWith("face_8")) {
				appendFace(R.drawable.face_8);
			} else if (value.endsWith("face_11")) {
				appendFace(R.drawable.face_11);
			} else if (value.endsWith("face_12")) {
				appendFace(R.drawable.face_12);
			} else if (value.endsWith("face_13")) {
				appendFace(R.drawable.face_13);
			} else if (value.endsWith("face_14")) {
				appendFace(R.drawable.face_14);
			} else if (value.endsWith("face_15")) {
				appendFace(R.drawable.face_15);
			} else if (value.endsWith("face_16")) {
				appendFace(R.drawable.face_16);
			} else if (value.endsWith("face_17")) {
				appendFace(R.drawable.face_17);
			} else if (value.endsWith("face_18")) {
				appendFace(R.drawable.face_18);
			} else if (value.endsWith("face_19")) {
				appendFace(R.drawable.face_19);
			} else if (value.endsWith("face_10")) {
				appendFace(R.drawable.face_10);
			}
		}

	}

	/**
	 * 查找光标所在位置内容在正文链表中所在的位置
	 * 
	 * @param start
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private int findstartPosition(int start) {
		int newLineNum = 0;
		if (start == 0) {
			return 0;
		}
		ImageSpan[] imgspans = myEdit.getText().getSpans(0, start,
				ImageSpan.class);
		Spanned textSpan = myEdit.getText();
		int startIndex = 0;
		int endIndex = 0;
		Arrays.sort(imgspans, new SpanComparator());
		for (ImageSpan span : imgspans) {
			startIndex = textSpan.getSpanStart(span);
			if (endIndex != startIndex) {
				newLineNum++;
			}
			endIndex = textSpan.getSpanEnd(span);
			newLineNum++;
		}

		return newLineNum;
	}

	@SuppressWarnings("rawtypes")
	private class SpanComparator implements Comparator {

		@Override
		public int compare(Object o1, Object o2) {
			// TODO Auto-generated method stub
			int firstIndex = myEdit.getText().getSpanStart((ImageSpan) o1);

			int secondIndex = myEdit.getText().getSpanStart((ImageSpan) o2);
			if (firstIndex > secondIndex) {
				return 1;
			} else if (firstIndex < secondIndex) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	/**
	 * 查找光标所在位置的上一个图片Span的结束位置
	 * 
	 * @param start
	 * @return
	 */
	private int findEndIndex(int start) {
		int endIndex = 0;
		if (start == 0) {
			return 0;
		}
		ImageSpan[] imgspans = myEdit.getText().getSpans(0, start,
				ImageSpan.class);
		Arrays.sort(imgspans, new SpanComparator());
		Spanned textSpan = myEdit.getText();
		for (ImageSpan span : imgspans) {
			int tempIndex = textSpan.getSpanEnd(span);
			if (tempIndex > endIndex) {
				endIndex = tempIndex;
			}
		}
		return endIndex;
	}

	/**
	 * 查找光标所在位置的下一个图片Span的开始位置
	 * 
	 * @param start
	 * @return
	 */
	private int findStartIndex(int start) {
		int startIndex = myEdit.getText().length();
		if (start >= myEdit.getText().length()) {
			return start;
		}
		ImageSpan[] imgspans = myEdit.getText().getSpans(start,
				myEdit.getText().length(), ImageSpan.class);
		Arrays.sort(imgspans, new SpanComparator());
		Spanned textSpan = myEdit.getText();
		for (ImageSpan span : imgspans) {
			int tempIndex = textSpan.getSpanStart(span);
			if (tempIndex < startIndex) {
				startIndex = tempIndex;
			}
		}
		return startIndex;
	}

	/**
	 * 查找下一页在正文链表的位置
	 * 
	 * @param start
	 * @return
	 */
	private int findNextPage(int start) {
		if (start >= mStrList.size()) {
			return -1;
		}
		int i = start;
		for (; i < mStrList.size(); i++) {
			String strItem = mStrList.get(i);
			if (strItem.startsWith("gft:")) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 查找上一页在正文链表的位置
	 * 
	 * @param start
	 * @return
	 */
	private int findPrePage(int start) {
		if (start <= 0) {
			return -1;
		}
		int i = start;
		for (; i > 0; i--) {
			String strItem = mStrList.get(i);
			if (strItem.startsWith("gft:")) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 获得光标所在位置的字符串，即光标所在位置前后两个图片Span中间的字符串内容
	 * 
	 * @param start
	 * @return
	 */
	private String getNewString(int start) {
		ImageSpan[] imgspanstart = myEdit.getText().getSpans(0, start,
				ImageSpan.class);
		ImageSpan[] imgSpanend = myEdit.getText().getSpans(start,
				myEdit.getText().length(), ImageSpan.class);
		int startIndex = 0;
		if (start == 0) {
			imgspanstart = new ImageSpan[0];
		}
		for (ImageSpan span : imgspanstart) {
			int tempIndex = myEdit.getText().getSpanEnd(span);
			if (tempIndex > startIndex) {
				startIndex = tempIndex;
			}
		}

		int endIndex = myEdit.getText().length();
		if (start == endIndex) {
			imgSpanend = new ImageSpan[0];
		}
		for (ImageSpan span : imgSpanend) {
			int tempIndex = myEdit.getText().getSpanStart(span);
			if (tempIndex < endIndex && tempIndex >= startIndex) {
				endIndex = tempIndex;
			}
		}

		return myEdit.getText().subSequence(startIndex, endIndex).toString()
				.replace("\n", "\\n");
	}

	/**
	 * 合并正文链表中的两个连续的字符串
	 * 
	 * @param firstIndex
	 *            链表中需要合并的第一个元素索引
	 */
	private void mergerSentence(int firstIndex) {
		if (firstIndex >= mStrList.size() - 1) {
			return;
		}

		int secondIndex = firstIndex + 1;
		String firstStr = mStrList.get(firstIndex);
		String secondStr = mStrList.get(secondIndex);

		String mergerStr = firstStr;
		if (firstStr.startsWith("str:") && secondStr.startsWith("str:")) {
			mergerStr = firstStr + secondStr.substring("str:".length());
			mStrList.set(firstIndex, mergerStr);
			mStrList.remove(secondIndex);
		}

	}

	/**
	 * 滑动到下一页
	 * 
	 * @return
	 */
	public boolean moveNextPage(boolean isSave) {
		if (mCurPage < mTotalPage) {
			int pageStart = findNextPage(mLastPageEnd);
			if (pageStart == -1) {
				return false;
			}
			pageStart = pageStart + 1;
			int pageEnd = findNextPage(pageStart);
			if (pageEnd == -1) {
				pageEnd = mStrList.size();
			} /*
			 * else { pageEnd = pageEnd - 1; }
			 */
			myEdit.addGraffit("page" + String.valueOf(mCurPage));
			isNeedSaveChange = false;
			isInsert = true;
			myEdit.getEditableText().clear();
			reInsert(pageStart, pageEnd);
			isInsert = false;
			mCurPage++;
			mLastPageEnd = pageStart;

			myEdit.reloadGraffit("page" + String.valueOf(mCurPage));

			int totalLine = myEdit.getLineCount();
			int i = countLinesHeight(totalLine);

			if (i != totalLine) {
				int lineStart = myEdit.getLayout().getLineStart(i + 1);
				int textLength = myEdit.getText().length();
				if (lineStart < textLength) { // 超出本分
					processWhenOutofbounds(i, lineStart, textLength);
					myEdit.getText().delete(lineStart, textLength);
				}
			} else {
				if (mCurPage < mTotalPage) {
					processWhenInBounds();
				}
			}

			if (isSave) {
				System.out.println("flipper =2= " + flipper.getChildCount() + ", " + flipper.getCurrentView());

				flipper.removeAllViews();
				flipper.setVisibility(View.GONE);
				
				FrameLayout fl = (FrameLayout) findViewById(R.id.framelayout1);
				System.out.println("==fl.getChildCount : " + fl.getChildCount());
				if (fl.getChildCount()==3) {
					fl.addView(mScrollView);
					 //isRemoveEdit = false;
				}
			} else {
				FrameLayout fl = (FrameLayout) findViewById(R.id.framelayout1);
				fl.removeView(mScrollView);
				System.out.println("flipper =0= " + flipper.getChildCount() + ", " + flipper.getCurrentView());
				flipper.removeAllViews();
				flipper.addView(mScrollView, 0);
				System.out.println("flipper =1= " + flipper.getChildCount() + ", " + flipper.getCurrentView());

				flipper.setInAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_left_in));
				flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_left_out));

				flipper.setDisplayedChild(0);
				Toast.makeText(EditNoteScreen.this, "第" + (mCurPage+1) + "页, 共"+ (mTotalPage+1) + "页",
						Toast.LENGTH_SHORT).show();
			}

			// FrameLayout fl = (FrameLayout) findViewById(R.id.framelayout1);
			// fl.removeView(myEdit);
			//
			// flipper.removeAllViews();
			// flipper.addView(myEdit,0);
			//
			// flipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.push_left_in));
			// flipper.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.push_left_out));
			//
			// flipper.setDisplayedChild(0);
			Selection.setSelection(myEdit.getEditableText(), myEdit.getText()
					.length());
			isNeedSaveChange = true;
			return true;
		}
		return false;
	}

	/**
	 * 滑动到上一页
	 */
	public void movePrePage() {
		if (mCurPage > 0) {
			int pageStart = findPrePage(mLastPageEnd - 2);
			if (pageStart == -1) {
				pageStart = 0;
			}
			int pageEnd = mLastPageEnd - 1;
			myEdit.addGraffit("page" + String.valueOf(mCurPage));
			isNeedSaveChange = false;
			isInsert = true;
			myEdit.getEditableText().clear();
			reInsert(pageStart, pageEnd);
			isInsert = false;
			mCurPage--;
			myEdit.reloadGraffit("page" + String.valueOf(mCurPage));
			if (pageStart == 0) {
				mLastPageEnd = pageStart;
			} else {
				mLastPageEnd = pageStart + 1;
			}
			FrameLayout fl = (FrameLayout) findViewById(R.id.framelayout1);
			fl.removeView(mScrollView);
			//isRemoveEdit = true;

			flipper.removeAllViews();
			flipper.addView(mScrollView, 0);

			flipper.setInAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_right_in));
			flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_right_out));

			flipper.setDisplayedChild(0);
			Selection.setSelection(myEdit.getEditableText(), myEdit.getText()
					.length());
			isNeedSaveChange = true;
			Toast.makeText(EditNoteScreen.this, "第" + (mCurPage+1) + "页, 共"+ (mTotalPage+1) + "页",
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 将正文链表中元素start - end插入到编辑框中
	 * 
	 * @param start
	 * @param end
	 */
	private void reInsert(int start, int end) {
		isInsert = true;
		for (int i = start; i < end; i++) {
			addItemOfEditText(i);
		}
		isInsert = false;
	}

	/**
	 * 初始化天气和日期
	 * 
	 * @param id
	 */
	private void initWeatherAndDate(int id) {
		Cursor cursor = ServiceManager.getDbManager().queryLocalNotesById(id);
		if (cursor == null || cursor.getCount() == 0) {

			return;
		}
		cursor.moveToFirst();

		long createTime = Long.parseLong(cursor.getString((cursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME))));

		Calendar timeCal = Calendar.getInstance(Locale.CHINA);
		timeCal.setTimeInMillis(createTime);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		final String dateStr = sdf.format(timeCal.getTime());
		SimpleDateFormat sdf1 = new SimpleDateFormat("EEEE");
		final String weekStr = sdf1.format(timeCal.getTime());

		String weather = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_WEATHER));
		cursor.close();
		int weatherId = 0;
		if (weather == null || "".equals(weather)) {
			new Thread(new Runnable() {
				public void run() {
					initWeather();
				}
			}).start();
		} else if ("sunny".equals(weather)) {
			weather = getString(R.string.weather_sunny);
			weatherId = R.drawable.weather_sunny;
			mWeather = "sunny";
		} else if ("rain".equals(weather)) {
			weather = getString(R.string.weather_rain);
			weatherId = R.drawable.weather_rain;
			mWeather = "rain";
		} else if ("cloudy".equals(weather)) {
			weather = getString(R.string.weather_cloudy);
			weatherId = R.drawable.weather_cloudy;
			mWeather = "cloudy";
		} else if ("snow".equals(weather)) {
			weather = getString(R.string.weather_snow);
			weatherId = R.drawable.weather_snow;
			mWeather = "snow";
		} else {
			weather = getString(R.string.weather_cloudy);
			weatherId = R.drawable.weather_cloudy;
			mWeather = "cloudy";
		}

		final String weatherTemp = weather;
		final int imgIdTemp = weatherId;

		handler.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				TextView dateTextView = (TextView) findViewById(R.id.edit_date_textview);
				dateTextView.setText(dateStr);

				TextView weekTextView = (TextView) findViewById(R.id.edit_week_textview);
				weekTextView.setText(weekStr);

				if (weatherTemp != null && !"".equals(weatherTemp)) {
					weather_tv.setText(weatherTemp);
					mWeatherImg.setImageDrawable(getResources().getDrawable(
							imgIdTemp));
				}
			}
		});

	}

	/**
	 * 得到天气
	 * 
	 * @return
	 */
	public String getWeather() {
		return mWeather;
	}

	/**
	 * 初始化天气，从网络中获取
	 */
	private void initWeather() {
		ServerInterface sinterface = new ServerInterface();
		String prov = "湖北";
		String city = "武汉";
		String result = sinterface.getWeather(prov, city);
		result = result.replace("\\", "");
		// String reg = "weather1:";
		// 编译
		Pattern pattern = Pattern.compile("weather1:([^,]+),");
		Matcher matcher = pattern.matcher(result);
		if (matcher.find()) {
			String ttt = matcher.group();
			ttt = ttt.replace("weather1:", "");
			ttt = ttt.replace(",", "");
			String weather = "";
			int imgId = 0;
			if (ttt.contains("雨")) {
				weather = "雨";
				mWeather = "rain";
				imgId = R.drawable.weather_rain;
			} else if (ttt.contains("雪")) {
				weather = "雪";
				mWeather = "snow";
				imgId = R.drawable.weather_snow;
			} else if (ttt.contains("多云") || ttt.contains("阴")
					|| ttt.contains("沙")) {
				weather = "多云";
				mWeather = "cloudy";
				imgId = R.drawable.weather_cloudy;
			} else if (ttt.contains("晴")) {
				weather = "晴";
				mWeather = "sunny";
				imgId = R.drawable.weather_sunny;
			} else {
				weather = "多云";
				mWeather = "cloudy";
				imgId = R.drawable.weather_cloudy;
			}
			final String weatherTemp = weather;
			final int imgIdTemp = imgId;

			handler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					weather_tv.setText(weatherTemp);
					mWeatherImg.setImageDrawable(getResources().getDrawable(
							imgIdTemp));
				}
			});
		}
	}

	/**
	 * 新建笔记时初始化天气和日期
	 */
	private void initNewWeatherAndDate() {
		long timeTemp = getIntent().getLongExtra("time", 0);
		long createTime = 0;
		if (timeTemp != 0) {
			createTime = timeTemp;
		} else {
			createTime = MainScreen.snoteCreateTime;
		}

		Calendar timeCal = Calendar.getInstance(Locale.CHINA);
		timeCal.setTimeInMillis(createTime);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		final String dateStr = sdf.format(timeCal.getTime());
		SimpleDateFormat sdf1 = new SimpleDateFormat("EEEE");
		final String weekStr = sdf1.format(timeCal.getTime());

		handler.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				TextView dateTextView = (TextView) findViewById(R.id.edit_date_textview);
				dateTextView.setText(dateStr);

				TextView weekTextView = (TextView) findViewById(R.id.edit_week_textview);
				weekTextView.setText(weekStr);
			}
		});

		new Thread(new Runnable() {
			public void run() {
				initWeather();
			}
		}).start();
	}

	/**
	 * 在点击按钮后，将相关视图重置
	 * 
	 * @param v
	 */
	/*
	 * private void resetState(View v) { if ((v.getId() != mViewId) &&(v.getId()
	 * == R.id.edit_insert ||v.getId() == R.id.edit_delete ||v.getId() ==
	 * R.id.edit_inputtype ||v.getId() == R.id.edit_setting)) {
	 * edit_type.setVisibility(View.GONE); pic_type.setVisibility(View.GONE);
	 * setting_type.setVisibility(View.GONE); //
	 * faceGridview.setVisibility(View.GONE); mViewId = v.getId(); }
	 * 
	 * if (v.getId() == R.id.edit_type_graffit ||v.getId() ==
	 * R.id.edit_type_soft ||v.getId() == R.id.edit_insert_pic) { //
	 * faceGridview.setVisibility(View.GONE); if (v.getId() !=
	 * R.id.edit_type_handwrite) { gestureview.setVisibility(View.GONE); }
	 * 
	 * } }
	 */

	/**
	 * 初始化粗细对话框
	 */
	private void initThicknessDialog() {
		thickness_dialog = new Dialog(this, R.style.CornerDialog);
		thickness_dialog.setContentView(R.layout.thickness_dialog);

		thickness_seekbar = (SeekBar) thickness_dialog
				.findViewById(R.id.thickness_setting_bar);
		final TextView thickness_textview = (TextView) thickness_dialog
				.findViewById(R.id.thickness_textview);
		thickness_textview.setText(thickness_seekbar.getProgress() + "px");
		thickness_seekbar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					public void onProgressChanged(SeekBar arg0, int arg1,
							boolean arg2) {
						// TODO Auto-generated method stub
						if (mState == HANDWRITINGSTATE) {
							gestureview.setGestureStrokeWidth(arg1);
						} else if (mState == GRAFFITINSERTSTATE) {
							myEdit.getFingerPen().setStrokeWidth(arg1);
						}
						thickness_textview.setText(arg1 + "px");
					}

					public void onStartTrackingTouch(SeekBar arg0) {
						// TODO Auto-generated method stub

					}

					public void onStopTrackingTouch(SeekBar arg0) {
						// TODO Auto-generated method stub

					}
				});
	}

	/**
	 * 初始化字体对话框
	 */
	/*
	 * private void initFontDialog() { fontchoose_dialog = new Dialog(this);
	 * fontchoose_dialog.setContentView(R.layout.font_dialog_window);
	 * 
	 * ListView lv = (ListView)
	 * fontchoose_dialog.findViewById(R.id.lv_font_setting); lv.setAdapter(new
	 * FontAdapter(this));
	 * fontchoose_dialog.setTitle(getString(R.string.choose_font));
	 * lv.setOnItemClickListener(new OnItemClickListener() {
	 * 
	 * @Override public void onItemClick(AdapterView<?> arg0, View arg1, int
	 * arg2, long arg3) { // TODO Auto-generated method stub if (arg2 == 0) {
	 * Typeface type = Typeface.createFromAsset(getAssets(),"xdxwzt.ttf");
	 * myEdit.setTypeface(type); myEdit.invalidate(); } else if(arg2 == 1) {
	 * myEdit.setTypeface(Typeface.DEFAULT,Typeface.NORMAL);
	 * myEdit.invalidate(); } else if (arg2 == 2) {
	 * myEdit.setTypeface(Typeface.SANS_SERIF,Typeface.ITALIC);
	 * myEdit.invalidate(); }
	 * 
	 * Log.d("=YYY=","arg2 = " + arg2); if (fontchoose_dialog.isShowing()) {
	 * fontchoose_dialog.dismiss(); } } }); }
	 */

	/**
	 * 初始化表情对话框
	 */
	private void initFaceDialog() {
		facechoose_dialog = new Dialog(this, R.style.CornerDialog);
		facechoose_dialog.setContentView(R.layout.face_dialog_window);
		initFaceAdapter();

	}

	/**
	 * 从压缩文件中读取正文内容
	 * 
	 * @param file
	 *            压缩文件路径
	 * @param picStr
	 *            正文中的图片路径
	 * @return
	 */
	public static String readTextFromZip(String file, String[] picStr) {
		if (file == null || "".equals(file)) {
			return null;
		}
		String retStr = "";
		boolean hasPic = false;
		try {
			ZipFile zip = new ZipFile(file);// 由指定的File对象打开供阅读的ZIP文件
			Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip
					.entries();// 获取zip文件中的各条目（子文件）
			while (entries.hasMoreElements()) {// 依次访问各条目
				ZipEntry ze = (ZipEntry) entries.nextElement();
				if (ze.getName().endsWith("text")) {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(zip.getInputStream(ze)));
					String line = "";
					while ((line = br.readLine()) != null) {
						retStr += line;
						retStr += "\n";
						if (line.startsWith("pic:") && !hasPic) {
							picStr[0] = line.substring("pic:".length(),
									line.length());
							hasPic = true;
						}
					}
					br.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retStr;
	}

	/**
	 * 在压缩文件中读取手势，并保存到内存中。
	 * 
	 * @param filePath
	 * @return
	 */
	public static AmGestureLibrary readGestureFromZip(String filePath) {
		Log.d("=TTT=", "readGestureFromZip in");
		if (filePath == null || "".equals(filePath)) {
			return null;
		}
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}
		AmGestureLibrary store = AmGestureLibraries.fromZipFile(filePath);
		store.load(false);
		for (String str : store.getGestureEntries()) {
			Log.d("=TTT=", "str = " + str);
		}
		return store;
	}

	/**
	 * 设置编辑框的dirty标志，为ture时退出时提示用户保存
	 * 
	 * @param flag
	 */
	public void setHasChanged(boolean flag) {
		hasChanged = flag;
	}

	/**
	 * 判断编辑框是否修改
	 * 
	 * @return
	 */
	public boolean hasChanged() {
		return hasChanged;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// resetState(v);
		switch (v.getId()) {
		case R.id.edit_inputtype:
			if (mEditTypePopupWindow == null) {
				initEditTypePopupWindow();
			}

			if (mEditTypePopupWindow.isShowing()) {
				mEditTypePopupWindow.dismiss();
			} else {
				mEditTypePopupWindow.showAsDropDown(edit_input_type,
						DensityUtil.dip2px(this, -55),
						DensityUtil.dip2px(this, BOTTOMOFFSET));
			}

			break;
		case R.id.edit_insert:
			if (mInsertTypePopupWindow == null) {
				initInsertTypePopupWindow();
			}

			if (mInsertTypePopupWindow.isShowing()) {
				mInsertTypePopupWindow.dismiss();
			} else {
				mInsertTypePopupWindow.showAsDropDown(edit_insert, 0,
						DensityUtil.dip2px(this, BOTTOMOFFSET));
			}
			break;
		case R.id.edit_setting:
			if (mSetTypePopupWindow == null) {
				initSetTypePopupWindow();
			}

			if (mSetTypePopupWindow.isShowing()) {
				mSetTypePopupWindow.dismiss();
			} else {
				// ImageView imgView = (ImageView)
				// mSetTypePopupWindow.getContentView().findViewById(R.id.thickness_imageview);
				// TextView tv = (TextView)
				// mSetTypePopupWindow.getContentView().findViewById(R.id.thickness_textview);

				// ImageView fontimgView = (ImageView)
				// mSetTypePopupWindow.getContentView().findViewById(R.id.fontsetting_imageview);
				// TextView fonttv = (TextView)
				// mSetTypePopupWindow.getContentView().findViewById(R.id.fontsetting_textview);

				/*
				 * if (mState == SOFTINPUTSTATE) {
				 * imgView.setImageDrawable(getResources
				 * ().getDrawable(R.drawable.text_thickness_adjust));
				 * tv.setText("加粗");
				 * 
				 * if (Integer.parseInt(Build.VERSION.SDK) >= 9) {
				 * fontimgView.setVisibility(View.VISIBLE);
				 * fonttv.setVisibility(View.VISIBLE); } else {
				 * fontimgView.setVisibility(View.GONE);
				 * fonttv.setVisibility(View.GONE); } } else {
				 */
				// imgView.setImageDrawable(getResources().getDrawable(R.drawable.edit_setting_thickness));
				// tv.setText("粗细");
				// fontimgView.setVisibility(View.GONE);
				// fonttv.setVisibility(View.GONE);
				// }
				mSetTypePopupWindow.showAsDropDown(edit_setting, 0,
						DensityUtil.dip2px(this, BOTTOMOFFSET));
			}
			break;
		case R.id.edit_delete:
			if (mState != GRAFFITINSERTSTATE) {
				int delete_index = myEdit.getSelectionStart();
				if (delete_index <= 0) {
					return;
				}
				Spanned s_delete = myEdit.getText();
				ImageSpan[] imageSpans_delete = s_delete.getSpans(0,
						delete_index, ImageSpan.class);
				if (imageSpans_delete.length == 0) {
					int delete_index_temp = delete_index - 1 < 0 ? 0
							: delete_index - 1;
					myEdit.getText().delete(delete_index_temp, delete_index);
					return;
				}

				Arrays.sort(imageSpans_delete, new SpanComparator());
				ImageSpan imgSpan_delete = imageSpans_delete[imageSpans_delete.length - 1];

				if (s_delete.getSpanEnd(imgSpan_delete) != delete_index) {
					int delete_index_temp = delete_index - 1 < 0 ? 0
							: delete_index - 1;
					myEdit.getText().delete(delete_index_temp, delete_index);
					return;
				}

				int start_index = s_delete.getSpanStart(imgSpan_delete);
				int end_index = s_delete.getSpanEnd(imgSpan_delete);
				String spanStr = s_delete.subSequence(start_index, end_index)
						.toString();
				String[] arrayStr = spanStr.split("_");
				if (arrayStr.length == 2) {
					if (arrayStr[0].equals("hw")) {
						mStore.removeGesture(spanStr,
								mStore.getGestures(spanStr).get(0));
						mStore.save(false);
					} else if (arrayStr[0].equals("pic")) {
						mPicMap.remove(spanStr);
					}
				}

				myEdit.getText().delete(start_index, end_index);
			} else {
				if (isgraffit_erase) {
					break;
				}
				mStrokeWidth = myEdit.getFingerPen().getStrokeWidth();
				mFingerColor = myEdit.getFingerPen().getColor();
				myEdit.setFingerStrokeWidth(MAX_STROKEWIDTH);
				myEdit.getFingerPen().setStrokeWidth(MAX_STROKEWIDTH);
				myEdit.getFingerPen().setColor(0xffffffff);
				myEdit.setFingerColor(0x00000000);
				myEdit.setErase(true);
				isgraffit_erase = true;
			}
			break;
		case R.id.edit_type_handwrite:

			mEditTypePopupWindow.dismiss();
			gestureview.setVisibility(View.VISIBLE);
			myEdit.setFocusable(true);
			myEdit.setFocusableInTouchMode(true);
			edit_input_type.setImageDrawable(getResources().getDrawable(
					R.drawable.edit_handwrite_selector));
			edit_delete.setImageDrawable(getResources().getDrawable(
					R.drawable.edit_delete_1_selector));
			mState = HANDWRITINGSTATE;
			break;
		case R.id.edit_type_graffit:
			mEditTypePopupWindow.dismiss();
			gestureview.setVisibility(View.GONE);
			myEdit.setFocusable(true);
			myEdit.setFocusableInTouchMode(true);
			myEdit.setErase(false);
			if (isgraffit_erase) {
				myEdit.setFingerStrokeWidth((int) mStrokeWidth);
				myEdit.colorChanged(mFingerColor);
				myEdit.getFingerPen().setXfermode(null);
				isgraffit_erase = false;
				break;
			}

			if (mState == GRAFFITINSERTSTATE) {
				break;
			}
			edit_input_type.setImageDrawable(getResources().getDrawable(
					R.drawable.edit_graffit_selector));
			edit_delete.setImageDrawable(getResources().getDrawable(
					R.drawable.edit_delete_selector));

			mState = GRAFFITINSERTSTATE;
			break;
		case R.id.edit_type_soft:
			mEditTypePopupWindow.dismiss();
			gestureview.setVisibility(View.GONE);
			myEdit.setFocusable(false);
			myEdit.setFocusableInTouchMode(false);
			if (mState == READNOTESTATE) {
				break;
			}
			edit_input_type.setImageDrawable(getResources().getDrawable(
					R.drawable.edit_readnote_selector));
			mState = READNOTESTATE;
			break;
		case R.id.edit_insert_face:
			mInsertTypePopupWindow.dismiss();
			if (facechoose_dialog == null) {
				initFaceDialog();
			}

			facechoose_dialog.show();
			break;
		case R.id.edit_insert_pic:
			if (picchoose_dialog == null) {
				picchoose_dialog = new Dialog(this, R.style.CornerDialog);
				picchoose_dialog.setContentView(R.layout.dialog_pic_source);
				TextView cameraView = (TextView) picchoose_dialog
						.findViewById(R.id.dialog_item_camera);
				cameraView.setOnClickListener(this);
				TextView localView = (TextView) picchoose_dialog
						.findViewById(R.id.dialog_item_local);
				localView.setOnClickListener(this);
			}
			picchoose_dialog.show();
			mInsertTypePopupWindow.dismiss();

			break;
		case R.id.edit_insert_space:
			int index = myEdit.getSelectionStart();
			index = index < 0 ? 0 : index;
			myEdit.getText().insert(index, " ");
			mInsertTypePopupWindow.dismiss();
			break;
		case R.id.edit_insert_newline:
			int index1 = myEdit.getSelectionStart();
			index1 = index1 < 0 ? 0 : index1;
			myEdit.getText().insert(index1, "\n");
			mInsertTypePopupWindow.dismiss();
			break;
		case R.id.edit_setting_color:
			if (mState == HANDWRITINGSTATE) {
				OnColorChangedListener listener = new OnColorChangedListener() {
					public void colorChanged(int color) {
						gestureview.setGestureColor(color);
					}
				};
				new ColorPickerDialog(this, R.style.CornerDialog,listener,
						gestureview.getGestureColor()).show();
			} else if (mState == GRAFFITINSERTSTATE) {
				new ColorPickerDialog(this, R.style.CornerDialog, myEdit, myEdit.getFingerPen()
						.getColor()).show();
			} /*
			 * else if (mState == SOFTINPUTSTATE) { OnColorChangedListener
			 * listener = new OnColorChangedListener() { public void
			 * colorChanged(int color) { myEdit.setTextColor(color); } }; new
			 * ColorPickerDialog(this, listener,
			 * myEdit.getCurrentTextColor()).show(); }
			 */
			mSetTypePopupWindow.dismiss();
			break;
		case R.id.edit_setting_thickness:
			if (mState == HANDWRITINGSTATE || mState == GRAFFITINSERTSTATE) {
				if (thickness_dialog == null) {
					initThicknessDialog();
				}
				thickness_dialog.show();
			} /*
			 * else if (mState == SOFTINPUTSTATE) { if
			 * (myEdit.getPaint().isFakeBoldText()) {
			 * myEdit.getPaint().setFakeBoldText(false); myEdit.invalidate(); }
			 * else { myEdit.getPaint().setFakeBoldText(true);
			 * myEdit.invalidate(); } }
			 */
			mSetTypePopupWindow.dismiss();
			break;
		/*
		 * case R.id.edit_setting_font: if (fontchoose_dialog == null) {
		 * initFontDialog(); } fontchoose_dialog.show();
		 * mSetTypePopupWindow.dismiss(); break;
		 */
		case R.id.dialog_item_camera:
			String pName = picName.generateName();
			if ("".equals(mDiaryPath)) {
				mDiaryPath = preffix + "diary_" + MainScreen.snoteCreateTime;
			}
			String noteFileName = "";
			if (!"".equals(mDiaryPath)) {
				noteFileName = mDiaryPath
						.substring(mDiaryPath.lastIndexOf("/") + 1);
			}

			imageFilePath = NoteApplication.savePath + "pic/" + noteFileName
					+ "_pic_" + pName + ".jpg";

			File imageFile = new File(imageFilePath);
			if (!imageFile.exists()) {
				try {
					imageFile.getParentFile().mkdirs();
					imageFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Uri imageFileUri = Uri.fromFile(imageFile);
			Intent i = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
			startActivityForResult(i, CAMERA_RESULT);
			picchoose_dialog.dismiss();
			break;
		case R.id.dialog_item_local:
			Intent intent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			intent.setType("image/*");
			// intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(Intent.createChooser(intent, "使用以下内容完成操作"),
					ALBUM_RESULT);
			picchoose_dialog.dismiss();
			break;
		case R.id.edit_weather_linearlayout:
			if (mWeatherPopupWindow.isShowing()) {
				mWeatherPopupWindow.dismiss();
			} else {
				mWeatherPopupWindow.showAsDropDown(mWeatherLayout, 0, -3);
			}
			break;
		case R.id.weather_sunny:
			weather_tv.setText(getString(R.string.weather_sunny));
			mWeatherImg.setImageDrawable(getResources().getDrawable(
					R.drawable.weather_sunny));
			mWeatherPopupWindow.dismiss();
			setHasChanged(true);
			mWeather = "sunny";
			hasChanged = true;
			break;
		case R.id.weather_cloudy:
			weather_tv.setText(getString(R.string.weather_cloudy));
			mWeatherImg.setImageDrawable(getResources().getDrawable(
					R.drawable.weather_cloudy));
			mWeatherPopupWindow.dismiss();
			setHasChanged(true);
			mWeather = "cloudy";
			hasChanged = true;
			break;
		case R.id.weather_rain:
			weather_tv.setText(getString(R.string.weather_rain));
			mWeatherImg.setImageDrawable(getResources().getDrawable(
					R.drawable.weather_rain));
			mWeatherPopupWindow.dismiss();
			setHasChanged(true);
			mWeather = "rain";
			hasChanged = true;
			break;
		case R.id.weather_snow:
			weather_tv.setText(getString(R.string.weather_snow));
			mWeatherImg.setImageDrawable(getResources().getDrawable(
					R.drawable.weather_snow));
			mWeatherPopupWindow.dismiss();
			setHasChanged(true);
			mWeather = "snow";
			hasChanged = true;
			break;
		case R.id.screen_top_play_control_back:
			if (!hasChanged) {
				finish();
				break;
			}
			if (save_dialog == null) {
				save_dialog = new NoteSaveDialog(this);
			}
			save_dialog.show();
			break;
		}
	}

	/**
	 * 初始化天气popwindow
	 */
	private void initPopupWindow() {
		View view = getLayoutInflater().inflate(R.layout.dialog_weather, null);

		LinearLayout weather_sunny = (LinearLayout) view
				.findViewById(R.id.weather_sunny);
		LinearLayout weather_cloudy = (LinearLayout) view
				.findViewById(R.id.weather_cloudy);
		LinearLayout weather_rain = (LinearLayout) view
				.findViewById(R.id.weather_rain);
		LinearLayout weather_snow = (LinearLayout) view
				.findViewById(R.id.weather_snow);
		weather_sunny.setOnClickListener(this);
		weather_cloudy.setOnClickListener(this);
		weather_rain.setOnClickListener(this);
		weather_snow.setOnClickListener(this);

		mWeatherPopupWindow = new PopupWindow(view, DensityUtil.dip2px(this,
				200), DensityUtil.dip2px(this, 90), true);
		mWeatherPopupWindow.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.edit_dropdown_dialog));
		mWeatherPopupWindow.setOutsideTouchable(true);
	}

	/**
	 * 初始化输入方式的popwindow
	 */
	private void initEditTypePopupWindow() {
		View view = getLayoutInflater().inflate(R.layout.popwindow_inputtype,
				null);

		LinearLayout graffit_input = (LinearLayout) view
				.findViewById(R.id.edit_type_graffit);
		LinearLayout soft_input = (LinearLayout) view
				.findViewById(R.id.edit_type_soft);
		LinearLayout handwrite_input = (LinearLayout) view
				.findViewById(R.id.edit_type_handwrite);
		graffit_input.setOnClickListener(this);
		soft_input.setOnClickListener(this);
		handwrite_input.setOnClickListener(this);

		mEditTypePopupWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, true);
		mEditTypePopupWindow.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.pop_layout_bg));
		mEditTypePopupWindow.setOutsideTouchable(true);
	}

	/**
	 * 初始化插入类型popwindow
	 */
	private void initInsertTypePopupWindow() {
		View view = getLayoutInflater().inflate(R.layout.popwindow_inserttype,
				null);

		LinearLayout pic_insert = (LinearLayout) view
				.findViewById(R.id.edit_insert_pic);
		LinearLayout face_insert = (LinearLayout) view
				.findViewById(R.id.edit_insert_face);
		LinearLayout space_insert = (LinearLayout) view
				.findViewById(R.id.edit_insert_space);
		LinearLayout newline_insert = (LinearLayout) view
				.findViewById(R.id.edit_insert_newline);
		pic_insert.setOnClickListener(this);
		face_insert.setOnClickListener(this);
		space_insert.setOnClickListener(this);
		newline_insert.setOnClickListener(this);

		mInsertTypePopupWindow = new PopupWindow(view,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		mInsertTypePopupWindow.setBackgroundDrawable(getResources()
				.getDrawable(R.drawable.pop_layout_bg_left));
		mInsertTypePopupWindow.setOutsideTouchable(true);
	}

	/**
	 * 初始化设置类型popwindow
	 */
	private void initSetTypePopupWindow() {
		View view = getLayoutInflater().inflate(R.layout.popwindow_settype,
				null);
		mSetTypePopupWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, true);
		mSetTypePopupWindow.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.pop_layout_bg_right));
		mSetTypePopupWindow.setOutsideTouchable(true);
		LinearLayout color_setting = (LinearLayout) view
				.findViewById(R.id.edit_setting_color);
		LinearLayout thickness_setting = (LinearLayout) view
				.findViewById(R.id.edit_setting_thickness);
		// LinearLayout font_setting = (LinearLayout)
		// view.findViewById(R.id.edit_setting_font);
		color_setting.setOnClickListener(this);
		thickness_setting.setOnClickListener(this);
		// font_setting.setOnClickListener(this);
	}

	/**
	 * 退出前保存笔记
	 */
	public void save() {
		// 保存涂鸦
		saveGraffit();

		// 删除上一次保存的图片
		if (!"".equals(mDiaryPath) && mDiaryPath != null) {
			ArrayList<String> strList = getNotePictureFromZip(mDiaryPath);
			deletefiles(strList);
		}

		mPicPathList.clear();
		reloadFirstPage();
		myEdit.reloadGraffit("page0");
		myEdit.scrollTo(0, 0);
		myEdit.setCursorVisible(false);
		convertDiary2Pic();

		while (moveNextPage(true)) {
			myEdit.scrollTo(0, 0);
			convertDiary2Pic();
		}

		myEdit.setCursorVisible(true);
		// 保存图片资源
		writePicMap();

		// 保存手写笔记
		saveGesture();

		// 保存正文
		saveList2File(preffix + "text", mStrList);

		// 保存笔记转换成的图片地址
		saveList2File(preffix + "notepicindex", mPicPathList);

		// 压缩成一个文件
		String[] fileNames = { preffix + "gesture", preffix + "picmap",
				preffix + "graffit", preffix + "text",
				preffix + "notepicindex", preffix + "pic/" };
		if ("".equals(mDiaryPath)) {
			mDiaryPath = preffix + "diary_" + MainScreen.snoteCreateTime;
		}
		zipFile(fileNames, mDiaryPath + ".note");
		mPicPathList.add(mDiaryPath + ".note");

		hasChanged = false;
	}

	/**
	 * 将链表中的内容保存为文件
	 * 
	 * @param fileName
	 *            文件名
	 * @param strList
	 *            链表对象
	 */
	private void saveList2File(String fileName, ArrayList<String> strList) {
		File f = new File(fileName);
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			if (!f.exists()) {
				f.getParentFile().mkdir();
				f.createNewFile();
			}
			fw = new FileWriter(f);
			bw = new BufferedWriter(fw);
			for (String str : strList) {
				bw.write(str + "\n");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		EditNoteScreen.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				isInsert = true;
				isNeedSaveChange = false;
				myEdit.getText().clear();
				myEdit.recycleBitmap();
				if (gestureview != null) {
					gestureview.clear(false);
					gestureview = null;
				}
				isInsert = false;
				isNeedSaveChange = true;

				if (flipper != null) {
					flipper.removeAllViews();
					flipper = null;
				}

				myEdit = null;
			}
		});
		// EditNoteScreen.mState = EditNoteScreen.SOFTINPUTSTATE;
		EditNoteScreen.mState = EditNoteScreen.HANDWRITINGSTATE;
		deleteDefaultFiles();

		ServiceManager.getEventservice().remove(this);
		if (mStrList != null) {
			mStrList.clear();
		}
		if (mPicPathList != null) {
			mPicPathList.clear();
		}
		if (mPicMap != null) {
			mPicMap.clear();
		}

		mGesture = null;

		System.gc();
		super.finish();
	}

	/**
	 * 得到笔记转换成的图片
	 * 
	 * @param filePath
	 *            笔记保存地址
	 * @return
	 */
	public static ArrayList<String> getNotePictureFromZip(String filePath) {
		if (filePath == null || "".equals(filePath)) {
			return null;
		}

		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}

		ArrayList<String> strList = new ArrayList<String>();
		try {
			ZipFile zip = new ZipFile(filePath);// 由指定的File对象打开供阅读的ZIP文件
			Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip
					.entries();// 获取zip文件中的各条目（子文件）
			while (entries.hasMoreElements()) {// 依次访问各条目
				ZipEntry ze = (ZipEntry) entries.nextElement();
				if (ze.getName().endsWith("notepicindex")) {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(zip.getInputStream(ze)));
					String line = "";
					while ((line = br.readLine()) != null) {
						strList.add(line);
					}
					br.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strList;
	}

	/**
	 * 保存涂鸦
	 */
	private void saveGraffit() {
		myEdit.addGraffit("page" + String.valueOf(mCurPage));
		myEdit.save();
		Log.d("=TTT=", "saveGraffit curPage = " + mCurPage);
	}

	/**
	 * 将编辑框中的内容保存为图片
	 */
	private void convertDiary2Pic() {
		if ("".equals(mDiaryPath)) {
			mDiaryPath = preffix + "diary_" + MainScreen.snoteCreateTime;
		}
		String noteFileName = "";
		if (!"".equals(mDiaryPath)) {
			noteFileName = mDiaryPath
					.substring(mDiaryPath.lastIndexOf("/") + 1);
		}

		int editHeight = getEditHeight();
		int viewHeight = myEdit.getHeight();
		int offset = 0;
		if (editHeight > viewHeight) {
			offset = editHeight - viewHeight;
		}

		final Bitmap viewBitmap = Bitmap.createBitmap(
				bitmap_rect_linearlayout.getWidth(),
				bitmap_rect_linearlayout.getHeight() + offset,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(viewBitmap);
		bitmap_rect_linearlayout.draw(canvas);

		if (editHeight > viewHeight) {
			myEdit.scrollTo(0, editHeight - viewHeight);
			myEdit.setDrawingCacheEnabled(true);
			myEdit.buildDrawingCache(true);
			Bitmap bm = myEdit.getDrawingCache();
			Rect src = new Rect();
			src.left = 0;
			src.right = bitmap_rect_linearlayout.getWidth();
			src.top = viewHeight - (editHeight - viewHeight);
			src.bottom = myEdit.getHeight();

			Rect dst = new Rect();
			dst.left = 0;
			dst.right = bitmap_rect_linearlayout.getWidth();
			dst.top = bitmap_rect_linearlayout.getHeight();
			dst.bottom = dst.top + src.height();
			Log.d("=CCCC=", "editHeight = " + editHeight + " viewHeight = "
					+ viewHeight + " srcTop = " + src.top + " scrBottom = "
					+ src.bottom + " dstTop = " + dst.top + " dstBottom = "
					+ dst.bottom + " bmpHeight = " + bm.getHeight());

			Paint pt = new Paint();
			pt.setColor(0xFFE8E8E8);
			canvas.drawRect(dst, pt);
			canvas.drawBitmap(bm, src, dst, null);

		}

		final String fileName = NoteApplication.savePath + "notepicture/"
				+ noteFileName + "_page" + String.valueOf(mCurPage) + ".png";
		mPicPathList.add(fileName);
		ImageSpan[] imgspans = myEdit.getText().getSpans(0,
				myEdit.getText().length(), ImageSpan.class);
		for (ImageSpan span : imgspans) {
			int start_index = myEdit.getText().getSpanStart(span);
			int end_index = myEdit.getText().getSpanEnd(span);
			String spanStr = myEdit.getText()
					.subSequence(start_index, end_index).toString();
			if (spanStr.startsWith("pic_")) {
				BitmapDrawable bd = (BitmapDrawable) span.getDrawable();
				Bitmap bmp = bd.getBitmap();
				comPressBmp(bmp, spanStr);
			}
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				File myCaptureFile = new File(fileName);
				if (!myCaptureFile.getParentFile().exists()) {
					myCaptureFile.getParentFile().mkdir();
				}

				try {
					BufferedOutputStream bos = new BufferedOutputStream(
							new FileOutputStream(myCaptureFile));
					viewBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
					bos.flush();
					bos.close();
					viewBitmap.recycle();
					synchronized (thread_lock) {
						mCurSavePage++;
						if (mCurSavePage == mTotalPage + 1) {
							ServiceManager
									.getEventservice()
									.onUpdateEvent(
											new EventArgs(
													EventTypes.NOTE_PIC_SAVE_OVER));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * 获取编辑框的高度
	 * 
	 * @return
	 */
	private int getEditHeight() {
		int totalLine = myEdit.getLineCount();
		int height = 0;

		for (int i = 0; i < totalLine; i++) {
			Rect rc = new Rect();
			myEdit.getLineBounds(i, rc);
			int curLineHeight = rc.height();
			height += curLineHeight;
		}
		return height;
	}

	/**
	 * 获取笔记的存储地址
	 * 
	 * @return
	 */
	public String getDiaryPath() {
		return mDiaryPath;
	}

	/**
	 * 压缩文件
	 * 
	 * @param fileFroms
	 *            需要压缩的文件
	 * @param fileTo
	 *            压缩文件的文件名
	 */
	private void zipFile(String[] fileFroms, String fileTo) {
		try {
			FileOutputStream out = new FileOutputStream(fileTo);
			ZipOutputStream zipOut = new ZipOutputStream(out);
			for (String fileFrom : fileFroms) {
				File file = new File(fileFrom);
				if (!file.exists()) {
					continue;
				}

				if (file.isDirectory()) {
					File[] fl = file.listFiles();
					for (File f : fl) {
						FileInputStream in = new FileInputStream(f);
						ZipEntry entry = new ZipEntry(fileFrom + f.getName());
						zipOut.putNextEntry(entry);
						int nNumber;
						byte[] buffer = new byte[512];
						while ((nNumber = in.read(buffer)) != -1) {
							zipOut.write(buffer, 0, nNumber);
						}
						in.close();
						f.delete();
					}
					file.delete();
				} else {
					FileInputStream in = new FileInputStream(fileFrom);
					ZipEntry entry = new ZipEntry(fileFrom);
					zipOut.putNextEntry(entry);
					int nNumber;
					byte[] buffer = new byte[512];
					while ((nNumber = in.read(buffer)) != -1) {
						zipOut.write(buffer, 0, nNumber);
					}
					in.close();
					file.delete();
				}
			}
			zipOut.close();

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 文件是否存在
	 * 
	 * @param filePath
	 *            文件路径
	 * @return 文件路径
	 */
	private boolean isFileExists(String filePath) {
		File file = new File(filePath);
		return file.exists();
	}

	/**
	 * 重新加载笔记文件并显示到编辑框中
	 * 
	 * @param filePath
	 *            笔记文件的路径
	 */
	private void reload(String filePath) {
		File noteFile = new File(filePath);
		if (!noteFile.exists()) {
			return;
		}
		Unzip(filePath, "");

		try {
			// 加载图片资源，并保存到map中
			if (isFileExists(preffix + "picmap")) {
				if (mPicMap == null) {
					mPicMap = new LinkedHashMap<String, String>();
				}
				SetSystemProperty.loadIntoMap(preffix + "picmap", mPicMap);
				Iterator ite = mPicMap.entrySet().iterator();
				while (ite.hasNext()) {
					Map.Entry<String, String> entry = (Entry<String, String>) ite
							.next();
					String key = entry.getKey();// map中的key
					String value = entry.getValue();// 上面key对应的value

					int curNum = 0;
					try {
						curNum = Integer.parseInt(
								value.substring(value.indexOf("_") + 1),
								value.length());
					} catch (Exception e) {
						curNum = 0;
					}
					if (curNum >= picName.getCurNum()) {
						picName.setCurNum(curNum + 1);
					}
				}
			}

			// 加载手写笔记资源。
			if (gestureFile == null) {
				gestureFile = new File(preffix + "gesture");
			}
			if (gestureFile.exists()) {
				if (mStore == null) {
					mStore = AmGestureLibraries.fromFile(gestureFile);
					mStore.load(false);
				}
				for (String str : mStore.getGestureEntries()) {
					int curNum = 0;
					try {
						String subStr = str.substring((str.indexOf("_") + 1),
								str.length());
						curNum = Integer.parseInt(subStr);
					} catch (Exception e) {
						curNum = 0;
					}
					if (curNum >= gestureName.getCurNum()) {
						gestureName.setCurNum(curNum + 1);
					}
				}
			}

			// 加载正文部分
			File file = new File(preffix + "text");
			if (!file.exists()) {
				return;
			}
			BufferedReader reader = null;
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				mStrList.add(tempString);
				if (tempString.startsWith("gft:")) {
					mTotalPage++;
				}
			}
			reader.close();

			// 加载第一页
			reloadFirstPage();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				String[] fileNames = { preffix + "picmap", preffix + "text" };
				deletefiles(fileNames);
			} catch (Exception e1) {
			}
		}

		// 设置为阅读模式
		gestureview.setVisibility(View.GONE);
		myEdit.setFocusable(false);
		myEdit.setFocusableInTouchMode(false);// 不可编辑
		edit_input_type.setImageDrawable(getResources().getDrawable(
				R.drawable.edit_readnote_selector));
		mState = READNOTESTATE;
	}

	/**
	 * 加载第一页的内容到编辑框中
	 */
	private void reloadFirstPage() {
		int pageEnd = findNextPage(0);
		if (pageEnd == -1) {
			pageEnd = mStrList.size();
		}
		mCurPage = 0;
		mLastPageEnd = 0;
		isInsert = true;
		// myEdit.reloadGraffit("page0");
		isNeedSaveChange = false;
		myEdit.getEditableText().clear();
		reInsert(0, pageEnd);
		isInsert = false;
		isNeedSaveChange = true;
	}

	/**
	 * 删除指定文件
	 * 
	 * @param fileName
	 *            文件名数组
	 */
	public void deletefiles(String[] fileName) {
		for (String filename : fileName) {
			File file = new File(filename);
			if (file.exists()) {
				if (file.isDirectory()) {
					File[] files = file.listFiles();
					for (int i = 0; i < files.length; i++) {
						// 删除子文件
						if (files[i].isFile()) {
							files[i].delete();
						}
					}
				}
				file.delete();
			}
		}
	}

	/**
	 * 删除指定文件
	 * 
	 * @param fileName
	 *            文件名链表
	 */
	public void deletefiles(ArrayList<String> fileName) {
		if (fileName == null || fileName.size() == 0) {
			return;
		}
		for (String filename : fileName) {
			File file = new File(filename);
			if (file.exists()) {
				if (file.isDirectory()) {
					File[] files = file.listFiles();
					for (int i = 0; i < files.length; i++) {
						// 删除子文件
						if (files[i].isFile()) {
							files[i].delete();
						}
					}
				}
				file.delete();
			}
		}
	}

	/**
	 * 删除一些本地的中间文件，本程序生成的。
	 */
	public void deleteDefaultFiles() {
		String[] deletefileNames = { preffix + "picmap", preffix + "text",
				preffix + "gesture", preffix + "graffit", preffix + "pic/" };
		deletefiles(deletefileNames);
	}

	/**
	 * 解压压缩文件。
	 * 
	 * @param zipFile
	 *            压缩文件的路径
	 * @param targetDir
	 *            解压到指定目录
	 */
	private void Unzip(String zipFile, String targetDir) {
		int BUFFER = 4096; // 这里缓冲区我们使用4KB，
		String strEntry; // 保存每个zip的条目名称
		try {
			BufferedOutputStream dest = null; // 缓冲输出流
			FileInputStream fis = new FileInputStream(zipFile);
			ZipInputStream zis = new ZipInputStream(
					new BufferedInputStream(fis));
			ZipEntry entry; // 每个zip条目的实例
			while ((entry = zis.getNextEntry()) != null) {
				try {
					int count;
					byte data[] = new byte[BUFFER];
					strEntry = entry.getName();
					if (strEntry.contains("notepicindex")) {
						continue;
					}
					File entryFile = new File(targetDir + strEntry);
					File entryDir = new File(entryFile.getParent());
					if (!entryDir.exists()) {
						entryDir.mkdirs();
					}
					FileOutputStream fos = new FileOutputStream(entryFile);
					entry.getSize();
					dest = new BufferedOutputStream(fos, BUFFER);
					while ((count = zis.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, count);
					}
					dest.flush();
					dest.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			zis.close();
		} catch (Exception cwj) {
			cwj.printStackTrace();
		}
	}

	/**
	 * 将保存图片的map写到property文件中
	 */
	private void writePicMap() {
		if (mPicMap == null || mPicMap.size() == 0) {
			return;
		}
		for (String str : mPicMap.keySet()) {
			SetSystemProperty.writeProperties(str, mPicMap.get(str));
		}
	}

	/**
	 * 监听手势
	 * 
	 * @author root
	 * 
	 */
	private class GesturesProcessorHandWrite implements
			AmGestureOverlayView.OnAmGestureListener {
		public void onGestureStarted(AmGestureOverlayView overlay,
				MotionEvent event) {
			mGesture = null;
			System.out.println("==onGestureStarted== left : "
					+ overlay.getLeft() + ", top : " + overlay.getTop()
					+ ", bottom : " + overlay.getBottom() + ", Right : "
					+ overlay.getRight());
			System.out.println("==onGestureStarted== x : " + event.getX()
					+ ", y : " + event.getY());
			mColorFullRectView.setVisibility(View.VISIBLE);
		}

		public void onGesture(AmGestureOverlayView overlay, MotionEvent event) {
		}

		public void onGestureEnded(AmGestureOverlayView overlay,
				MotionEvent event) {
			System.out.println("==onGestureEnded== left : " + overlay.getLeft()
					+ ", top : " + overlay.getTop() + ", bottom : "
					+ overlay.getBottom() + ", Right : " + overlay.getRight());
			System.out.println("==onGestureEnded== x : " + event.getX()
					+ ", y : " + event.getY());
			mGesture = overlay.getGesture();
			if (mGesture.getLength() < LENGTH_THRESHOLD) {
				MotionEvent event2 = MotionEvent.obtain(event);
				event2.setAction(MotionEvent.ACTION_DOWN);
				myEdit.onTouchEvent(event2);
				myEdit.onTouchEvent(event);
				overlay.clear(false);
				mColorFullRectView.setVisibility(View.GONE);
			}
		}

		public void onGestureCancelled(AmGestureOverlayView overlay,
				MotionEvent event) {
			System.out.println("==onGestureCancelled== left : "
					+ overlay.getLeft() + ", top : " + overlay.getTop()
					+ ", bottom : " + overlay.getBottom() + ", Right : "
					+ overlay.getRight());
			System.out.println("==onGestureCancelled gestureview== width : "
					+ gestureview.getWidth() + ", heigth : " + gestureview.getHeight());

			if (mGesture == null) {
				return;
			}

			System.out.println("==mGesture.getBoundingBox()== left : "
					+ mGesture.getBoundingBox().left + ", top : "
					+ mGesture.getBoundingBox().top + ", bottom : "
					+ mGesture.getBoundingBox().bottom + ", Right : "
					+ mGesture.getBoundingBox().right);

			if (mGesture.getLength() < LENGTH_THRESHOLD) {
				return;
			}

			mColorFullRectView.setVisibility(View.GONE);

			// int lineheight = DensityUtil.dip2px(EditNoteScreen.this,
			// myEdit.getLineHeight() );
			System.out.println("===create bitmap");
			Bitmap gestrueBmp = mGesture.toBitmap(dip2px(48), dip2px(48
					* gestureview.getHeight() / gestureview.getWidth()), 0,
					mGesture.getGesturePaintColor(), gestureview.getHeight(),
					gestureview.getWidth());
			if (gestrueBmp == null || gestrueBmp.isRecycled()) {
				return;
			}
			Drawable drawable = new BitmapDrawable(gestrueBmp);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
					drawable.getIntrinsicHeight());
			ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
			String gName = gestureName.generateName();
			SpannableString spanStr = new SpannableString("hw_" + gName);
			addGesture("hw_" + gName, mGesture);
			spanStr.setSpan(span, 0, spanStr.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			int index = myEdit.getSelectionStart();
			index = index < 0 ? 0 : index;
			myEdit.getEditableText().insert(index, spanStr);
			overlay.clear(false);
		}
	}

	/**
	 * 添加手势
	 * 
	 * @param name
	 *            手势名称
	 * @param gesture
	 *            手势对象
	 */
	private void addGesture(String name, AmGesture gesture) {
		if (gestureFile == null) {
			gestureFile = new File(preffix + "gesture");
		}

		if (!gestureFile.exists()) {
			try {
				gestureFile.getParentFile().mkdirs();
				gestureFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (mStore == null) {
			mStore = AmGestureLibraries.fromFile(gestureFile);
		}

		mStore.addGesture(name, gesture);
		mStore.save(false);
	}

	/**
	 * 保存所有手势到文件中
	 */
	private void saveGesture() {
		if (gestureFile == null) {
			gestureFile = new File(preffix + "gesture");
		}

		if (!gestureFile.exists()) {
			try {
				gestureFile.getParentFile().mkdirs();
				gestureFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (mStore == null) {
			mStore = AmGestureLibraries.fromFile(gestureFile);
		}

		if (mStore.getGestureEntries() == null
				|| mStore.getGestureEntries().size() == 0) {
			gestureFile.delete();
			return;
		}

		mStore.save(false);
	}

	/**
	 * dip转换为px
	 * 
	 * @param x
	 * @return
	 */
	private int dip2px(int x) {
		return DensityUtil.dip2px(EditNoteScreen.this, x);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		// mState = SOFTINPUTSTATE;
		if (requestCode == CAMERA_RESULT) {
			if (resultCode == RESULT_OK) {
				Bitmap bmp = decodeFile(new File(imageFilePath),
						myEdit.getWidth(), myEdit.getHeight());
				ImageSpan span = new ImageSpan(bmp);
				int index = myEdit.getSelectionStart();
				if (index > 0) {
					myEdit.getText().insert(index, "\n");
				}
				String pName = "pic_" + picName.getCurNum();
				addMapItem(pName);
				SpannableString spanStr = new SpannableString(pName);
				spanStr.setSpan(span, 0, spanStr.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				index = myEdit.getSelectionStart();
				myEdit.getText().insert(index, spanStr);
			}
		} else if (requestCode == ALBUM_RESULT) {
			if (data == null) {
				return;
			}
			Uri _uri = data.getData();

			// this will be null if no image was selected...
			if (_uri != null) {
				// now we get the path to the image file
				Cursor cursor = getContentResolver().query(_uri, null, null,
						null, null);

				if (cursor == null || cursor.getCount() == 0) {
					return;
				}
				cursor.moveToFirst();
				imageFilePath = cursor.getString(1);
				cursor.close();

				Bitmap bmp = decodeFile(new File(imageFilePath),
						myEdit.getWidth(), myEdit.getHeight());
				ImageSpan span = new ImageSpan(bmp);
				int index = myEdit.getSelectionStart();
				if (index > 0) {
					myEdit.getText().insert(index, "\n");
				}
				String pName = picName.generateName();
				SpannableString spanStr = new SpannableString("pic_" + pName);
				addMapItem("pic_" + pName);
				spanStr.setSpan(span, 0, spanStr.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				index = myEdit.getSelectionStart();
				myEdit.getText().insert(index, spanStr);
			}
		}
	}

	private void comPressBmp(Bitmap bmp, String fileName) {
		if (bmp == null || bmp.isRecycled()) {
			return;
		}
		File file = new File(preffix + "pic/" + fileName);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdir();
		}

		BufferedOutputStream bos;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(file));
			bmp.compress(Bitmap.CompressFormat.JPEG, 80, bos);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 在图片的map中添加元素
	 * 
	 * @param name
	 */
	private void addMapItem(String name) {
		if (mPicMap == null) {
			mPicMap = new LinkedHashMap<String, String>();
		}
		mPicMap.put(name, imageFilePath);
	}

	/**
	 * 加载图片，若图片的本身大小大于宽，高，则等比缩小图片。
	 * 
	 * @param f
	 *            图片文件
	 * @param width
	 *            图片的最大宽度
	 * @param height
	 *            图片的最大高度
	 * @return 返回一个图片
	 */
	public static Bitmap decodeFile(File f, int width, int height) {
		Log.d("=XXX=", "width = " + width + " height = " + height);
		if (width == 0 || height == 0) {
			Display display = ((MainScreen) MainScreen.mContext)
					.getWindowManager().getDefaultDisplay();
			width = display.getWidth();
			height = display.getHeight()
					- DensityUtil.dip2px(MainScreen.mContext, 145);
		}
		Bitmap retBmp = null;
		try {
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			FileInputStream fis = new FileInputStream(f);
			BitmapFactory.decodeStream(fis, null, o);
			int widthRatio = o.outWidth / width;
			int heightRatio = o.outHeight / height;

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = 1;
			if (widthRatio > 1 || heightRatio > 1) {
				o2.inSampleSize = Math.max(widthRatio, heightRatio);
			}

			fis = new FileInputStream(f);
			retBmp = BitmapFactory.decodeStream(fis, null, o2);

			float widthScale = (float) width / o2.outWidth;
			float heightScale = (float) height / o2.outHeight;

			float scale = Math.min(widthScale, heightScale);

			if (scale < 1) {
				Matrix matrix = new Matrix();
				matrix.postScale(scale, scale);
				retBmp = Bitmap.createBitmap(retBmp, 0, 0, retBmp.getWidth(),
						retBmp.getHeight(), matrix, true);
			}

			fis.close();
		} catch (Exception e) {
		}
		return retBmp;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		// super.onBackPressed();
		if (!hasChanged) {
			String[] fileNames = { preffix + "picmap", preffix + "text",
					preffix + "gesture", preffix + "graffit", preffix + "pic/" };
			deletefiles(fileNames);
			finish();
			return;
		}

		if (save_dialog == null) {
			save_dialog = new NoteSaveDialog(this);
		}
		save_dialog.show();
	}

	/**
	 * 初始化GridView
	 */
	public void makeAdapters() {
		faceAdapter = new FaceAdapter(this);
	}

	private void initFaceAdapter() {
		makeAdapters();
		GridView faceGridview = (GridView) facechoose_dialog
				.findViewById(R.id.face_gridview);
		faceGridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				int viewId = (Integer) ((FrameLayout) view).getChildAt(0)
						.getTag();
				// View childView = ((FrameLayout) view).getChildAt(1);
				/*
				 * for(int index = 0; index < faceAdapter.getCount(); index++){
				 * View tempView = parent.getChildAt(index); //((FrameLayout)
				 * tempView).getChildAt(1).setVisibility(View.GONE); }
				 */

				insertFace(viewId);

				facechoose_dialog.dismiss();
			}
		});
		faceGridview.setAdapter(faceAdapter);
	}

	/**
	 * 插入表情
	 * 
	 * @param id
	 *            表情id
	 */
	private void insertFace(int id) {
		String fname = this.getResources().getResourceName(id);
		fname = fname.substring(fname.lastIndexOf("/") + 1, fname.length());
		Drawable drawable = this.getResources().getDrawable(id);
		drawable.setBounds(0, 0, 48, 48);
		ImageSpan span = new ImageSpan(drawable);
		SpannableString spanStr = new SpannableString(fname);
		spanStr.setSpan(span, 0, spanStr.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		int index = myEdit.getSelectionStart();
		index = index < 0 ? 0 : index;
		myEdit.getText().insert(index, spanStr);
	}

	/**
	 * 追加表情
	 * 
	 * @param id
	 *            表情id
	 */
	private void appendFace(int id) {
		String fname = this.getResources().getResourceName(id);
		fname = fname.substring(fname.lastIndexOf("/") + 1, fname.length());
		Drawable drawable = this.getResources().getDrawable(id);
		drawable.setBounds(0, 0, 48, 48);
		ImageSpan span = new ImageSpan(drawable);
		SpannableString spanStr = new SpannableString(fname);
		spanStr.setSpan(span, 0, spanStr.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		myEdit.getText().append(spanStr);
	}

	@Override
	public boolean onEvent(Object sender, EventArgs e) {
		// TODO Auto-generated method stub
		switch (e.getType()) {
		case SHARE_NOTE_SUCCESSED:
			dismissProgress();
			finish();
			break;
		case SHARE_NOTE_FAILED:
			dismissProgress();
			finish();
			break;
		case NOTE_PIC_SAVE_OVER:
			isPicSaveOver = true;
			shareNote2Square();
			saveOverAndFinish();
			break;
		case NOTE_TO_BE_SHARE:
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (!EditNoteScreen.this.isFinishing()) {
						showProgress(null, getString(R.string.saving_note_now));
					}
				}
			});
			isNoteTobeShare = true;
			mShareNoteId = (String) e.getExtra("noteid");
			mShareNoteTitle = (String) e.getExtra("title");
			mShareNoteAction = (String) e.getExtra("action");
			mShareNoteSid = (String) e.getExtra("sid");
			shareNote2Square();
			break;
		case NOTE_SAVE_OVER:
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (!EditNoteScreen.this.isFinishing()) {
						showProgress(null, getString(R.string.saving_note_now));
					}
				}
			});
			isNoteSaveOver = true;
			saveOverAndFinish();
			break;
		}
		return false;
	}

	private void shareNote2Square() {
		if (isPicSaveOver && isNoteTobeShare) {
			dismissProgress();
			Intent intent = new Intent(this, ShareScreen.class);
			intent.putStringArrayListExtra("picpathlist", mPicPathList);
			intent.putExtra("noteid", mShareNoteId);
			intent.putExtra("title", mShareNoteTitle);
			intent.putExtra("action", mShareNoteAction);
			intent.putExtra("sid", mShareNoteSid);
			this.startActivity(intent);
		}
	}

	private void saveOverAndFinish() {
		if (isPicSaveOver && isNoteSaveOver) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					dismissProgress();
					finish();
				}
			});
		}
	}
}