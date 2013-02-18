package view.resultviewer.chartview;

import util.GraphAlgorithm;

public class ShortestPathChartWindow extends AbstractChartWindow {
	private static final long serialVersionUID = 1L;
	
	public ShortestPathChartWindow(int start, int target, boolean useRelative) {
		super(start, target, useRelative);
	}

	@Override
	protected void setGlobalTitle() {
		this.windowTitle = "Shortest Path From Node\"" + start + "\" To Node \"" + target + "\"";
		this.setTitle(windowTitle);
	}

	@Override
	protected void setBarDescription() {
		this.barDescription = "Distance";
	}

	@Override
	protected double getValue() {
//		return Algorithm.getShortestPath(Sampler.getInstance().generateSampledGraph(), start, target);
		return GraphAlgorithm.getShortestPathOnTheFly(start, target);
	}

}
