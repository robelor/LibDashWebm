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
import android.media.MediaFormat;
import android.media.MediaCodec.BufferInfo;
import android.os.SystemClock;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.Surface;
import es.upv.comm.webm.dash.adaptation.AdaptationManager;
import es.upv.comm.webm.dash.buffer.Buffer;
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
	private Stream[] mVideoStreams;
	private int mCurrentVideoStream = -1;

	private Surface mSurface;

	private AdaptationManager mAdaptationManager;

	private Buffer mBuffer;

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

					// mAudioSream = new Stream(mAudioAdaptationSet.getFirstRepresentation(), mBaseUrl);

					// MediaExtractor meA = new MediaExtractor();
					// meA.setDataSource(mAudioSream.getStreamUrl().toString());

					// video streams
					mVideoStreams = new Stream[mVideoAdaptationSet.getRepresentations().size()];
					int vi = 0;
					for (Representation representation : mVideoAdaptationSet.getRepresentations()) {
						Stream videoStream = new Stream(representation, mBaseUrl);

						mVideoStreams[vi++] = videoStream;
					}

					mBuffer = new Buffer(mVideoStreams, 10000, 5000, mAdaptationManager);

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


		VideoThread vt = new VideoThread(mVideoStreams, mBuffer, mSurface);
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

		private Stream[] mStreams;
		private Buffer mBuffer;
		private Surface mSurface;

		private MediaCodec[] mVideoCodec;
		private ByteBuffer[][] mVideoCodecInputBuffers = null;
		private ByteBuffer[][] mVideoCodecOutputBuffers = null;

		public VideoThread(Stream[] videoStreams, Buffer buffer, Surface surface) {
			mStreams = videoStreams;
			mBuffer = buffer;
			mSurface = surface;
			
			mVideoCodec = new MediaCodec[mStreams.length];

			for (int i = 0; i < mStreams.length; i++) {
				
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Creating Codec for  " + i);
				
				
				Stream stream = mStreams[i];
				MediaFormat mf = stream.getStreamFormat();
				
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Creating Codec with mediaformat  " + mf);
				
				mVideoCodec[i] = MediaCodec.createByCodecName(mf.getString(MediaFormat.KEY_MIME));
				mVideoCodec[i].configure(mf, mSurface, null, 0);
				mVideoCodec[i].start();
				mVideoCodecInputBuffers[i] = mVideoCodec[i].getInputBuffers();
				mVideoCodecOutputBuffers[i] = mVideoCodec[i].getOutputBuffers();

				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Video Codec: " + mVideoCodec[i].toString());

			}

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

			boolean eos = false;

			while (Thread.currentThread() == mVideoThread && !sawInputEOS && !sawOutputEOS) {

				long presentationTimeUs = -1;

				eos = !mBuffer.advance();

				if (!eos) {
					int si = mBuffer.getStreamIndex();

					// input buffer
					int inputBufIndex = mVideoCodec[si].dequeueInputBuffer(20000);
					if (inputBufIndex >= 0) {

						ByteBuffer dstBuf = mVideoCodecInputBuffers[si][inputBufIndex];
						int sampleSize = mBuffer.readSampleData(dstBuf, 0);

						presentationTimeUs = 0;
						if (sampleSize < 0) {
							sawInputEOS = true;
							sampleSize = 0;
						} else {
							presentationTimeUs = mBuffer.getSampleTime();
							if (mStartTime < 0) {
								mStartTime = SystemClock.elapsedRealtime();
							}
						}

						mVideoCodec[si].queueInputBuffer(inputBufIndex, 0, sampleSize, presentationTimeUs, sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM
								: 0);
						if (!sawInputEOS) {
							mBuffer.advance();
						}
					}

					while ((SystemClock.elapsedRealtime() - mStartTime) < presentationTimeUs) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					// output buffer

					BufferInfo info = new BufferInfo();
					final int res = mVideoCodec[si].dequeueOutputBuffer(info, 20000);
					if (res >= 0) {
						int outputBufIndex = res;
						ByteBuffer buf = mVideoCodecOutputBuffers[si][outputBufIndex];

						final byte[] chunk = new byte[info.size];
						buf.get(chunk); // Read the buffer all at once
						buf.clear(); // ** MUST DO!!! OTHERWISE THE NEXT TIME YOU GET THIS SAME BUFFER BAD THINGS WILL HAPPEN

						// if (chunk.length > 0) {
						// audioTrack.write(chunk, 0, chunk.length);
						// }
						mVideoCodec[si].releaseOutputBuffer(outputBufIndex, true /* render */);

						if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
							sawOutputEOS = true;
						}
					} else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
						mVideoCodecOutputBuffers[si] = mVideoCodec[si].getOutputBuffers();
					} else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
						final MediaFormat oformat = mVideoCodec[si].getOutputFormat();
						Log.d(LOG_TAG, "Output format has changed to " + oformat);
					}

				}

			}

			// close
			
			for (int i = 0; i < mStreams.length; i++) {
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Releasing VideoCodec");
				mVideoCodec[i].release();
			}

			

		}
	}

}
