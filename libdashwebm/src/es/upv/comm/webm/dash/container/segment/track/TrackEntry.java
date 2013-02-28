package es.upv.comm.webm.dash.container.segment.track;

import org.ebml.BinaryElement;
import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.StringElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataSource;
import org.ebml.matroska.MatroskaDocType;

import android.util.Log;
import es.upv.comm.webm.dash.Debug;
import es.upv.comm.webm.dash.container.Container;
import es.upv.comm.webm.dash.container.ParseException;
import es.upv.comm.webm.dash.util.HexByteArray;

public class TrackEntry implements Debug {

	private long mTrackNumber;
	private long mTrackUid;
	private boolean mFlagLacing;
	private byte mTrackType;
	private boolean mFlagDefault;
	private String mCodecId;
	private long mTrackDefaultDuration;
	private String mTrackName;
	private String mTrackLanguage;
	
	private Video mVideo;
	
	private Audio mAudio;

	public long getTrackNumber() {
		return mTrackNumber;
	}

	private void setTrackNumber(long trackNumber) {
		mTrackNumber = trackNumber;
	}

	public static TrackEntry create(DataSource dataSource) {
		EBMLReader reader = new EBMLReader(dataSource, MatroskaDocType.obj);
		return create(reader, dataSource);
	}

	public long getmTrackUid() {
		return mTrackUid;
	}

	public void setmTrackUid(long mTrackUid) {
		this.mTrackUid = mTrackUid;
	}
	
	public boolean ismFlagLacing() {
		return mFlagLacing;
	}
	
	public void setmFlagLacing(boolean mFlagLacing) {
		this.mFlagLacing = mFlagLacing;
	}

	public byte getmTrackType() {
		return mTrackType;
	}

	public void setmTrackType(byte mTrackType) {
		this.mTrackType = mTrackType;
	}
	
	public boolean ismFlagDefault() {
		return mFlagDefault;
	}
	
	public void setmFlagDefault(boolean mFlagDefault) {
		this.mFlagDefault = mFlagDefault;
	}
	
	public String getmCodecId() {
		return mCodecId;
	}
	
	public void setmCodecId(String mCodecId) {
		this.mCodecId = mCodecId;
	}

	public long getmTrackDefaultDuration() {
		return mTrackDefaultDuration;
	}

	public void setmTrackDefaultDuration(long mTrackDefaultDuration) {
		this.mTrackDefaultDuration = mTrackDefaultDuration;
	}

	public String getmTrackName() {
		return mTrackName;
	}

	public void setmTrackName(String mTrackName) {
		this.mTrackName = mTrackName;
	}

	public String getmTrackLanguage() {
		return mTrackLanguage;
	}

	public void setmTrackLanguage(String mTrackLanguage) {
		this.mTrackLanguage = mTrackLanguage;
	}
	
	public Video getmVideo() {
		return mVideo;
	}
	
	public void setmVideo(Video mVideo) {
		this.mVideo = mVideo;
	}
	
	public Audio getmAudio() {
		return mAudio;
	}
	
	public void setmAudio(Audio mAudio) {
		this.mAudio = mAudio;
	}

	private static TrackEntry create(EBMLReader ebmlReader, DataSource dataSource) {

		Element rootElement = ebmlReader.readNextElement();
		if (rootElement == null) {
			throw new ParseException("Error: Unable to scan for EBML elements");
		}

		if (rootElement.equals(MatroskaDocType.TrackEntry_Id)) {
			return create(rootElement, ebmlReader, dataSource);
		} else {
			return null;
		}

	}

	public static TrackEntry create(Element trackEntryElement, EBMLReader ebmlReader, DataSource dataSource) {
		TrackEntry trackEntry = new TrackEntry();

		Element auxElement = ((MasterElement) trackEntryElement).readNextChild(ebmlReader);
		while (auxElement != null) {
			
			if (auxElement.equals(MatroskaDocType.TrackNumber_Id)) {
				auxElement.readData(dataSource);
				long trackNumber = ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "      TrackNumber: " + trackNumber);
				trackEntry.setTrackNumber(trackNumber);

			} else if (auxElement.equals(MatroskaDocType.TrackUID_Id)) {
				auxElement.readData(dataSource);
				long trackUid = ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "      TrackUid: " + trackUid);
				trackEntry.setmTrackUid(trackUid);

			}else if (auxElement.equals(MatroskaDocType.TrackFlagLacing)) {
				auxElement.readData(dataSource);
				byte[] flagLacing = ((BinaryElement) auxElement).getData();
				if(flagLacing.length == 1){
					boolean flag = false;
					if(flagLacing[0]>0){
						flag = true;
					}
					if (D)
						Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "      FlagLacing: " + flag);
					trackEntry.setmFlagLacing(flag);
				}

			}else if (auxElement.equals(MatroskaDocType.TrackFlagDefault)) {
				auxElement.readData(dataSource);
				byte[] flagDefault = ((BinaryElement) auxElement).getData();
				if(flagDefault.length == 1){
					boolean flag = false;
					if(flagDefault[0]>0){
						flag = true;
					}
					if (D)
						Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "      FlagDefault: " + flag);
					trackEntry.setmFlagDefault(flag);
				}

			} else if (auxElement.equals(MatroskaDocType.TrackCodecID_Id)) {
				auxElement.readData(dataSource);
				String codecId = ((StringElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "      CodedId: " + codecId);
				trackEntry.setmCodecId(codecId);

			}  else if (auxElement.equals(MatroskaDocType.TrackType_Id)) {
				auxElement.readData(dataSource);
				byte trackType = (byte) ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "      TrackType: " + trackType); 
				trackEntry.setmTrackType(trackType);

			} else if (auxElement.equals(MatroskaDocType.TrackDefaultDuration_Id)) {
				auxElement.readData(dataSource);
				long defaulDuration = ((UnsignedIntegerElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "      TrackDefaultDuration: " + defaulDuration);
				trackEntry.setmTrackDefaultDuration(defaulDuration);

			} else if (auxElement.equals(MatroskaDocType.TrackName_Id)) {
				auxElement.readData(dataSource);
				String trackName = ((StringElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "      TrackName: " + trackName);
				trackEntry.setmTrackName(trackName);

			} else if (auxElement.equals(MatroskaDocType.TrackLanguage_Id)) {
				auxElement.readData(dataSource);
				String trackLanguage = ((StringElement) auxElement).getValue();
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "      TrackLanguage: " + trackLanguage);
				trackEntry.setmTrackName(trackLanguage);
				
			} else if (auxElement.equals(MatroskaDocType.TrackVideo_Id)) {
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "      Parsing Video...");
				trackEntry.setmVideo(Video.create(auxElement, ebmlReader, dataSource));
				
			}else if (auxElement.equals(MatroskaDocType.TrackAudio_Id)) {
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "      Parsing Audio...");
				trackEntry.setmAudio(Audio.create(auxElement, ebmlReader, dataSource));
			}else{
				if (D)
					Log.d(LOG_TAG, Container.class.getSimpleName() + ": " + "      Unhandled element: "+HexByteArray.bytesToHex(auxElement.getType()));
			}

			auxElement.skipData(dataSource);
			auxElement = ((MasterElement) trackEntryElement).readNextChild(ebmlReader);
		}

		return trackEntry;

	}

}
