package es.upv.comm.webm.dash.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import android.util.Log;

import es.upv.comm.webm.dash.Debug;

public class AsyncInputStream3 extends InputStream implements Debug, Runnable {

	private Thread thisThread;

	private BufferedInputStream bis;
	private final int size;

	private PipedOutputStream pos;
	private PipedInputStream pis;
	private int pisIndex;

	public AsyncInputStream3(InputStream inputStream, int size) {

		this.size = size;

		bis = new BufferedInputStream(inputStream);

		try {
			pis = new PipedInputStream(size);
			pos = new PipedOutputStream(pis);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void start() {
		if (thisThread == null) {
			thisThread = new Thread(this);
			thisThread.setName("AsyncInputStream");
			thisThread.setDaemon(true);
			thisThread.start();
		}
	}

	public void stop() {
		if (thisThread != null) {
			thisThread = null;
		}
	}

	@Override
	public int read() throws IOException {
		if (pisIndex == size) {
			return -1;
		}
		int r = pis.read();
		pisIndex++;
		return r;
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		if (pisIndex == size) {
			return -1;
		}

		int r = pis.read(buffer, offset, length);
		pisIndex += r;

		return r;
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		if (pisIndex == size) {
			return -1;
		}

		int r = pis.read(buffer);
		pisIndex += r;

		return r;
	}

	@Override
	public long skip(long byteCount) throws IOException {
		long r = pis.skip(byteCount);

		pisIndex += (int) r;

		return r;
	}

	@Override
	public void run() {

		int b;

		try {
			while ((b = (byte) bis.read()) != -1) {
				pos.write(b);
			}
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
