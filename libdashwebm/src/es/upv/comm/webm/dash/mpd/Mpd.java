package es.upv.comm.webm.dash.mpd;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class Mpd implements Debug {

	public	static final String KEY_MPD = "MPD";
	public	static final String KEY_MPD_MEDIA_PRESENTATION_DURATION = "mediaPresentationDuration";
	public	static final String KEY_MPD_MIN_BUFFER_TIME = "minBufferTime";

	public	static final String KEY_PERIOD = "Period";
	public	static final String KEY_PERIOD_ID = "id";
	public	static final String KEY_PERIOD_START = "start";
	public	static final String KEY_PERIOD_DURATION = "duration";

	private boolean mpdFound = false;
	private int mpdCount = 0;
	private String mediaPresentationDuration;
	private String minBufferTime;

	private boolean periodFound = false;
	private int periodCount = 0;
	private String id;
	private String start;
	private String duration;

	private String profiles;

	private ArrayList<AdaptationSet> adaptationSets = new ArrayList<AdaptationSet>();

	public Mpd(String xml) {
		if (D)
			Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Parsing Webm MPD");

		Document doc = getDomElement(xml);

		NodeList mpdNl = doc.getChildNodes();
		for (int i = 0; i < mpdNl.getLength(); i++) {
			Node mpdNode = mpdNl.item(i);
			if (mpdNode != null) {
				if (mpdNode.getNodeName().equalsIgnoreCase(KEY_MPD)) {
					if (D)
						Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "Parsing MPD node...");

					mpdFound = true;
					mpdCount++;

					NamedNodeMap mpdAttrs = mpdNode.getAttributes();

					mediaPresentationDuration = U.getAttribute(mpdAttrs, KEY_MPD_MEDIA_PRESENTATION_DURATION);
					if (D)
						Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "  Duration: " + mediaPresentationDuration);

					minBufferTime = U.getAttribute(mpdAttrs, KEY_MPD_MIN_BUFFER_TIME);
					if (D)
						Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "  Min buffer time: " + minBufferTime);

					NodeList periodNl = mpdNode.getChildNodes();
					for (int j = 0; j < periodNl.getLength(); j++) {
						Node periodNode = periodNl.item(j);
						if (periodNode != null) {
							if (periodNode.getNodeName().equalsIgnoreCase(KEY_PERIOD)) {
								if (D)
									Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "  Parsing Period node...");

								periodFound = true;
								periodCount++;

								NamedNodeMap periodAttrs = periodNode.getAttributes();

								id = U.getAttribute(periodAttrs, KEY_PERIOD_ID);
								if (D)
									Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "    Id: " + id);

								start = U.getAttribute(periodAttrs, KEY_PERIOD_START);
								if (D)
									Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "    Min start time: " + start);

								duration = U.getAttribute(periodAttrs, KEY_PERIOD_DURATION);
								if (D)
									Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "    Duration: " + duration);

								NodeList adaptationNl = periodNode.getChildNodes();
								for (int k = 0; k < adaptationNl.getLength(); k++) {
									Node adaptationSetNode = adaptationNl.item(k);
									if (adaptationSetNode != null) {
										if (adaptationSetNode.getNodeName().equalsIgnoreCase(AdaptationSet.KEY_ADAPTATION_SET)) {
											AdaptationSet adaptationSet = new AdaptationSet(adaptationSetNode);
											adaptationSets.add(adaptationSet);
										}
									}
								}

							}
						}
					}
				}
			}
		}
	}

	public Document getDomElement(String xml) {
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			DocumentBuilder db = dbf.newDocumentBuilder();

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			doc = db.parse(is);

		} catch (ParserConfigurationException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (SAXException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		}
		// return DOM
		return doc;
	}

}
