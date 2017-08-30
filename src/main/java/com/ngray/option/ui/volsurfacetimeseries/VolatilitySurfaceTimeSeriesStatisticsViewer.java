package com.ngray.option.ui.volsurfacetimeseries;

import java.awt.Dimension;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart2d.Chart2d;
import org.jzy3d.colors.Color;
import org.jzy3d.plot2d.primitives.Serie2d;
import org.jzy3d.plot3d.primitives.axes.AxeBox;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;
import org.jzy3d.plot3d.text.renderers.TextBitmapRenderer;

import com.jogamp.opengl.util.gl2.GLUT;
import com.ngray.option.analysis.timeseries.TimeSeries;
import com.ngray.option.analysis.timeseries.VolatilitySurfaceTimeSeriesStatistics;
import com.ngray.option.ui.Frames;
import net.miginfocom.swing.MigLayout;

public class VolatilitySurfaceTimeSeriesStatisticsViewer {
	
	private final JFrame parentFrame;
	private final VolatilitySurfaceTimeSeriesStatistics statistics;
	private JInternalFrame frame;
	
	public VolatilitySurfaceTimeSeriesStatisticsViewer(JFrame parentFrame, VolatilitySurfaceTimeSeriesStatistics statistics) {
		this.parentFrame = parentFrame;
		this.statistics = statistics;
	}
	
	public void show() {
		String title = statistics.getVolatilitySurfaceName() + ":" + statistics.getSnapshotType() + ": " + "Days to Expiry=" + statistics.getDaysToExpiry();
		frame = createFrame(title, statistics);
		parentFrame.add(frame);	
	}

	private JInternalFrame createFrame(String title, VolatilitySurfaceTimeSeriesStatistics statistics) {
		JPanel panel = new JPanel(new MigLayout("", "[grow]", "[][][][]"));
		
		// table of calculated summary data
		panel.add(new JLabel("Summary"), "cell 0 0");
		String[] columnNames = { "Strike(-ATM)", "Mean IV", "Min IV", "Max IV" };
		Object[][] data = getSummaryData(statistics, columnNames.length);
		JTable summaryTable = new JTable(data, columnNames);
		summaryTable.setPreferredScrollableViewportSize(
				new Dimension(summaryTable.getPreferredSize().width, summaryTable.getRowHeight() * (1 + summaryTable.getRowCount()))
				);
		JScrollPane scrollPane = new JScrollPane(summaryTable);
		panel.add(scrollPane, "cell 0 1");
		
		// moving averages
		panel.add(new JLabel("Moving Average IV"), "cell 0 2");
		createMovingAverageTables(panel, statistics);
		
		Chart chart = createMovingAverageGraph(statistics);
		ChartLauncher.openChart(chart);
		JInternalFrame frame = Frames.createJInternalFrame(title, null, panel);
		return frame;
	}

	private void createMovingAverageTables(JPanel panel, VolatilitySurfaceTimeSeriesStatistics statistics) {
		
		JTabbedPane tabbedPane = new JTabbedPane();
		String[] columnNames = new String[] { "ValueDate", "Actual", "5-day", "30-day", "90-day" };
		for (Double atmOffset : statistics.getAtmOffsets()) {
			Object[][] data = getMovingAverages(atmOffset, statistics, columnNames.length);
			JTable table = new JTable(data, columnNames);
			String tabTitle = null;
			if (Double.compare(atmOffset, 0) == 0) tabTitle = "ATM";
			if (Double.compare(atmOffset, 0) < 0) tabTitle = "ATM" + atmOffset;
			if (Double.compare(atmOffset, 0) > 0) tabTitle = "ATM+" + atmOffset; 
			tabbedPane.add(tabTitle, new JScrollPane(table));
		}
		panel.add(tabbedPane, "cell 0 3");
	}

