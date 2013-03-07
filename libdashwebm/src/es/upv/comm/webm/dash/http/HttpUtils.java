package es.upv.comm.webm.dash.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpUtils {

	private static final String RANGE_PROPERTY_PREFIX = "bytes=";

	public InputStream getUrlRangeInputStream(URL url, int initByte, int endByte) {
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			String rangeProperty = RANGE_PROPERTY_PREFIX + initByte + "-" + endByte;
			connection.addRequestProperty("range", rangeProperty);
			return new BufferedInputStream(connection.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] readUrlRange(URL url, ByteRange range) {
		HttpURLConnection connection = null;
		BufferedInputStream bis = null;
		byte[] buffer = null;

		try {
			connection = (HttpURLConnection) url.openConnection();
			String rangeProperty = RANGE_PROPERTY_PREFIX + range.getInitByte() + "-" + range.getEndByte();
			connection.addRequestProperty("range", rangeProperty);

			int buffSize = range.getRangeSize();
			buffer = new byte[buffSize];

			int readed = 0;
			boolean endReached = false;

			bis = new BufferedInputStream(connection.getInputStream());

			while (readed < buffSize && !endReached) {

				int thisRead = bis.read(buffer, readed, buffSize - readed);
				if (thisRead > 0) {
					readed = readed + thisRead;
				} else {
					endReached = true;
				}

			}

			return buffer;
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

		return null;
	}

}
