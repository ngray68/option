package com.ngray.option.mongo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.ngray.option.ig.price.IGPriceSnapshot;

public class Price {
	
	public static final String ID_COL = "Id";
	public static final String VALUE_DATE_COL = "ValueDate";
	public static final String OPEN_COL = "Open";
	public static final String CLOSE_COL = "Close";
	public static final String LOW_COL = "Low";
	public static final String HIGH_COL = "High";
	
	private final String id;
	private final LocalDate valueDate;
	private final double open;
	private final double close;
	private final double low;
	private final double high;
	private final LocalDateTime timestamp;
	
	
	public Price(String id, LocalDate valueDate, double open, double close, double low, double high, LocalDateTime timestamp) {
		this.id = id;
		this.valueDate = valueDate;
		this.open = open;
		this.close = close;
		this.low = low;
		this.high = high;
		this.timestamp = timestamp;
	}

	public Price(String id, LocalDate valueDate, IGPriceSnapshot historicalPrice) {
		this.id = id;
		this.valueDate = valueDate;
		this.open = historicalPrice.getOpenPrice().getMid();
		this.close = historicalPrice.getClosePrice().getMid();
		this.low = historicalPrice.getLowPrice().getMid();
		this.high = historicalPrice.getHighPrice().getMid();
		this.timestamp = LocalDateTime.parse(historicalPrice.getSnapshotTimeISOFormat());
	}

	public String getId() {
		return id;
	}

	public LocalDate getValueDate() {
		return valueDate;
	}

	public double getOpen() {
		return open;
	}

	public double getClose() {
		return close;
	}

	public double getLow() {
		return low;
	}

	public double getHigh() {
		return high;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	
	
}
