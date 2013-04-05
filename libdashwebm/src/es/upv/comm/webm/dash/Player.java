package es.upv.comm.webm.dash;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaCodec.BufferInfo;
import android.os.SystemClock;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.Surface;
import es.upv.comm.webm.dash.adaptation.AdaptationManager;
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
	private int mCurrentVideoStream = -1;

	private Surface mSurface;
	
	private AdaptationManager mAdaptationManager;
	
	public Player() {
		mAdaptationManager = new AdaptationManager();
	}


	public void setDataSource(String url) throws MalformedURLException {

		mUrl = new URL(url);
		String baseUrl = mUrl.getProtocol() + "://" + mUrl.getHost() + mUrl.getPath();
		baseUrl = baseUrl.substring(0, baseUrl.length() - (mUrl.getFile().length() - 1));
		mBaseUrl = new URL(baseUrl);
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Base URL: " + mBaseUrl);

	}

	public void setVideoSurface(Surface surface) {
		mSurface = surface;
	}

	public void prepareAsync(final ActionListener actionListener) {

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

//					mAudioSream = new Stream(mAudioAdaptationSet.getFirstRepresentation(), mBaseUrl);

//					MediaExtractor meA = new MediaExtractor();
//					meA.setDataSource(mAudioSream.getStreamUrl().toString());

					// System.out.println("--------------------");
					// int iI = meA.getTrackCount();
					// for (int j = 0; j < iI; j++) {
					//
					// MediaFormat mf = meA.getTrackFormat(j);
					// System.out.println(mf);
					//
					// }
					// System.out.println("--------------------");

					// video streams
					for (Representation representation : mVideoAdaptationSet.getRepresentations()) {
						Stream videoStream = new Stream(representation, mBaseUrl);

						// MediaExtractor me = new MediaExtractor();
						// me.setDataSource(videoStream.getStreamUrl().toString());
						//
						// System.out.println("--------------------");
						// int i = me.getTrackCount();
						// for (int j = 0; j < i; j++) {
						//
						// MediaFormat mf = me.getTrackFormat(j);
						// System.out.println(mf);
						//
						// }
						//
						// System.out.println("--------------------");

						mVideoStreams.add(videoStream);
					}

					// prepare streams

					// mAudioSream.prepare();
					//
					 Stream vs = mVideoStreams.get(0);
					 vs.prepare();
					 
					 
						 MediaExtractor me = new MediaExtractor();
						 me.setDataSource(vs.getStreamUrl().toString());

				

					actionListener.onSuccess();

				} catch (MalformedURLException e) {
					e.printStackTrace();
					actionListener.onFailure(-1);
				} catch (IOException e) {
					e.printStackTrace();
					actionListener.onFailure(-1);
				}

			}
		}).start();

	}

	public void play() {
//		new Thread(new Runnable() {
//
//			long startTime = SystemClock.elapsedRealtime();
//
//			@Override
//			public void run() {
//
//
//					Stream vs = mVideoStreams.get(0);
//					
//					boolean go = true;
//
//					do {
//
//						vs.readSampleData(null, 0);
//						go = vs.advance();
//
//						int t = vs.getSampleTime();
//
//						while ((SystemClock.elapsedRealtime() - startTime) < t) {
//							try {
//								Thread.sleep(1);
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
//
//						// System.out.println("-------->"+t +"  -  "+(SystemClock.elapsedRealtime() - startTime));
//
//					} while (go);
//
//			}
//		}).start();
		
		Stream vs = mVideoStreams.get(0);
		MediaFormat mf = vs.getStreamFormat();
		
		
		
		
		VideoThread vt = new VideoThread(vs, mf, mSurface);
		vt.start();
		
		
	}

	public String getXmlFromUrl(URL url) {
		String xml = null;

		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url.toString());

			HttpResponse httpResponse = httpClient.execute(httpGet);
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

	public interface ActionListener {

		public void onFailure(int error);

		public void onSuccess();

	}

	class VideoThread implements Runnable {
		private Thread mVideoThread;
		
		private long mStartTime = -1;

		private Stream mStream;
		private MediaFormat mVideoFormat;
		private Surface mSurface;

		private String mVideoMime;
		private MediaCodec mVideoCodec;
		private ByteBuffer[] mVideoCodecInputBuffers = null;
		private ByteBuffer[] mVideoCodecOutputBuffers = null;
		
		private MediaExtractor mMediaExtractor;

		public VideoThread(Stream stream, MediaFormat videoFormat, Surface surface) {
			mStream = stream;
			mVideoFormat = videoFormat;
			mSurface = surface;
			
			// media extractor
//			mMediaExtractor = new MediaExtractor();
//			mMediaExtractor.setDataSource(mStream.getStreamUrl().toString());
//			mVideoFormat = mMediaExtractor.getTrackFormat(0);
//			System.out.println("----------------");
//			System.out.println(mVideoFormat.toString());
//			System.out.println("----------------");
//			mMediaExtractor.selectTrack(0);

			// audio codec
			mVideoMime = mVideoFormat.getString(MediaFormat.KEY_MIME);
			mVideoCodec = MediaCodec.createDecoderByType(mVideoMime);
			mVideoCodec.configure(mVideoFormat, mSurface, null, 0);
			mVideoCodec.start();
			mVideoCodecInputBuffers = mVideoCodec.getInputBuffers();
			mVideoCodecOutputBuffers = mVideoCodec.getOutputBuffers();
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Video Codec: " + mVideoCodec.toString());

		}

		public void start() {
			if (mVideoThread == null) {
				mVideoThread = new Thread(this, "VideoThread");
				mVideoThread.setDaemon(true);
				mVideoThread.start();
			}
		}

		public void stop() {
			if (mVideoThread != null) {
				mVideoThread = null;
			}
		}

		@Override
		public void run() {

			boolean sawInputEOS = false;
			boolean sawOutputEOS = false;

			while (Thread.currentThread() == mVideoThread && !sawInputEOS && !sawOutputEOS) {

				long presentationTimeUs = -1;
				
				// input buffer
				int inputBufIndex = mVideoCodec.dequeueInputBuffer(20000);
				if (inputBufIndex >= 0) {
					ByteBuffer dstBuf = mVideoCodecInputBuffers[inputBufIndex];
					
					int sampleSize = mStream.readSampleData(dstBuf, 0);
//					System.out.println(sampleSize);
					
					presentationTimeUs = 0;
					if (sampleSize < 0) {
						sawInputEOS = true;
						sampleSize = 0;
					} else {
						presentationTimeUs = mStream.getSampleTime();
						// Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Presentation time: " + presentationTimeUs);
						if(mStartTime< 0){
							mStartTime = SystemClock.elapsedRealtime();
						}
					}

					mVideoCodec.queueInputBuffer(inputBufIndex, 0, sampleSize, presentationTimeUs, sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
					if (!sawInputEOS) {
						mStream.advance();
					}
				}

				
				while ((SystemClock.elapsedRealtime() - mStartTime) < presentationTimeUs) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
				// output buffer

				BufferInfo info = new BufferInfo();
				final int res = mVideoCodec.dequeueOutputBuffer(info, 20000);
				if (res >= 0) {
					int outputBufIndex = res;
					ByteBuffer buf = mVideoCodecOutputBuffers[outputBufIndex];

					final byte[] chunk = new byte[info.size];
					buf.get(chunk); // Read the buffer all at once
					buf.clear(); // ** MUST DO!!! OTHERWISE THE NEXT TIME YOU GET THIS SAME BUFFER BAD THINGS WILL HAPPEN

					// if (chunk.length > 0) {
					// audioTrack.write(chunk, 0, chunk.length);
					// }
					mVideoCodec.releaseOutputBuffer(outputBufIndex, true /* render */);

					if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
						sawOutputEOS = true;
					}
				} else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					mVideoCodecOutputBuffers = mVideoCodec.getOutputBuffers();
				} else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					final MediaFormat oformat = mVideoCodec.getOutputFormat();
					Log.d(LOG_TAG, "Output format has changed to " + oformat);
				}
			}

			// close

			if (mVideoCodec != null) {
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Releasing VideoCodec");
				mVideoCodec.release();
			}


		}
	}

}
