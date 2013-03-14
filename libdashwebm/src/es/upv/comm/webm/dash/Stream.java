package es.upv.comm.webm.dash;

import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;
import es.upv.comm.webm.dash.container.Container;
import es.upv.comm.webm.dash.mpd.Representation;

public class Stream implements Debug {
	
	private int deleteme;

	private URL mUrl;

	private Container mContainer;

	public Stream(Representation representation, URL baseUrl) throws MalformedURLException {
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Creating Stream...");

		mContainer = new Container(baseUrl, representation);
		mUrl = mContainer.getContainerUrl();
		mContainer.init();
	}

	public void getNextBlock() {
		if (mContainer != null && mContainer.getSegment() != null) {
			mContainer.getSegment().getNextBlock();
		}

	}
}
