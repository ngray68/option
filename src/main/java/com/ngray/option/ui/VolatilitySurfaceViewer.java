package com.ngray.option.ui;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart.SwingChart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.axes.AxeBox;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.text.renderers.TextBitmapRenderer;
import org.jzy3d.plot3d.builder.Builder;

import com.jogamp.opengl.util.gl2.GLUT;
import com.ngray.option.Log;
import com.ngray.option.mongo.MongoCache;
import com.ngray.option.mongo.MongoCacheRegistry;
import com.ngray.option.mongo.MongoCacheRegistryException;
import com.ngray.option.volatilitysurface.VolatilitySurface;
import com.ngray.option.volatilitysurface.VolatilitySurfaceException;

public class VolatilitySurfaceViewer {
	
	private final MainUI parentUI;
	private final VolatilitySurface volatilitySurface;
	
	public VolatilitySurfaceViewer(MainUI parentUI, VolatilitySurface volatilitySurface) {
		this.parentUI = parentUI;
		this.volatilitySurface = volatilitySurface;
	}
	
	public void create() {
		String title = volatilitySurface.getUniqueId();
		JInternalFrame frame = createFrame(title, volatilitySurface);
		parentUI.getDesktopPane().add(frame);
	}
	
	private JInternalFrame createFrame(String title, VolatilitySurface volatilitySurface) {
		VolatilitySurfaceTableModel model = new VolatilitySurfaceTableModel(volatilitySurface);
		JTable table = new JTable(model);
		JScrollPane pane = new JScrollPane(table);
		JPanel panel = new JPanel();
		panel.add(pane);
		JButton viewButton = new JButton("View Surface");
		JButton saveButton = new JButton("Save Surface");
		panel.add(viewButton);
		panel.add(saveButton);
		saveButton.addActionListener(e -> saveSurface(volatilitySurface));
		viewButton.addActionListener(e -> viewSurface(volatilitySurface));
		JInternalFrame frame = Frames.createJInternalFrame(title, null, panel);
		return frame;
	}

	private void viewSurface(VolatilitySurface volatilitySurface) {
		if (volatilitySurface == null) {
			Log.getLogger().warn("Attempt to view null volatility surface ignored");
			return;
		}
		Log.getLogger().info("Viewing volatility surface " + volatilitySurface.getUniqueId());
		// Define a function to plot
		Mapper mapper = new Mapper() {
		    public double f(double x, double y) {
		        try {
					return volatilitySurface.getImpliedVolatility(x, y);
				} catch (VolatilitySurfaceException e) {
					Log.getLogger().warn(e.getMessage(), e);
					return 0.0;
				}
		    }
		};

		// Define range and precision for the function to plot
		Range rangeX = new Range((float)volatilitySurface.getMinDaysToExpiry(), (float)volatilitySurface.getMaxDaysToExpiry());
		Range rangeY = new Range((float)volatilitySurface.getMinStrikeOffset(), (float)volatilitySurface.getMaxStrikeOffset());
		int steps = 50;

		// Create a surface drawing that function
		Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(rangeX, steps, rangeY, steps), mapper);
		surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
		surface.setFaceDisplayed(true);
		surface.setWireframeDisplayed(true);
		surface.setWireframeColor(Color.BLACK);
	

		// Create a chart and add the surface
		Chart chart = AWTChartComponentFactory.chart(Quality.Advanced);	
		chart.getScene().getGraph().add(surface);
		((AxeBox)chart.getView().getAxe()).setTextRenderer(new TextBitmapRenderer()  {
				{
		          font = GLUT.BITMAP_HELVETICA_18;
		          fontHeight = 18;
				}
	        });
		chart.getAxeLayout().setXAxeLabel("Days to Expiry");
		chart.getAxeLayout().setYAxeLabel("Strike Offset");
		chart.getAxeLayout().setZAxeLabel("Implied Vol");
		ChartLauncher.openChart(chart);
	}

	private void saveSurface(VolatilitySurface volatilitySurface) {
		try {
			if (volatilitySurface == null) {
				Log.getLogger().warn("Attempt to save null volatility surface ignored");
				return;
			}
			Log.getLogger().info("Saving volatility surface " + volatilitySurface.getUniqueId() + " to Mongo DB");
			MongoCache<VolatilitySurface> cache = MongoCacheRegistry.get(VolatilitySurface.class);
			cache.put(volatilitySurface, true);
		} catch (MongoCacheRegistryException e) {
			Log.getLogger().error(e.getMessage(), e);
		}
	}

}
