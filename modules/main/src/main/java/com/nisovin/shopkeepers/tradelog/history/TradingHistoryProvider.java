package com.nisovin.shopkeepers.tradelog.history;

import java.util.concurrent.CompletableFuture;

/**
 * Handles trading history requests.
 */
public interface TradingHistoryProvider {

	/**
	 * Retrieves the logged trades according to the given request.
	 * 
	 * @param request
	 *            the request
	 * @return the trading history result
	 */
	public CompletableFuture<TradingHistoryResult> getTradingHistory(TradingHistoryRequest request);
}
