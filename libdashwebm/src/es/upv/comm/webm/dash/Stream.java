package es.upv.comm.webm.dash;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.ebml.io.InputStreamDataSource;

import android.util.Log;
import es.upv.comm.webm.dash.container.Container;
import es.upv.comm.webm.dash.container.segment.cueing.Cues;
import es.upv.comm.webm.dash.mpd.Representation;
import es.upv.comm.webm.dash.util.HexByteArray;

public class Stream implements Debug {
	private static final String RANGE_PROPERTY_PREFIX = "bytes=";

	private URL mUrl;

	private String mInitRange;
	private String mInitRangeProperty;
	private int mInitSize;
	private byte[] mInitialization;

	private String mIndexRange;
	private String mIndexRangeProperty;
	private int mIndexSize;
	private byte[] mIndex;

	private Container mContainer;

	public Stream(Representation representation, String baseUrl) {
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Creating Stream...");

		String urlString = baseUrl + representation.getBaseUrl();
		try {
			mUrl = new URL(urlString);

			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "  Stream URL" + urlString);

			mInitRange = representation.getInitRange();
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "  Init index: " + mInitRange);
			mInitSize = getRangeSize(mInitRange);
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "   Init index size: " + mInitSize);

			mIndexRange = representation.getIndexRange();
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "  Cueing index: " + mIndexRange);
			mIndexSize = getRangeSize(mIndexRange);
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "   Cueing size: " + mIndexSize);

			mInitRangeProperty = RANGE_PROPERTY_PREFIX + mInitRange;
			mIndexRangeProperty = RANGE_PROPERTY_PREFIX + mIndexRange;

			mInitialization = new byte[mInitSize];
			mIndex = new byte[mIndexSize];

			// Initialization data
			try {
				HttpURLConnection con = (HttpURLConnection) mUrl.openConnection();
				con.addRequestProperty("range", mInitRangeProperty);
				readStream2(con.getInputStream(), mInitialization);
				if (D)
					Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + HexByteArray.bytesToHex(mInitialization));

				InputStreamDataSource isds = new InputStreamDataSource(new ByteArrayInputStream(mInitialization));
				mContainer = Container.parse(isds);
				
				if (D)
					Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " +"kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk"+mContainer.getSegment().getSegmentOffset());

			} catch (IOException e) {
				e.printStackTrace();
			}

			// Index data
			try {
				HttpURLConnection con = (HttpURLConnection) mUrl.openConnection();
				con.addRequestProperty("range", mIndexRangeProperty);
				readStream2(con.getInputStream(), mIndex);
				if (D)
					Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + HexByteArray.bytesToHex(mIndex));
				InputStreamDataSource isds = new InputStreamDataSource(new ByteArrayInputStream(mIndex));
				if (mContainer != null && mContainer.getSegment() != null) {
					mContainer.getSegment().setCues(Cues.create(isds, mContainer.getSegment().getSegmentOffset()));
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
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

	public void getNextBlock() {
		if (mContainer != null && mContainer.getSegment() != null) {
			mContainer.getSegment().getNextBlock();
		}

	}
}
