package es.upv.comm.webm.dash;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import es.upv.comm.webm.dash.mpd.AdaptationSet;
import es.upv.comm.webm.dash.mpd.Mpd;
import es.upv.comm.webm.dash.mpd.Representation;

public class Player implements Debug {

	private URL mUrl;
	private URL mBaseUrl;

	private String mXml;

	private Mpd mpd;

	private AdaptationSet mAudioAdaptationSet;
	private AdaptationSet mVideoAdaptationSet;

	private Stream mAudioSream;
	private ArrayList<Stream> mVideoStreams = new ArrayList<Stream>();

	public Player(String url) {

		try {
			mUrl = new URL(url);
			String baseUrl = mUrl.getProtocol() + "://" + mUrl.getHost() + mUrl.getPath();
			baseUrl = baseUrl.substring(0, baseUrl.length() - (mUrl.getFile().length() - 1));
			mBaseUrl = new URL(baseUrl);
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Base URL: " + mBaseUrl);

			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						mXml = getXmlFromUrl(mUrl);
						mpd = new Mpd(mXml);

						// sets
						mAudioAdaptationSet = mpd.getAdaptationSet(AdaptationSet.Type.Audio);
						mVideoAdaptationSet = mpd.getAdaptationSet(AdaptationSet.Type.Video);

						// audio stream

						mAudioSream = new Stream(mAudioAdaptationSet.getFirstRepresentation(), mBaseUrl);

						// video streams
						for (Representation representation : mVideoAdaptationSet.getRepresentations()) {
							Stream videoStream = new Stream(representation, mBaseUrl);
							mVideoStreams.add(videoStream);
						}

						Stream vs = mVideoStreams.get(0);
						if (vs != null) {
							vs.getNextBlock();
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}

				}
			}).start();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}

	public String getXmlFromUrl(URL url) {
		String xml = null;

		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url.toString());

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
