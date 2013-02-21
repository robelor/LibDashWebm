package es.iteam.comm.webm.container.segment.track;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;
import es.iteam.comm.webm.Debug;
import es.iteam.comm.webm.container.WebmContainer;
import es.iteam.comm.webm.container.WebmParseException;
import es.iteam.comm.webm.util.HexByteArray;

public class Video implements Debug{
	
	private int mWidth;
	private int mHeight;
	
	public int getmWidth() {
		return mWidth;
	}
	
	public void setmWidth(int mWidth) {
		this.mWidth = mWidth;
	}
	
	public int getmHeight() {
		return mHeight;
	}
	
	public void setmHeight(int mHeight) {
		this.mHeight = mHeight;
	}
	
	public static Video create(DataSource dataSource) {
		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		return create(reader, dataSource);
	}

	private static Video create(EBMLReader ebmlReader, DataSource dataSource) {

		Element rootElement = ebmlReader.readNextElement();
		if (rootElement == null) {
			throw new WebmParseException("Error: Unable to scan for EBML elements");
		}

		if (rootElement.equals(MatroskaDocType.TrackVideo_Id)) {
			return create(rootElement, ebmlReader, dataSource);
		} else {
			return null;
		}

	}

	public static Video create(Element videoElement, EBMLReader ebmlReader, DataSource dataSource) {
		Video video = new Video();

		Element auxElement = ((MasterElement) videoElement).readNextChild(ebmlReader);
		while (auxElement != null) {
			
			auxElement.readData(dataSource);
			
			System.out.println(">>>>>**" + HexByteArray.bytesToHex(auxElement.getType()));
			
			if (auxElement.equals(MatroskaDocType.PixelWidth_Id)) {
				int pixelWidth = (int) ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "        PixelWidth: " + pixelWidth);
				video.setmWidth(pixelWidth);
				
			}else if (auxElement.equals(MatroskaDocType.PixelHeight_Id)) {
				int pixelHeight = (int) ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, WebmContainer.class.getSimpleName() + ": " + "        PixelHeight: " + pixelHeight);
				video.setmHeight(pixelHeight);

			}

			auxElement.skipData(dataSource);
			auxElement = ((MasterElement) videoElement).readNextChild(ebmlReader);
		}

		return video;
	}

}
