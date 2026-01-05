package com.nisovin.shopkeepers.tradelog.history;

import com.nisovin.shopkeepers.util.java.Range;
import com.nisovin.shopkeepers.util.java.Validate;

public class TradingHistoryRequest {

	public final PlayerSelector playerSelector; // Not null
	public final ShopSelector shopSelector; // Not null
	public final Range range; // Not null

	/**
	 * Creates a {@link TradingHistoryRequest} for logged trades in the specified range matching the
	 * given criteria.
	 * 
	 * @param playerSelector
	 *            specifies the involved trading player(s), not <code>null</code>
	 * @param shopSelector
	 *            specifies the involved shop(s), not <code>null</code>
	 * @param range
	 *            the range of records to retrieve, not <code>null</code>
	 */
	public TradingHistoryRequest(PlayerSelector playerSelector, ShopSelector shopSelector, Range range) {
		Validate.notNull(playerSelector, "playerSelector is null");
		Validate.notNull(shopSelector, "shopSelector is null");
		Validate.notNull(range, "range is null");
		this.playerSelector = playerSelector;
		this.shopSelector = shopSelector;
		this.range = range;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TradingHistoryRequest [playerSelector=");
		builder.append(playerSelector);
		builder.append(", shopSelector=");
		builder.append(shopSelector);
		builder.append(", range=");
		builder.append(range);
		builder.append("]");
		return builder.toString();
	}
}
