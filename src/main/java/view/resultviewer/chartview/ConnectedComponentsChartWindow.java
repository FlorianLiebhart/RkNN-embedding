package view.resultviewer.chartview;

import util.GraphAlgorithm;

public class ConnectedComponentsChartWindow extends AbstractChartWindow {
	private static final long serialVersionUID = 1L;

	public ConnectedComponentsChartWindow(boolean useRelative) {
		super(useRelative);
	}
	
	@Override
	protected void setGlobalTitle() {
		this.windowTitle = "Number of Connected Components in Graph";
		this.setTitle(windowTitle);
	}

	@Override
	protected void setBarDescription() {
		this.barDescription = "Connected Components";
	}

	@Override
	protected double getValue() {
		return GraphAlgorithm.getConnectedComponents();
	}

}

