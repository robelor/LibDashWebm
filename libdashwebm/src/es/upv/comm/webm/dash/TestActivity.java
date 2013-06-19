package es.upv.comm.webm.dash;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class TestActivity extends Activity implements Debug {

	private static final String SETTING_URL = "url";
	private static final String SETTING_BUFFER_SIZE = "buffer_size";
	private static final String SETTING_BUFFER_MIN_SIZE = "min_buffer_size";
	private static final String SETTING_BUFFER_TYPE = "buffer_type";
	private static final String SETTING_ALG = "alg";

	private TextView mUrlTextView;
	private TextView mBufferSizeTextView;
	private TextView mMinBufferSizeTextView;

	private RadioGroup mBuffTypeGroup;
	private RadioButton mRadioButton0;
	private RadioButton mRadioButton1;

	private RadioGroup mAlgGroup;
	private RadioButton mRadioButton2;
	private RadioButton mRadioButton3;
	private RadioButton mRadioButton4;

	private Button mTestButton;

	private SharedPreferences settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

		settings = getPreferences(MODE_PRIVATE);

		mUrlTextView = (TextView) findViewById(R.id.video_url);
		mUrlTextView.setText(settings.getString(SETTING_URL, "http://172.16.0.100/ed/ed.xml"));
		mUrlTextView.addTextChangedListener(mUrlTextViewTextChangeListener);

		mBufferSizeTextView = (TextView) findViewById(R.id.buffer_size);
		mBufferSizeTextView.setText(settings.getString(SETTING_BUFFER_SIZE, "10") + "");
		mBufferSizeTextView.addTextChangedListener(mBufferSizeTextViewChangeListener);
		
		mMinBufferSizeTextView = (TextView) findViewById(R.id.min_buffer_size);
		mMinBufferSizeTextView.setText(settings.getString(SETTING_BUFFER_MIN_SIZE , "5") + "");
		mMinBufferSizeTextView.addTextChangedListener(mMinBufferSizeTextViewChangeListener);

		mBuffTypeGroup = (RadioGroup) findViewById(R.id.buff_type_group);
		mRadioButton0 = (RadioButton) findViewById(R.id.radio0);
		mRadioButton1 = (RadioButton) findViewById(R.id.radio1);
		mBuffTypeGroup.setOnCheckedChangeListener(mBuffTypeGroupOnCheckedChangeListener);
		int aux = settings.getInt(SETTING_BUFFER_TYPE, Player.BUFFER_SYNC);
		switch (aux) {
		case Player.BUFFER_SYNC:
			mRadioButton0.setChecked(true);
			break;
		case Player.BUFFER_ASYNC:
			mRadioButton1.setChecked(true);
			break;

		}

		mAlgGroup = (RadioGroup) findViewById(R.id.alg_group);
		mRadioButton2 = (RadioButton) findViewById(R.id.radio2);
		mRadioButton3 = (RadioButton) findViewById(R.id.radio3);
		mRadioButton4 = (RadioButton) findViewById(R.id.radio4);
		mAlgGroup.setOnCheckedChangeListener(mAlgGroupOnCheckedChangeListener);
		aux = settings.getInt(SETTING_ALG, Player.ALG_LOWER_BITRATE);
		switch (aux) {
		case Player.ALG_LOWER_BITRATE:
			mRadioButton2.setChecked(true);
			break;
		case Player.ALG_MULLER:
			mRadioButton3.setChecked(true);
			break;
		case Player.ALG_LOOk_AHEAD:
			mRadioButton4.setChecked(true);
			break;

		}

		mTestButton = (Button) findViewById(R.id.test_button);
		mTestButton.setOnClickListener(testOnClick);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			System.exit(0);
		}
		return super.onKeyDown(keyCode, event);
	}

	private OnClickListener testOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {

			String url = mUrlTextView.getText().toString();
			int bufferType = settings.getInt(SETTING_BUFFER_TYPE, Player.BUFFER_SYNC);
			int alg = settings.getInt(SETTING_ALG, Player.ALG_LOWER_BITRATE);
			int buffSize = Integer.parseInt(mBufferSizeTextView.getText().toString()) *1000;
			int minBuffSize = Integer.parseInt(mMinBufferSizeTextView.getText().toString()) *1000;

			Intent i = new Intent(getApplicationContext(), PlayerActivity.class);
			i.putExtra(PlayerActivity.URL_EXTRA, url);
			i.putExtra(PlayerActivity.BUFFER_SIZE, buffSize);
			i.putExtra(PlayerActivity.BUFFER_MIN_SIZE, minBuffSize);
			i.putExtra(PlayerActivity.BUFFER_TYPE, bufferType);
			i.putExtra(PlayerActivity.ALG, alg);

			// i.putExtra(PlayerActivity.URL_EXTRA, "http://172.16.0.100/tos/tos.xml");
			// i.putExtra(PlayerActivity.URL_EXTRA, "http://172.16.0.100/ed/ed.xml");
			// i.putExtra(PlayerActivity.URL_EXTRA, "http://xolotl.iteam.upv.es/tos/tos.xml");
			// i.putExtra(PlayerActivity.URL_EXTRA, "http://xolotl.iteam.upv.es/ed/ed.xml");
			startActivity(i);
		}
	};

	private TextWatcher mUrlTextViewTextChangeListener = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			Editor ed = settings.edit();
			ed.putString(SETTING_URL, mUrlTextView.getText().toString());
			ed.commit();
		}
	};

	private TextWatcher mBufferSizeTextViewChangeListener = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			Editor ed = settings.edit();
			ed.putString(SETTING_BUFFER_SIZE, mBufferSizeTextView.getText().toString());
			ed.commit();
		}
	};
	
	private TextWatcher mMinBufferSizeTextViewChangeListener = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			Editor ed = settings.edit();
			ed.putString(SETTING_BUFFER_MIN_SIZE, mMinBufferSizeTextView.getText().toString());
			ed.commit();
		}
	};

	private OnCheckedChangeListener mBuffTypeGroupOnCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.radio0:
				if (D)
					Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Checked Buffer Type: Sync");
				Editor ed1 = settings.edit();
				ed1.putInt(SETTING_BUFFER_TYPE, Player.BUFFER_SYNC);
				ed1.commit();
				break;

			case R.id.radio1:
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Checked Buffer Type: Async");
				Editor ed2 = settings.edit();
				ed2.putInt(SETTING_BUFFER_TYPE, Player.BUFFER_ASYNC);
				ed2.commit();
				break;
			}

		}
	};

	private OnCheckedChangeListener mAlgGroupOnCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.radio2:
				if (D)
					Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Checked Alg: Lower bitrate");
				Editor ed1 = settings.edit();
				ed1.putInt(SETTING_ALG, Player.ALG_LOWER_BITRATE);
				ed1.commit();
				break;

			case R.id.radio3:
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Checked Alg: Muller");
				Editor ed2 = settings.edit();
				ed2.putInt(SETTING_ALG, Player.ALG_MULLER);
				ed2.commit();
				break;

			case R.id.radio4:
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Checked Alg: Look ahead");
				Editor ed3 = settings.edit();
				ed3.putInt(SETTING_ALG, Player.ALG_LOOk_AHEAD);
				ed3.commit();
				break;
			}

		}
	};

}
