package view.resultviewer.chartview;

import util.GraphAlgorithm;

public class DiameterChartWindow extends AbstractChartWindow {
	private static final long serialVersionUID = 1L;

	public DiameterChartWindow(boolean useRelative) {
		super(useRelative);
	}
	
	@Override
	protected void setGlobalTitle() {
		this.windowTitle = "Diameter from this Graph";
		this.setTitle(windowTitle);
	}

	@Override
	protected void setBarDescription() {
		this.barDescription = "Diameter";
	}

	@Override
	protected double getValue() {
		return GraphAlgorithm.getGraphDiameter();
	}

}
