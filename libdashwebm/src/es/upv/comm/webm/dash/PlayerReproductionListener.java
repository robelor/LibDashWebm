package es.upv.comm.webm.dash;

public interface PlayerReproductionListener {
	
	public void playerReproductionTime(int time);
	
	public void playerQualityChange(int x, int y);

}
