package es.upv.comm.webm.dash;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.ebml.io.InputStreamDataSource;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import es.upv.comm.webm.dash.container.WebmContainer;
import es.upv.comm.webm.dash.container.segment.cueing.Cues;
import es.upv.comm.webm.dash.util.HexByteArray;

public class WebmDashStream implements Runnable, Debug {
	private static final String RANGE_PROPERTY_PREFIX = "bytes=";

	private InitThread mInitThread;

	private Context mContext;

	private URL mUrl;
	private String mInitRange;
	private String mInitRangeProperty;
	private int mInitSize;
	private byte[] mInitialization;
	private String mIndexRange;
	private String mIndexRangeProperty;
	private int mIndexSize;
	private byte[] mIndex;

	public WebmDashStream(Context context, URL url, String initRange, String indexRange) {
		mContext = context;
		mUrl = url;
		mInitRange = initRange;
		mIndexRange = indexRange;

		mInitRangeProperty = RANGE_PROPERTY_PREFIX + mInitRange;
		mIndexRangeProperty = RANGE_PROPERTY_PREFIX + mIndexRange;

		mInitSize = getRangeSize(initRange);
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Init size: " + mInitSize);
		mIndexSize = getRangeSize(indexRange);
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Index size: " + mIndexSize);

		mInitialization = new byte[mInitSize];
		mIndex = new byte[mIndexSize];

		mInitThread = new InitThread();
		mInitThread.start();

	}

	@Override
	public void run() {

	}

	private class InitThread implements Runnable {

		private Thread mThisThread;

		public void start() {
			if (mThisThread == null) {
				mThisThread = new Thread(this);
				mThisThread.start();
			}
		}

		@Override
		public void run() {

//			// Initialization data
//			try {
//				HttpURLConnection con = (HttpURLConnection) mUrl.openConnection();
//				con.addRequestProperty("range", mInitRangeProperty);
//				readStream2(con.getInputStream(), mInitialization);
//				if (D)
//					Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + HexByteArray.bytesToHex(mInitialization));
//				
//				InputStreamDataSource isds = new InputStreamDataSource(new ByteArrayInputStream(mInitialization));
//				WebmContainer.parse(isds);
//				
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			// Index data
//			try {
//				HttpURLConnection con = (HttpURLConnection) mUrl.openConnection();
//				con.addRequestProperty("range", mIndexRangeProperty);
//				readStream2(con.getInputStream(), mIndex);
//				if (D)
//					Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + HexByteArray.bytesToHex(mIndex));
//				InputStreamDataSource isds = new InputStreamDataSource(new ByteArrayInputStream(mIndex));
//				Cues.create(isds);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			
			
			File fDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
//			File f = new File(fDir, "tos.webm");
//			File f = new File(fDir, "big_buck_bunny.webm");
			File f = new File(fDir, "tears_of_steel_480p_muxed.webm");
//			File f = new File(fDir, "tears_of_steel_480p.webm");
			try {
				WebmContainer.parse(new InputStreamDataSource(new FileInputStream(f)));
			} catch (FileNotFoundException e) { 
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void readStream2(InputStream in, byte[] buff) {
		byte[] buffer = buff;
		int buffSize = buff.length;
		BufferedInputStream bis = null;

		int readed = 0;
		boolean endReached = false;

		try {
			bis = new BufferedInputStream(in);

			while (readed < buffSize && !endReached) {

				int thisRead = bis.read(buffer, readed, buffSize - readed);
				if (thisRead > 0) {
					readed = readed + thisRead;
				} else {
					endReached = true;
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static final int getRangeSize(String range) {
		int initRange = 0;
		int endRange = 0;

		String[] ranges = range.split("-");

		if (ranges.length == 2) {
			try {
				initRange = Integer.parseInt(ranges[0]);
				endRange = Integer.parseInt(ranges[1]);
				endRange--;
			} catch (NumberFormatException e) {
				throw new NumberFormatException("Incorrect range");
			}
		} else {
			throw new NumberFormatException("Incorrect range");
		}

		return (endRange - initRange + 1);
	}
}
