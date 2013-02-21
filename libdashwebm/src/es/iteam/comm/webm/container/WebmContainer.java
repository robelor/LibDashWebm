package es.iteam.comm.webm.container;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;

import es.iteam.comm.webm.Debug;
import es.iteam.comm.webm.container.header.Header;
import es.iteam.comm.webm.container.segment.Segment;

public class WebmContainer implements Debug {

	private Header mHeader;
	private Segment mSegment;

	public Header getHeader() {
		return mHeader;
	}

	public void setHeader(Header header) {
		mHeader = header;
	}

	public Segment getSegment() {
		return mSegment;
	}

	public void setSegment(Segment segment) {
		mSegment = segment;
	}

	public static WebmContainer parse(DataSource dataSource) {
		WebmContainer container = new WebmContainer();

		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		Element rootElement = reader.readNextElement();

		if (rootElement == null) {
			throw new java.lang.RuntimeException("Error: Unable to scan for EBML elements");
		} else {

			if (rootElement.equals(MatroskaDocType.EBMLHeader_Id)) {
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "Parsing Header...");
				container.setHeader(Header.create(rootElement, reader, dataSource));
			}
			
			rootElement = reader.readNextElement();

			if (rootElement.equals(MatroskaDocType.Segment_Id)) {
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "Parsing Segment...");
				container.setSegment(Segment.create(rootElement, reader, dataSource));
			}
		}

		return container;
	}

}
