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

import android.media.MediaRecorder.VideoSource;
import android.util.Log;

import es.upv.comm.webm.dash.mpd.AdaptationSet;
import es.upv.comm.webm.dash.mpd.Mpd;
import es.upv.comm.webm.dash.mpd.Representation;

public class Player implements Debug{

	private String mUrl;
	private String mBaseUrl;

	private String mXml;
	
	private Mpd mpd; 

	private AdaptationSet mAudioAdaptationSet;
	private AdaptationSet mVideoAdaptationSet;
	
	private Stream mAudioSream;
	private ArrayList<Stream> mVideoStreams = new ArrayList<Stream>();

	public Player(String url) {
		mUrl = url;
		
		try {
			URL aux = new URL(mUrl);
			String baseUrl =aux.getProtocol()+"://"+aux.getHost()+aux.getPath();
			baseUrl = baseUrl.substring(0, baseUrl.length()-(aux.getFile().length()-1));
			mBaseUrl = baseUrl;
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Base URL: "+ mBaseUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				mXml = getXmlFromUrl(mUrl);
				System.out.println(mXml);
				mpd = new Mpd(mXml);
				
				// sets
				mAudioAdaptationSet = mpd.getAdaptationSet(AdaptationSet.Type.Audio);
				mVideoAdaptationSet = mpd.getAdaptationSet(AdaptationSet.Type.Video);
				
				// audio stream
				mAudioSream = new Stream(mAudioAdaptationSet.getFirstRepresentation(),mBaseUrl);
				
				// video streams
				for (Representation representation : mVideoAdaptationSet.getRepresentations()) {
					Stream videoStream = new Stream(representation, mBaseUrl);
					mVideoStreams.add(videoStream);
				}
				
				Stream vs = mVideoStreams.get(0);
				if(vs!=null){
					vs.getNextBlock();
				}

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
