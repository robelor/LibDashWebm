package es.iteam.comm.webm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.ebml.io.InputStreamDataSource;

import android.content.Context;
import android.os.Environment;
import es.iteam.comm.webm.container.WebmContainer;

public class WebmDashStream implements Runnable {
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
		System.out.println("Init size: " + mInitSize);
		mIndexSize = getRangeSize(indexRange);
		System.out.println("Index size: " + mIndexSize);

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
//				 System.out.println(HexByteArray.bytesToHex(mInitialization));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

//			// Index data
//			try {
//				HttpURLConnection con = (HttpURLConnection) mUrl.openConnection();
//				con.addRequestProperty("range", mIndexRangeProperty);
//				readStream2(con.getInputStream(), mIndex);
//				System.out.println(HexByteArray.bytesToHex(mIndex));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			
			
			File fDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
			File f = new File(fDir, "tos.webm");
//			File f = new File(fDir, "big_buck_bunny.webm");
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
			} catch (NumberFormatException e) {
				throw new NumberFormatException("Incorrect range");
			}
		} else {
			throw new NumberFormatException("Incorrect range");
		}

		return (endRange - initRange + 1);
	}
}
