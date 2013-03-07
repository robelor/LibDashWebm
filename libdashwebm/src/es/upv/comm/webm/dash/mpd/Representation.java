package es.upv.comm.webm.dash.mpd;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import es.upv.comm.webm.dash.http.ByteRange;

import android.util.Log;

public class Representation implements Debug {

	public static final String KEY_REPRESENTATION = "Representation";
	public static final String KEY_REPRESENTATION_ID = "id";
	public static final String KEY_REPRESENTATION_BANDWIDTH = "bandwidth";
	public static final String KEY_REPRESENTATION_WIDTH = "width";
	public static final String KEY_REPRESENTATION_HEIGHT = "height";

	public static final String KEY_BASE_URL = "BaseURL";

	public static final String KEY_SEGMENT_BASE = "SegmentBase";
	public static final String KEY_SEGMENT_BASE_INDEX_RANGE = "indexRange";

	public static final String KEY_INITIALIZATION = "Initialization";
	public static final String KEY_INITIALIZATION_RANGE = "range";

	private String id;
	private String bandwidth;

	private String width;
	private String height;

	private String baseUrl;
	private String initRange;
	private ByteRange initByteRange;
	private String indexRange;
	private ByteRange indexByteRange;

	public Representation(Node representationNode) {

		if (representationNode.getNodeName().equalsIgnoreCase(KEY_REPRESENTATION)) {
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "      Parsing Representation node...");

			NamedNodeMap representationAttrs = representationNode.getAttributes();

			id = U.getAttribute(representationAttrs, KEY_REPRESENTATION_ID);
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "        Id: " + id);

			bandwidth = U.getAttribute(representationAttrs, KEY_REPRESENTATION_BANDWIDTH);
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "        Bandwidth: " + bandwidth);

			width = U.getAttribute(representationAttrs, KEY_REPRESENTATION_WIDTH);
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "        Width: " + width);

			height = U.getAttribute(representationAttrs, KEY_REPRESENTATION_HEIGHT);
			if (D)
				Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "        Height: " + height);

			NodeList representationChildsNl = representationNode.getChildNodes();
			for (int i = 0; i < representationChildsNl.getLength(); i++) {
				Node representationChildNode = representationChildsNl.item(i);
				if (representationChildNode != null) {
					if (representationChildNode.getNodeName().equalsIgnoreCase(Representation.KEY_BASE_URL)) {
						if (D)
							Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "        Parsing BaseUrl node...");

						baseUrl = U.getElementValue(representationChildNode);
						if (D)
							Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "        BaseUrl: " + baseUrl);

					} else if (representationChildNode.getNodeName().equalsIgnoreCase(Representation.KEY_SEGMENT_BASE)) {
						if (D)
							Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "        Parsing SegmentBase node...");

						NamedNodeMap segmentBaseAttrs = representationChildNode.getAttributes();

						indexRange = U.getAttribute(segmentBaseAttrs, KEY_SEGMENT_BASE_INDEX_RANGE);
						if (indexRange != null && !indexRange.equals(""))
							indexByteRange = new ByteRange(indexRange);
						if (D)
							Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "        IndexRange: " + indexRange);

						NodeList segmentBaseNl = representationChildNode.getChildNodes();
						for (int j = 0; j < segmentBaseNl.getLength(); j++) {
							Node initializationNode = segmentBaseNl.item(j);
							if (initializationNode != null) {
								if (initializationNode.getNodeName().equalsIgnoreCase(Representation.KEY_INITIALIZATION)) {
									if (D)
										Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "        Parsing Initialization node...");

									NamedNodeMap initializationBaseAttrs = initializationNode.getAttributes();

									initRange = U.getAttribute(initializationBaseAttrs, KEY_INITIALIZATION_RANGE);
									if (initRange != null && !initRange.equals(""))
										initByteRange = new ByteRange(initRange);
									if (D)
										Log.d(LOG_TAG, this.getClass().getSimpleName() + ": " + "        InitRange: " + initRange);

								}
							}
						}

					}
				}
			}
		}

	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public ByteRange getInitRange() {
		return initByteRange;
	}

	public ByteRange getIndexRange() {
		return indexByteRange;
	}

}
