package view.resultviewer.chartview;

import util.GraphAlgorithm;

public class ReachableNodesChartWindow extends AbstractChartWindow {
	private static final long serialVersionUID = 1L;

	public ReachableNodesChartWindow(int start, boolean useRelative) {
		super(start, useRelative);
	}
	
	@Override
	protected void setGlobalTitle() {
		this.windowTitle = "Reachable Nodes From Node \"" + start + "\"";
		this.setTitle(windowTitle);
	}

	@Override
	protected void setBarDescription() {
		this.barDescription = "Reachable Nodes";
	}

	@Override
	protected double getValue() {
//		return Algorithm.getReachableNodes(Sampler.getInstance().generateSampledGraph(), start);
		return GraphAlgorithm.getReachableNodesOnTheFly(start);
	}

}
