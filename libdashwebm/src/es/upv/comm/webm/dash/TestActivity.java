package es.upv.comm.webm.dash;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.libwebm.R;

public class TestActivity extends Activity {

	private Button mTestButton;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

		mTestButton = (Button) findViewById(R.id.test_button);
		mTestButton.setOnClickListener(testOnClick);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_test, menu);
		return true;
	}
	
	private OnClickListener testOnClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			try {
//				URL url = new URL("http://xolotl.iteam.upv.es/tears_of_steel_480p_muxed.webm");
				URL url = new URL("http://xolotl.iteam.upv.es/tears_of_steel.xml");
//				Stream dashStream = new Stream(getApplicationContext(), url,"0-250","1047205-1047286");
				
					Player p = new Player("http://xolotl.iteam.upv.es/tears_of_steel.xml");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	};

}
