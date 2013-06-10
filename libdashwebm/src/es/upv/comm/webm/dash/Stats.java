package es.upv.comm.webm.dash;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import es.upv.comm.webm.dash.buffer.BufferReport;
import es.upv.comm.webm.dash.buffer.BufferReportListener;
import es.upv.comm.webm.dash.http.NetworkSpeedListener;

import android.content.Context;
import android.os.Environment;
import android.os.SystemClock;

public class Stats implements BufferReportListener, PresentationTimeListener, PresentationQualityListener, NetworkSpeedListener {

	private static final String BUFFER_FILE_SUFIX = "buffer";
	private static final String PRESENTATION_TIME_SUFIX = "presetation_time";
	private static final String PRESENTATION_QUALITY_SUFIX = "presetation_quality";
	private static final String NETWORK_QUALITY_SUFIX = "network";

	private Context mContext;
	private boolean mStore = false;

	private boolean mDoBuffStats;
	private File mBuffFile;
	private FileOutputStream mBuffOutputStream;

	private boolean mDoPresentationTimeStats;
	private File mPresentationTimeFile;
	private FileOutputStream mPresentationTimeOutputStream;

	private boolean mDoPresentationQualityStats;
	private File mPresentationQualityFile;
	private FileOutputStream mPresentationQualityOutputStream;

	private boolean mDoNetworkStats;
	private File mNetworkFile;
	private FileOutputStream mNetworkOutputStream;

	private long mStartTime;

	public Stats(Context context) {

		mContext = context;

		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}

		if (mExternalStorageAvailable && mExternalStorageWriteable) {
			mStore = true;

			File appDir = mContext.getExternalFilesDir(null);

			Calendar calendar = Calendar.getInstance();
			int yyyy = calendar.get(Calendar.YEAR);
			int mm = calendar.get(Calendar.MONTH);
			int dd = calendar.get(Calendar.DAY_OF_MONTH);

			int hh = calendar.get(Calendar.HOUR_OF_DAY);
			int min = calendar.get(Calendar.MINUTE);

			// String fileNameBase = "" + yyyy + mm + dd + "-";

			String fileNameBase = String.format("%04d%02d%02d-%02d%02d-", yyyy, mm, dd, hh, min);

			try {

				String buffFileName = fileNameBase + BUFFER_FILE_SUFIX;
				mBuffFile = new File(appDir, buffFileName);
				mBuffOutputStream = new FileOutputStream(mBuffFile);
				mDoBuffStats = true;

				String presentationTimeFileName = fileNameBase + PRESENTATION_TIME_SUFIX;
				mPresentationTimeFile = new File(appDir, presentationTimeFileName);
				mPresentationTimeOutputStream = new FileOutputStream(mPresentationTimeFile);
				mDoPresentationTimeStats = true;

				String presentationQualityFileName = fileNameBase + PRESENTATION_QUALITY_SUFIX;
				mPresentationQualityFile = new File(appDir, presentationQualityFileName);
				mPresentationQualityOutputStream = new FileOutputStream(mPresentationQualityFile);
				mDoPresentationQualityStats = true;

				String networkFileName = fileNameBase + NETWORK_QUALITY_SUFIX;
				mNetworkFile = new File(appDir, networkFileName);
				mNetworkOutputStream = new FileOutputStream(mNetworkFile);
				mDoNetworkStats = true;

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		mStartTime = SystemClock.elapsedRealtime();

	}

	@Override
	public void bufferReport(BufferReport bufferReport) {

		if (mDoBuffStats) {
			int buf = bufferReport.getBufferUsage();
			if (buf > 100)
				buf = 0;
			try {
				mBuffOutputStream.write(((SystemClock.elapsedRealtime() - mStartTime) + "\t" + buf + "\n").getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public void presentationTime(int presentationTime) {
		if (mDoPresentationTimeStats) {
			try {
				mPresentationTimeOutputStream.write(((SystemClock.elapsedRealtime() - mStartTime) + "\t" + presentationTime + "\n").getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void presentationQuality(int presentationQuality) {
		if (mDoPresentationQualityStats) {
			try {
				mPresentationQualityOutputStream.write(((SystemClock.elapsedRealtime() - mStartTime) + "\t" + presentationQuality + "\n").getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void networkSpeed(int streamIndex, int index, float speed) {
		if (mDoNetworkStats) {
			try {
				mNetworkOutputStream.write(((SystemClock.elapsedRealtime() - mStartTime) + "\t" + index + "\t" + streamIndex + "\t" + speed + "\n").getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void close() {
		if (mDoBuffStats) {
			try {
				mBuffOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
