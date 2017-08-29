package com.ngray.option.ui.volsurfacetimeseries;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart2d.Chart2d;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.plot2d.primitives.Serie2d;
import org.jzy3d.plot3d.primitives.axes.layout.providers.ITickProvider;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.DateTickRenderer;

import com.ngray.option.Log;
import com.ngray.option.analysis.timeseries.TimeSeries;
import com.ngray.option.analysis.timeseries.VolatilitySurfaceTimeSeriesStatistics;
import com.ngray.option.ui.Frames;

public class VolatilitySurfaceTimeSeriesStatisticsViewer {
	
	private final JFrame parentFrame;
	private final VolatilitySurfaceTimeSeriesStatistics statistics;
	private JInternalFrame frame;
	
	public VolatilitySurfaceTimeSeriesStatisticsViewer(JFrame parentFrame, VolatilitySurfaceTimeSeriesStatistics statistics) {
		this.parentFrame = parentFrame;
		this.statistics = statistics;
		create();
	}
	
	public void show() {
		EventQueue.invokeLater(()-> {
			try {
				frame.setVisible(true);
			} catch (HeadlessException e) {
				Log.getLogger().error(e.getMessage(), e);
			}
		});
	}
	
	private void create() {
		String title = statistics.getVolatilitySurfaceName() + ": Days to Expiry=" + statistics.getDaysToExpiry();
		JInternalFrame frame = createFrame(title, statistics);
		parentFrame.add(frame);	
	}

	private JInternalFrame createFrame(String title, VolatilitySurfaceTimeSeriesStatistics statistics) {
		JPanel panel = new JPanel(new GridLayout(0,1));
		
		// table of summary data
		String[] columnNames = { "Strike(-ATM)", "Mean IV", "Min IV", "Max IV" };
		Object[][] data = getSummaryData(statistics, columnNames.length);
		JTable summaryTable = new JTable(data, columnNames);
		JScrollPane scrollPane = new JScrollPane(summaryTable);
		panel.add(scrollPane);
		
		Chart chart = createMovingAverageGraph(statistics);
		JInternalFrame frame = Frames.createJInternalFrame(title, null, panel);
		ChartLauncher.openChart(chart);
		return frame;
	}

	private Chart createMovingAverageGraph(VolatilitySurfaceTimeSeriesStatistics statistics) {
		
		Chart2d chart = new Chart2d();
		chart.getAxeLayout().setXTickRenderer(new DateTickRenderer("dd/MM/yy"));
		
		for (double atmOffset : statistics.getAtmOffsets()) {
			{
				TimeSeries<LocalDate> actual = statistics.getMovingAverage(atmOffset, 1);
				List<LocalDate> timePoints = actual.getTimePoints();
				Serie2d serie = chart.getSerie("Actual", Serie2d.Type.LINE);
				timePoints.forEach(timePoint -> serie.add(timePoint.toEpochDay(), actual.getValue(timePoint)));
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
		
		/*Component canvas = (java.awt.Component) chart.getCanvas(); 
		Border b = BorderFactory.createLineBorder(java.awt.Color.black);
        chartPanel.setBorder(b);
        chartPanel.add(canvas, BorderLayout.CENTER);*/
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
