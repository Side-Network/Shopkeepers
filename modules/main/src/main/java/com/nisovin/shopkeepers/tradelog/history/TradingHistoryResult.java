package com.nisovin.shopkeepers.tradelog.history;

import java.util.List;

import com.nisovin.shopkeepers.tradelog.data.TradeRecord;
import com.nisovin.shopkeepers.util.java.Validate;

public class TradingHistoryResult {

	private final List<TradeRecord> trades;
	private final int totalTradesCount;

	public TradingHistoryResult(
			List<TradeRecord> trades,
			int totalTradesCount
	) {
		Validate.notNull(trades, "trades is null!");
		Validate.noNullElements(trades, "trades cannot contain null!");
		Validate.isTrue(totalTradesCount >= 0, "Total trades count cannot be negative!");
		this.trades = trades;
		this.totalTradesCount = totalTradesCount;
	}

	/**
	 * @return the trades, not <code>null</code> but can be empty
	 */
	public List<TradeRecord> getTrades() {
		return trades;
	}

	/**
	 * @return the total number of matching trades
	 */
	public int getTotalTradesCount() {
		return totalTradesCount;
	}
}
