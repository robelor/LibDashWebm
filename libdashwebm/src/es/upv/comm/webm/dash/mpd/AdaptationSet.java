package es.upv.comm.webm.dash.mpd;

import java.util.ArrayList;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class AdaptationSet implements Debug {

	public static final String KEY_ADAPTATION_SET = "AdaptationSet";
	public static final String KEY_ADAPTATION_SET_ID = "id";
	public static final String KEY_ADAPTATION_SET_MIME_TYPE = "mimeType";
	public static final String KEY_ADAPTATION_SET_CODECS = "codecs";
	public static final String KEY_ADAPTATION_SET_LANG = "lang";
	public static final String KEY_ADAPTATION_SET_SUBSEGMENT_ALIGNMENT = "subsegmentAlignment";
	public static final String KEY_ADAPTATION_SET_SUBSEGMENT_STARTS_WITH_SAP = "subsegmentStartsWithSAP";
	public static final String KEY_ADAPTATION_SET_BITSTREAM_SWITCHING = "bitstreamSwitching";
	public static final String KEY_ADAPTATION_SET_AUDIO_SAMPLING_RATE = "audioSamplingRate";

	public enum Type {
		Audio, Video
	}

	private Type type;

	private String id;
	private String mimeType;
	private String codecs;
	private String lang;
	private String subsegmentAligment;
	private String subsegmentStartsWithSap;
	private String bitstreamSwitching;

	// audio
	private String audioSamplingRate;

	private ArrayList<Representation> representations = new ArrayList<Representation>();

	public AdaptationSet(Node adaptationSetNode) {
		if (adaptationSetNode.getNodeName().equalsIgnoreCase(KEY_ADAPTATION_SET)) {
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "    Parsing AdaptationSet node...");

			NamedNodeMap adaptationSetAttrs = adaptationSetNode.getAttributes();

			id = U.getAttribute(adaptationSetAttrs, KEY_ADAPTATION_SET_ID);
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "      Id: " + id);

			mimeType = U.getAttribute(adaptationSetAttrs, KEY_ADAPTATION_SET_MIME_TYPE);
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "      Mime type: " + mimeType);
			if (mimeType != null) {
				if (mimeType.startsWith("video")) {
					type = Type.Video;
				} else if (mimeType.startsWith("audio")) {
					type = Type.Audio;
				}
			}
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "      Type: " + type);

			codecs = U.getAttribute(adaptationSetAttrs, KEY_ADAPTATION_SET_CODECS);
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "      Codecs: " + codecs);

			lang = U.getAttribute(adaptationSetAttrs, KEY_ADAPTATION_SET_LANG);
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "      Lang: " + lang);

			subsegmentAligment = U.getAttribute(adaptationSetAttrs, KEY_ADAPTATION_SET_SUBSEGMENT_ALIGNMENT);
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "      Subsegment aligment: " + subsegmentAligment);

			subsegmentStartsWithSap = U.getAttribute(adaptationSetAttrs, KEY_ADAPTATION_SET_SUBSEGMENT_STARTS_WITH_SAP);
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "      Subsegment starts with SAP: " + subsegmentStartsWithSap);

			bitstreamSwitching = U.getAttribute(adaptationSetAttrs, KEY_ADAPTATION_SET_BITSTREAM_SWITCHING);
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "      Bitstream switching: " + bitstreamSwitching);

			audioSamplingRate = U.getAttribute(adaptationSetAttrs, KEY_ADAPTATION_SET_AUDIO_SAMPLING_RATE);
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "      Audio sampling rate: " + audioSamplingRate);

			NodeList mpdNl = adaptationSetNode.getChildNodes();
			for (int i = 0; i < mpdNl.getLength(); i++) {
				Node representationNode = mpdNl.item(i);
				if (representationNode != null) {
					if (representationNode.getNodeName().equalsIgnoreCase(Representation.KEY_REPRESENTATION)) {
						Representation representation = new Representation(representationNode);
						representations.add(representation);
					}
				}
			}

		}
	}

	public Type getType() {
		return type;
	}

	public Representation getFirstRepresentation() {
		if (representations.size() > 0) {
			return representations.get(0);
		}
		return null;
	}

	public ArrayList<Representation> getRepresentations() {
		return representations;
	}
}