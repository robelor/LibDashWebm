package es.upv.comm.webm.dash.container.segment.track;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.FloatElement;
import org.ebml.MasterElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;
import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.container.Container;
import es.upv.comm.webm.dash.container.WebmParseException;
import es.upv.comm.webm.dash.util.HexByteArray;

public class Audio implements Debug {

	private int mChannels;
	private double mSamplingFrequency;
	private int mBitDepth;

	public long getmChannels() {
		return mChannels;
	}

	public void setmChannels(int mChannels) {
		this.mChannels = mChannels;
	}

	public double getmSamplingFrequency() {
		return mSamplingFrequency;
	}

	public void setmSamplingFrequency(double mSamplingFrequency) {
		this.mSamplingFrequency = mSamplingFrequency;
	}

	public int getmBitDepth() {
		return mBitDepth;
	}

	public void setmBitDepth(int mBitDepth) {
		this.mBitDepth = mBitDepth;
	}

	public static Audio create(DataSource dataSource) {
		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		return create(reader, dataSource);
	}

	private static Audio create(EBMLReader ebmlReader, DataSource dataSource) {

		Element rootElement = ebmlReader.readNextElement();
		if (rootElement == null) {
			throw new WebmParseException("Error: Unable to scan for EBML elements");
		}

		if (rootElement.equals(MatroskaDocType.TrackAudio_Id)) {
			return create(rootElement, ebmlReader, dataSource);
		} else {
			return null;
		}

	}

	public static Audio create(Element audioElement, EBMLReader ebmlReader, DataSource dataSource) {
		Audio audio = new Audio();

		Element auxElement = ((MasterElement) audioElement).readNextChild(ebmlReader);
		while (auxElement != null) {

			auxElement.readData(dataSource);

			if (auxElement.equals(MatroskaDocType.Channels_Id)) {
				int channels = (int) ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "        Channels: " + channels);
				audio.setmChannels(channels);

			} else if (auxElement.equals(MatroskaDocType.SamplingFrequency_Id)) {
				double samplingFrequency = ((FloatElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "        SamplingFreq: " + samplingFrequency);
				audio.setmSamplingFrequency(samplingFrequency);

			} else if (auxElement.equals(MatroskaDocType.BitDepth_Id)) {
				int bitDepth = (int) ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "        BitDepth: " + bitDepth);
				audio.setmBitDepth(bitDepth);
			}else{
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "        Unhandled element: "+HexByteArray.bytesToHex(auxElement.getType()));
			}

			auxElement.skipData(dataSource);
			auxElement = ((MasterElement) audioElement).readNextChild(ebmlReader);
		}

		return audio;
	}

}
