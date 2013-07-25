package es.upv.comm.webm.dash.http;

import java.io.IOException;
import java.io.InputStream;

import es.upv.comm.webm.dash.Debug;

import android.os.SystemClock;
import android.util.Log;

public class SpeedOMeterInpotStream extends InputStream implements Debug {

	private InputStream mIs;
	private int mSize;
	private int mReaded;

	private boolean started = false;
	private long t1;
	private long t2;

	private NetworkSpeedListener mNetworkSpeedListener;

	private int mStreamIndex;

	public SpeedOMeterInpotStream(InputStream is, int size, int streamIndex, NetworkSpeedListener networkSpeedListener) {
		mIs = is;
		mSize = size;
		mStreamIndex = streamIndex;
		mNetworkSpeedListener = networkSpeedListener;
	}

	@Override
	public int read() throws IOException {
		checkInit();

		int aux = mIs.read();

		if (!isEnd(aux)) {
			mReaded++;
		}
		
		checkSize();

		return aux;
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		checkInit();

		int aux = mIs.read(buffer);

		if (!isEnd(aux)) {
			mReaded += aux;
		}
		
		checkSize();

		return aux;
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		checkInit();

		int aux = mIs.read(buffer, offset, length);

		if (!isEnd(aux)) {
			mReaded += aux;
		}
		
		checkSize();

		return aux;
	}

	private void checkInit() {
		if (!started) {
			started = true;
			t1 = SystemClock.elapsedRealtime();
		}
	}

	private boolean isEnd(int result) {
		if (result == -1) {		
			return true;
		} else {
			return false;
		}
	}
	
	private void checkSize(){

		if (mReaded >= (mSize-100)) {
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "readed: " + mReaded + " size: " + mSize);
			
			t2 = SystemClock.elapsedRealtime();

			long ms = t2 - t1;

			float speed = (float) mReaded / ((float) ms / (float) 1000l);

			mNetworkSpeedListener.networkSpeed(0, mStreamIndex, speed);
		}
	}

	@Override
	public long skip(long byteCount) throws IOException {
		return mIs.skip(byteCount);
	}

}
