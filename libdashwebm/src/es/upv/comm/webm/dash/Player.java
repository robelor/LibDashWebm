package es.upv.comm.webm.dash;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import es.upv.comm.webm.dash.adaptation.AdaptationManager;
import es.upv.comm.webm.dash.adaptation.BufferBasedAdaptationManager;
import es.upv.comm.webm.dash.buffer.Buffer;
import es.upv.comm.webm.dash.buffer.BufferReportListener;
import es.upv.comm.webm.dash.mpd.AdaptationSet;
import es.upv.comm.webm.dash.mpd.Mpd;
import es.upv.comm.webm.dash.mpd.Representation;

public class Player implements Debug {

	private Context mContext;

	private URL mUrl;
	private URL mBaseUrl;

	private String mXml;

	private Mpd mpd;

	private AdaptationSet mAudioAdaptationSet;
	private AdaptationSet mVideoAdaptationSet;

	private float mVideoSizeRatio;

	private Stream mAudioSream;
	private Stream[] mVideoStreams;
	private int mCurrentVideoStream = -1;

	private AdaptationManager mAdaptationManager;

	private Buffer mBuffer;

	private FrameLayout mFrameLayout;
	private SurfaceView[] mSurfaceViews = null;

	private final Lock mLock = new ReentrantLock();
	private final Condition mInit = mLock.newCondition();
	private int mSurfaceNumber;
	private int mInitializedSurfaces;

	private PlayerReproductionListener mPlayerReproductionListener;
	
	private BufferReportListener mBufferReportListener;
	
	private VideoThread mVideoThread;

	public Player(Context context) {

		mContext = context;

	}

	public FrameLayout getVideoView() {
		return mFrameLayout;
	}

	public float getVideoSizeRatio() {
		return mVideoSizeRatio;
	}

	public void setDataSource(String url) throws MalformedURLException {

		mUrl = new URL(url);
		String baseUrl = mUrl.getProtocol() + "://" + mUrl.getHost() + mUrl.getPath();
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Base URL: " + baseUrl);

		// baseUrl = baseUrl.substring(0, baseUrl.length() - (mUrl.getFile().length() - 1));
		baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/") + 1);

		mBaseUrl = new URL(baseUrl);
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Base URL: " + mBaseUrl);

	}

	public void setPlayerReproductionListener(PlayerReproductionListener playerReproductionListener) {
		mPlayerReproductionListener = playerReproductionListener;
	}
	
	public void setBufferReportListener(BufferReportListener bufferReportListener) {
		mBufferReportListener = bufferReportListener;
	}

