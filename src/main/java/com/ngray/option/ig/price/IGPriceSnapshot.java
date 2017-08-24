package com.ngray.option.ig.price;

import com.google.gson.Gson;

/**
 * This class encapsulates a HistoricalPrice snapshot. It captures
 * the high, low, open and close over a given time period. The time period
 * is not part of the price data, though the end-time of the period is.
 * @author nigelgray
 *
 */
public final class IGPriceSnapshot {

	private IGPrice openPrice;
	private IGPrice closePrice;
	private IGPrice highPrice;
	private IGPrice lowPrice;
	private String snapshotTime;
	private String snapshotTimeUTC;
	private int lastTradedVolume;
	
	
	
	/**
	 * Argument-less constructor needed for Json 
	 */
	public IGPriceSnapshot() {
	}
	
	/**
	 * Private constructor used by the Builder class
	 * @param openPrice
	 * @param closePrice
	 * @param highPrice
	 * @param lowPrice
	 * @param snapshotTime
	 * @param snapshotTimeUTC
	 * @param lastTradedVolume
	 */
	private IGPriceSnapshot(IGPrice openPrice, IGPrice closePrice, IGPrice highPrice, IGPrice lowPrice, String snapshotTime, String snapshotTimeUTC, int lastTradedVolume) {
		this.setOpenPrice(openPrice);
		this.setClosePrice(closePrice);
		this.setHighPrice(highPrice);
		this.setLowPrice(lowPrice);
		this.setSnapshotTime(snapshotTime);
		this.setSnapshotTimeUTC(snapshotTimeUTC);
		this.setLastTradedVolume(lastTradedVolume);
	}

	public static IGPriceSnapshot fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, IGPriceSnapshot.class);
	}
	
	public String asJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	public static String getAttributeNames() {
		return "Snapshot Time" + "," + "Open" + ","	 + "Close" + "," + "High" + "," + "Low" + "," + "Volume";
		}
	
	@Override
	public String toString() {
		return getSnapshotTime() + "," + getOpenPrice().getMid() + "," + getClosePrice().getMid() + "," + getHighPrice().getMid() + "," + getLowPrice().getMid() + "," + getLastTradedVolume();
	}
	
	public IGPrice getOpenPrice() {
		return openPrice;
	}

	public void setOpenPrice(IGPrice openPrice) {
		this.openPrice = openPrice;
	}

	public IGPrice getClosePrice() {
		return closePrice;
	}

	public void setClosePrice(IGPrice closePrice) {
		this.closePrice = closePrice;
	}

	public IGPrice getHighPrice() {
		return highPrice;
	}

	public void setHighPrice(IGPrice highPrice) {
		this.highPrice = highPrice;
	}

	public IGPrice getLowPrice() {
		return lowPrice;
	}

	public void setLowPrice(IGPrice lowPrice) {
		this.lowPrice = lowPrice;
	}

	public String getSnapshotTime() {
		return snapshotTime;
	}
	
	/*public String getSnapshotTimeISOFormat() {
		return snapshotTime.replaceAll("-", "T")
				           .replaceFirst(":", "-")
				           .replaceFirst(":", "-");
	}*/
	
	public void setSnapshotTime(String snapshotTime) {
		this.snapshotTime = snapshotTime;
	}
	
	public String getSnapshotTimeUTC() {
		return snapshotTimeUTC;
	}
	
	public void setSnapshotTimeUTC(String snapshotTimeUTC) {
		this.snapshotTimeUTC = snapshotTimeUTC;
	}

	public int getLastTradedVolume() {
		return lastTradedVolume;
	}

	public void setLastTradedVolume(int lastTradedVolume) {
		this.lastTradedVolume = lastTradedVolume;
	}

	public final class Builder {
		
		private IGPrice openPrice;
		private IGPrice closePrice;
		private IGPrice highPrice;
		private IGPrice lowPrice;
		private String snapshotTime;
		private String snapshotTimeUTC;
		private int lastTradedVolume;
		
		public Builder() {
			
		}
		
		public IGPriceSnapshot build() {
			return new IGPriceSnapshot(
						getOpenPrice(),
						getClosePrice(),
						getHighPrice(),
						getLowPrice(),
						getSnapshotTime(),
						getSnapshotTimeUTC(),
						getLastTradedVolume()
					);
		}
		
		public IGPrice getOpenPrice() {
			return openPrice;
		}

		public Builder setOpenPrice(IGPrice openPrice) {
			this.openPrice = openPrice;
			return this;
		}

		public IGPrice getClosePrice() {
			return closePrice;
		}

		public Builder setClosePrice(IGPrice closePrice) {
			this.closePrice = closePrice;
			return this;
		}

		public IGPrice getHighPrice() {
			return highPrice;
		}

		public Builder setHighPrice(IGPrice highPrice) {
			this.highPrice = highPrice;
			return this;
		}

		public IGPrice getLowPrice() {
			return lowPrice;
		}

		public Builder setLowPrice(IGPrice lowPrice) {
			this.lowPrice = lowPrice;
			return this;
		}

		public String getSnapshotTime() {
			return snapshotTime;
		}

		public Builder setSnapshotTime(String snapshotTime) {
			this.snapshotTime = snapshotTime;
			return this;
		}

		public int getLastTradedVolume() {
			return lastTradedVolume;
		}

		public Builder setLastTradedVolume(int lastTradedVolume) {
			this.lastTradedVolume = lastTradedVolume;
			return this;
		}

		public String getSnapshotTimeUTC() {
			return snapshotTimeUTC;
		}

		public void setSnapshotTimeUTC(String snapshotTimeUTC) {
			this.snapshotTimeUTC = snapshotTimeUTC;
		}		
	}
}
