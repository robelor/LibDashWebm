package es.upv.comm.webm.dash;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import es.upv.comm.webm.dash.mpd.Mpd;

public class Player {

	private String mUrl;

	private String mXml;

	private Stream mAudioSream;
	private Stream[] mVideoStreams;

	public Player(String url) {
		mUrl = url;

		new Thread(new Runnable() {

			@Override
			public void run() {
				mXml = getXmlFromUrl(mUrl);
				System.out.println(mXml);
				Mpd mpd = new Mpd(mXml);

			}
		}).start();

	}

	public String getXmlFromUrl(String url) {
		String xml = null;

		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			xml = EntityUtils.toString(httpEntity);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// return XML
		return xml;
	}

}
