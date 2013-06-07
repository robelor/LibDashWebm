package es.upv.comm.webm.dash.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.SystemClock;
import android.util.Log;

import es.upv.comm.webm.dash.Debug;

public class HttpUtils implements Debug {

	public static InputStream getUrlRangeInputStream(URL url, ByteRange byteRange) throws IOException {
		HttpURLConnection connection = null;
			connection = (HttpURLConnection) url.openConnection();
			String rangeProperty = byteRange.getRangeProperty();
			connection.addRequestProperty("range", rangeProperty);
			return new BufferedInputStream(connection.getInputStream());
		
	}

	public static UrlRangeDownload readUrlRange(URL url, ByteRange range) throws IOException {
		HttpURLConnection connection = null;
		BufferedInputStream bis = null;
		byte[] buffer = null;

		try {
			connection = (HttpURLConnection) url.openConnection();
			String rangeProperty = range.getRangeProperty();
			connection.addRequestProperty("range", rangeProperty);

			int buffSize = range.getRangeSize();
			buffer = new byte[buffSize];

			int readed = 0;
			boolean endReached = false;

			long t1 = SystemClock.elapsedRealtime();

			bis = new BufferedInputStream(connection.getInputStream());

			float speed = -1;
			
			while (readed < buffSize && !endReached) {

				int thisRead = bis.read(buffer, readed, buffSize - readed);
				if (thisRead > 0) {
					readed = readed + thisRead;
				} else {
					endReached = true;
				}

			}

			long t2 = SystemClock.elapsedRealtime();

			long ms = t2 - t1;

			 speed = (float) readed / ((float) ms / (float) 1000l);

			if (D)
				Log.d(LOG_TAG, HttpUtils.class.getSimpleName() + ": " + "Readed " + readed + " Bytes in " + ms + " ms  at " + speed + " Bytes/second");

			
			UrlRangeDownload download = new UrlRangeDownload(buffer, speed); 
			
			return download;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Error reading URL range");
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
	

}
