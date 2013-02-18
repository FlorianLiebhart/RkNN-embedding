package view.resultviewer;

import java.util.Observable;
import java.util.Observer;

public interface ResultVisualizer extends Observer {

	@Override
	public abstract void update(Observable o, Object arg);
	
	public void startSampling();
	
	public void stopSampling();
}