	private Object[][] getMovingAverages(Double atmOffset, VolatilitySurfaceTimeSeriesStatistics statistics, int numColumns) {
		TimeSeries<LocalDate> actuals = statistics.getMovingAverage(atmOffset, 1);
		TimeSeries<LocalDate> fiveDay = statistics.getMovingAverage(atmOffset, VolatilitySurfaceTimeSeriesStatistics.FIVE_DAY);
		TimeSeries<LocalDate> thirtyDay = statistics.getMovingAverage(atmOffset, VolatilitySurfaceTimeSeriesStatistics.THIRTY_DAY);
		TimeSeries<LocalDate> ninetyDay = statistics.getMovingAverage(atmOffset, VolatilitySurfaceTimeSeriesStatistics.NINETY_DAY);
		Object[][] data = new Object[actuals.getSize()][numColumns];
		int row = 0;
		for (LocalDate date : actuals.getTimePoints()) {
			data[row][0] = date;
			data[row][1] = actuals.getValue(date);
			data[row][2] = fiveDay.getValue(date);
			data[row][3] = thirtyDay.getValue(date);
			data[row][4] = ninetyDay.getValue(date);
			++row;
		}
		return data;
	}

	private Chart createMovingAverageGraph(VolatilitySurfaceTimeSeriesStatistics statistics) {
		
		Chart2d chart = new Chart2d();
		chart.getAxeLayout().setXTickRenderer(new ITickRenderer() {
			@Override
			public String format(double value) {	
				LocalDate date = LocalDate.ofEpochDay((long)value);
				String str = date.toString();
				return str;
			}});
		
		((AxeBox)chart.getView().getAxe()).setTextRenderer(new TextBitmapRenderer()  {
			{
	          font = GLUT.BITMAP_HELVETICA_18;
	          fontHeight = 18;
			}
        });
		chart.getAxeLayout().setXAxeLabel("Value Date");
		chart.getAxeLayout().setYAxeLabel("Implied Volatility");
		
		for (double atmOffset : statistics.getAtmOffsets()) {
			{
				TimeSeries<LocalDate> actual = statistics.getMovingAverage(atmOffset, 1);
				List<LocalDate> timePoints = actual.getTimePoints();
				Serie2d serie = chart.getSerie("Actual", Serie2d.Type.LINE);
				timePoints.forEach(timePoint ->
				  { long days = timePoint.toEpochDay(); 
				    serie.add(days, actual.getValue(timePoint)); });
				serie.setColor(Color.BLACK);
			}
			{
				TimeSeries<LocalDate> fiveDayMovingAvg = statistics.getMovingAverage(atmOffset, VolatilitySurfaceTimeSeriesStatistics.FIVE_DAY);
				List<LocalDate> timePoints = fiveDayMovingAvg.getTimePoints();
				Serie2d serie = chart.getSerie("5-day", Serie2d.Type.LINE);
				timePoints.forEach(timePoint -> serie.add(timePoint.toEpochDay(), fiveDayMovingAvg.getValue(timePoint)));
				serie.setColor(Color.RED);
			}
			{
				TimeSeries<LocalDate> thirtyDayMovingAvg = statistics.getMovingAverage(atmOffset, VolatilitySurfaceTimeSeriesStatistics.THIRTY_DAY);
				List<LocalDate> timePoints = thirtyDayMovingAvg.getTimePoints();
				Serie2d serie = chart.getSerie("30-day", Serie2d.Type.LINE);
				timePoints.forEach(timePoint -> serie.add(timePoint.toEpochDay(), thirtyDayMovingAvg.getValue(timePoint)));
				serie.setColor(Color.GREEN);
			}
			{
				TimeSeries<LocalDate> ninetyDayMovingAvg = statistics.getMovingAverage(atmOffset, VolatilitySurfaceTimeSeriesStatistics.NINETY_DAY);
				List<LocalDate> timePoints = ninetyDayMovingAvg.getTimePoints();
				Serie2d serie = chart.getSerie("90-day", Serie2d.Type.LINE);
				timePoints.forEach(timePoint -> serie.add(timePoint.toEpochDay(), ninetyDayMovingAvg.getValue(timePoint)));
				serie.setColor(Color.BLUE);
			}
		}
		
        return chart;
      }
	
	private Object[][] getSummaryData(VolatilitySurfaceTimeSeriesStatistics statistics, int numColumns) {
		Set<Double> atmOffsets = statistics.getAtmOffsets();
		Object[][] data = new Object[atmOffsets.size()][numColumns];
		int row = 0;
		for (double atmOffset : atmOffsets) {
			data[row][0] = atmOffset;
			data[row][1] = statistics.getMeanImpliedVol(atmOffset);
			data[row][2] = statistics.getMinImpliedVol(atmOffset);
			data[row][3] = statistics.getMaxImpliedVol(atmOffset);
			++row;
		}
		return data;
	}
	
	

}