	public void prepareAsync(final ActionListener actionListener) {

		new Thread(new Runnable() {

			@Override
			public void run() {

				Looper.prepare();

				try {
					mXml = getXmlFromUrl(mUrl);
					mpd = new Mpd(mXml);

					// sets
					mAudioAdaptationSet = mpd.getAdaptationSet(AdaptationSet.Type.Audio);
					mVideoAdaptationSet = mpd.getAdaptationSet(AdaptationSet.Type.Video);

					// video size ratio
					int width = Integer.parseInt(mVideoAdaptationSet.getFirstRepresentation().getWidth());
					int height = Integer.parseInt(mVideoAdaptationSet.getFirstRepresentation().getHeight());
					float ratio = (float) width / (float) height;
					mVideoSizeRatio = ratio;
					Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Video Ratio: " + mVideoSizeRatio);

					// video streams
					mVideoStreams = new Stream[mVideoAdaptationSet.getRepresentations().size()];
					int vi = 0;
					for (Representation representation : mVideoAdaptationSet.getRepresentations()) {
						Stream videoStream = new Stream(representation, mBaseUrl);

						mVideoStreams[vi++] = videoStream;
					}

					mAdaptationManager = new BufferBasedAdaptationManager(mVideoAdaptationSet);
					mBuffer = new Buffer(mVideoStreams, 10000, 5000, mAdaptationManager);
					if (mAdaptationManager instanceof BufferReportListener) {
						mBuffer.addBufferReportListener((BufferReportListener) mAdaptationManager);
					}

					mFrameLayout = new FrameLayout(mContext);

					mSurfaceViews = new SurfaceView[mVideoStreams.length];

					mSurfaceNumber = mVideoStreams.length;
					mInitializedSurfaces = 0;

					for (int i = 0; i < mSurfaceViews.length; i++) {
						mSurfaceViews[i] = new SurfaceView(mContext);
						mSurfaceViews[i].getHolder().addCallback(new SurfaceHolderCallback());

						mFrameLayout.addView(mSurfaceViews[i]);

					}

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

		mLock.lock();
		try {
			while (mInitializedSurfaces < mSurfaceNumber) {
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Initialized surfaces: " + mInitializedSurfaces + ", waiting for " + mSurfaceNumber);

				mInit.await();

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			mLock.unlock();
		}

		mVideoThread = new VideoThread(mVideoStreams, mBuffer);
		mVideoThread.start();

	}

	public String getXmlFromUrl(URL url) throws IOException {
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
			throw new IOException("Can't get URL");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new IOException("Can't get URL");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Can't get URL");
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

		private boolean mStreamChanged = false;

		private MediaCodec[] mVideoCodecs;
		private ByteBuffer[][] mVideoCodecInputBuffers = null;
		private ByteBuffer[][] mVideoCodecOutputBuffers = null;

		private Handler mHandler;

		public VideoThread(Stream[] videoStreams, Buffer buffer) {

			mStreams = videoStreams;
			mBuffer = buffer;

			mVideoCodecs = new MediaCodec[mStreams.length];
			mVideoCodecInputBuffers = new ByteBuffer[mStreams.length][];
			mVideoCodecOutputBuffers = new ByteBuffer[mStreams.length][];

			for (int i = 0; i < mStreams.length; i++) {

				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Creating Codec for  " + i);

				Stream stream = mStreams[i];
				MediaFormat mf = stream.getStreamFormat();

				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Creating Codec with mediaformat  " + mf);

				mVideoCodecs[i] = MediaCodec.createDecoderByType(mf.getString(MediaFormat.KEY_MIME));

				mVideoCodecs[i].configure(mf, mSurfaceViews[i].getHolder().getSurface(), null, 0);
				mVideoCodecs[i].setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

				mVideoCodecs[i].start();
				mVideoCodecInputBuffers[i] = mVideoCodecs[i].getInputBuffers();
				mVideoCodecOutputBuffers[i] = mVideoCodecs[i].getOutputBuffers();

				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Video Codec: " + mVideoCodecs[i].toString());

			}

			mBuffer.start();

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

			Looper.prepare();
			mHandler = new Handler(Looper.getMainLooper());

			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Starting Video Thread...");

			boolean sawInputEOS = false;
			boolean sawOutputEOS = false;

			boolean eos = false;

			while (Thread.currentThread() == mVideoThread && !sawInputEOS && !sawOutputEOS) {

				long presentationTimeUs = -1;

				eos = !mBuffer.advance();

				if (!eos) {
					final int si = mBuffer.getStreamIndex();

					if (si != mCurrentVideoStream) {
						mStreamChanged = true;
						mCurrentVideoStream = si;
					}

					// mHandler.post(new Runnable() {
					//
					// @Override
					// public void run() {
					// for (int i = 0; i < mSurfaceViews.length; i++) {
					// if(i==si){
					// mSurfaceViews[i].setAlpha(1);
					// Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Invisible: "+i);
					// }else{
					// mSurfaceViews[i].setAlpha(0);
					// Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Visible: "+i);
					// }
					// }
					// }
					// });

					// input buffer
					int inputBufIndex = mVideoCodecs[si].dequeueInputBuffer(20000);
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

						mVideoCodecs[si].queueInputBuffer(inputBufIndex, 0, sampleSize, presentationTimeUs, sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM
								: 0);
						if (!sawInputEOS) {
							// mBuffer.advance();
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
					final int res = mVideoCodecs[si].dequeueOutputBuffer(info, 80000);
					if (res >= 0) {
						int outputBufIndex = res;
						ByteBuffer buf = mVideoCodecOutputBuffers[si][outputBufIndex];

						final byte[] chunk = new byte[info.size];
						buf.get(chunk); // Read the buffer all at once
						buf.clear(); // ** MUST DO!!! OTHERWISE THE NEXT TIME YOU GET THIS SAME BUFFER BAD THINGS WILL HAPPEN

						// if (chunk.length > 0) {
						// audioTrack.write(chunk, 0, chunk.length);
						// }
						mVideoCodecs[si].releaseOutputBuffer(outputBufIndex, true /* render */);

						if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
							sawOutputEOS = true;
						}
					} else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
						mVideoCodecOutputBuffers[si] = mVideoCodecs[si].getOutputBuffers();
					} else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
						final MediaFormat oformat = mVideoCodecs[si].getOutputFormat();
						Log.d(LOG_TAG, "Output format has changed to " + oformat);
					}

					if (mStreamChanged) {
						mStreamChanged = false;
						
						if(mPlayerReproductionListener!=null){
							Representation r = mVideoAdaptationSet.getRepresentations().get(si);
							mPlayerReproductionListener.playerQualityChange(Integer.parseInt(r.getWidth()), Integer.parseInt(r.getHeight()));
						}

						mHandler.postDelayed(new Runnable() {

							@Override
							public void run() {

								for (int i = 0; i < mSurfaceViews.length; i++) {
									if (si == i) {
										mSurfaceViews[i].setY(0);
									} else {
										mSurfaceViews[i].setY(1000);
									}
								}

								// if (si == 1) {
								// mSurfaceViews[1].setY(0);
								// mSurfaceViews[0].setY(1000);
								// } else {
								//
								// mSurfaceViews[0].setY(0);
								// mSurfaceViews[1].setY(1000);
								// }
							}
						}, 80);

					}

					if (mPlayerReproductionListener != null)
						mPlayerReproductionListener.playerReproductionTime((int) presentationTimeUs);
				}

			}

			// close

			for (int i = 0; i < mStreams.length; i++) {
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Releasing VideoCodec");
				mVideoCodecs[i].release();
			}

		}
	}

	private class SurfaceHolderCallback implements SurfaceHolder.Callback {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {

			mLock.lock();
			mInitializedSurfaces++;

			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Surface created, total: " + mInitializedSurfaces);

			mInit.signal();
			mLock.unlock();

		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Surface changed");

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {

			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Surface destroyed");

		}

	}

	public void close() {
		if(mVideoThread!=null){
			mVideoThread.stop();
		}
		
	}

}
