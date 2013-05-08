package es.upv.comm.webm.dash;

import java.net.MalformedURLException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import es.upv.comm.webm.dash.Player.ActionListener;

public class TestActivity extends Activity implements Debug{

	private Button mTestButton;
	private LinearLayout mlLinearLayout;

	private Player p;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

		mTestButton = (Button) findViewById(R.id.test_button);
		mTestButton.setOnClickListener(testOnClick);

		mlLinearLayout = (LinearLayout) findViewById(R.id.video_layout);

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

				p = new Player(getApplicationContext());

//				p.setDataSource("http://xolotl.iteam.upv.es/tears_of_steel.xml");
				p.setDataSource("http://172.16.0.100/tos/tos.xml");

				p.prepareAsync(actionListener);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	};

	protected ActionListener actionListener = new ActionListener() {

		@Override
		public void onSuccess() {

			mHandler.post(new Runnable() {

				@Override
				public void run() {

					int x = mlLinearLayout.getWidth();
					int y = (int) ((float) x / (float) p.getVideoSizeRatio());
					LayoutParams lp = new LayoutParams(x, y);
					mlLinearLayout.addView(p.getVideoView(), lp);
				}
			});

			p.play();
		}

		@Override
		public void onFailure(int error) {

		}
	};

	private Handler mHandler = new Handler();

}
