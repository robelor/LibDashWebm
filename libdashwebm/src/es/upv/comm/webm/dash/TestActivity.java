package es.upv.comm.webm.dash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TestActivity extends Activity implements Debug {

	private Button mTestButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

		mTestButton = (Button) findViewById(R.id.test_button);
		mTestButton.setOnClickListener(testOnClick);
	}

	private OnClickListener testOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent i = new Intent(getApplicationContext(), PlayerActivity.class);
			i.putExtra(PlayerActivity.URL_EXTRA, "http://172.16.0.100/tos/tos.xml");
			startActivity(i);
		}
	};

}
