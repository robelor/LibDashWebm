package es.upv.comm.webm.dash;

import java.net.MalformedURLException;

import es.upv.comm.webm.dash.Player.ActionListener;
import es.upv.comm.webm.dash.buffer.BufferReport;
import es.upv.comm.webm.dash.buffer.BufferReportListener;
import es.upv.comm.webm.dash.http.NetworkSpeedListener;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class PlayerActivity extends Activity implements Debug, PlayerReproductionListener, BufferReportListener, NetworkSpeedListener {

	public static final String URL_EXTRA = "libwebm_dash_url_extra";
	public static final String BUFFER_SIZE = "libwebm_dash_buffer_size";
	public static final String BUFFER_MIN_SIZE = "libwebm_dash_buffer_min_size";
	public static final String BUFFER_TYPE = "libwebm_dash_buffer_type";
	public static final String ALG = "libwebm_dash_alg";

	private String mUrl;
	private int mBufferSize;
	private int mMinBufferSize;
	private int mBufferType;
	private int mAlg;
	private Player mPlayer;

	private LinearLayout mlLinearLayout;
	private TextView mTimeTextView;
	private TextView mSizeView;
	private TextView mBufferFill;
	private TextView mSpeed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_player);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		mlLinearLayout = (LinearLayout) findViewById(R.id.video_layout);
		mTimeTextView = (TextView) findViewById(R.id.timeView);
		mSizeView = (TextView) findViewById(R.id.sizeView);
		mBufferFill = (TextView) findViewById(R.id.bufferFillView);
		mSpeed = (TextView)findViewById(R.id.SpeedView);
		

		Intent i = getIntent();
		mUrl = i.getStringExtra(URL_EXTRA);
		mBufferSize = i.getIntExtra(BUFFER_SIZE, 10000);
		mMinBufferSize = i.getIntExtra(BUFFER_MIN_SIZE, 5000);
		mBufferType = i.getIntExtra(BUFFER_TYPE, Player.BUFFER_SYNC);
		mAlg = i.getIntExtra(ALG, Player.ALG_LOWER_BITRATE);

		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Video URL: " + mUrl);
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Video Buffer Type: " + mBufferType);
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Video Adaptation Alg: " + mAlg);

	}

	@Override
	protected void onResume() {
		super.onResume();

		

		mPlayer = new Player(getApplicationContext());
		mPlayer.setPlayerReproductionListener(this);
		mPlayer.setBufferReportListener(this);
		mPlayer.setmNetworkSpeedListener(this);
		try {
			mPlayer.setDataSource(mUrl);
			mPlayer.setBufferSize(mBufferSize);
			mPlayer.setMinBufferSize(mMinBufferSize);
			mPlayer.setBufferType(mBufferType);
			mPlayer.setAdaptationAlg(mAlg);
			mPlayer.prepareAsync(actionListener);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (mPlayer != null) {
			mPlayer.close();
		}
	}

	protected ActionListener actionListener = new ActionListener() {

		@Override
		public void onSuccess() {

			mHandler.post(new Runnable() {

				@Override
				public void run() {

					int x = mlLinearLayout.getWidth();
					int y = (int) ((float) x / (float) mPlayer.getVideoSizeRatio());
					LayoutParams lp = new LayoutParams(x, y);
					mlLinearLayout.addView(mPlayer.getVideoView(), lp);
				}
			});

			mPlayer.play();
		}

		@Override
		public void onFailure(int error) {

		}
	};

	private Handler mHandler = new Handler();

	@Override
	public void playerReproductionTime(final int time) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mTimeTextView.setText(time + "");
			}
		});
	}

	@Override
	public void playerQualityChange(final int x, final int y) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mSizeView.setText(x + "x" + y);
			}
		});
	}

	@Override
	public void bufferReport(final BufferReport bufferReport) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mBufferFill.setText(bufferReport.getBufferUsage() + "");
			}
		});
	}

	@Override
	public void networkSpeed(int streamIndex, int index, final float speed) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				
				
				mSpeed.setText(speed + "");
			}
		});
		
	}

}
