package es.upv.comm.webm.dash.container.segment.info;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.FloatElement;
import org.ebml.MasterElement;
import org.ebml.StringElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;
import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.container.Container;
import es.upv.comm.webm.dash.container.WebmParseException;

public class Info implements Debug {

	private long mTimeCodeScale;
	private double mDuration;
	private String mTitle;

	public long getTimeCodeScale() {
		return mTimeCodeScale;
	}

	private void setTimeCodeScale(long timeCodeScale) {
		mTimeCodeScale = timeCodeScale;
	}

	public double getDuration() {
		return mDuration;
	}

	private void setDuration(double duration) {
		mDuration = duration;
	}

	public String getTitle() {
		return mTitle;
	}

	private void setTitle(String title) {
		mTitle = title;
	}

	public static Info create(DataSource dataSource) {
		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		return create(reader, dataSource);
	}

	private static Info create(EBMLReader ebmlReader, DataSource dataSource) {

		Element rootElement = ebmlReader.readNextElement();
		if (rootElement == null) {
			throw new WebmParseException("Error: Unable to scan for EBML elements");
		}

		if (rootElement.equals(MatroskaDocType.Segment_Id)) {
			return create(rootElement, ebmlReader, dataSource);
		} else {
			return null;
		}

	}

	public static Info create(Element infoElement, EBMLReader ebmlReader, DataSource dataSource) {
		Info info = new Info();

		Element auxElement = ((MasterElement) infoElement).readNextChild(ebmlReader);
		while (auxElement != null) {
			auxElement.readData(dataSource);
			
			if (auxElement.equals(MatroskaDocType.TimecodeScale_Id)) {
				long timeCodeScale = ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "      TimeCodeScale: " + timeCodeScale);
				info.setTimeCodeScale(timeCodeScale);
			} else if (auxElement.equals(MatroskaDocType.Duration_Id)) {
				double duration = ((FloatElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "      Duration: " + duration);
				info.setDuration(duration);
			} else if (auxElement.equals(MatroskaDocType.Title_Id)) {
				String title = ((StringElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "      Title: " + title);
				info.setTitle(title);
			}

			auxElement.skipData(dataSource);
			auxElement = ((MasterElement) infoElement).readNextChild(ebmlReader);
		}

		return info;

	}

}
