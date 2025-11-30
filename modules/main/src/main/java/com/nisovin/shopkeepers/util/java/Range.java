package com.nisovin.shopkeepers.util.java;

/**
 * Specifies a range of elements via a start and end index.
 * <p>
 * There are different ways to determine the range's start and end indices, some of which may depend
 * on the total number of available elements, which might not be known at the time the range is
 * specified. Examples:
 * <ul>
 * <li>{@link ExplicitRange}: Specifies the start and end indices directly.
 * <li>{@link PageRange}: Spans a single page, specified by a page number and the number of elements
 * per page.
 * </ul>
 */
public interface Range {

	/**
	 * Gets the index of the first element in the range (inclusive), starting at <code>0</code>.
	 * <p>
	 * The range may depend on the total number of elements.
	 * 
	 * @param totalElements
	 *            the total number of elements
	 * @return the start index of the the range (inclusive)
	 */
	public int getStartIndex(int totalElements);

	/**
	 * Gets the end index of the range (exclusive). Must be greater than <code>startIndex</code>.
	 * <p>
	 * The range may depend on the total number of elements.
	 * 
	 * @param totalElements
	 *            the total number of elements
	 * @return the end index of the range (exclusive)
	 */
	public int getEndIndex(int totalElements);

	/**
	 * A {@link Range} with explicit bounds.
	 */
	public static class ExplicitRange implements Range {

		private final int startIndex;
		private final int endIndex;

		/**
		 * Creates a range with explicit bounds.
		 * 
		 * @param startIndex
		 *            index of the first returned record (inclusive), starting at <code>0</code>
		 * @param endIndex
		 *            upper index limit for the last returned record (exclusive), has to be greater
		 *            than <code>startIndex</code>
		 */
		public ExplicitRange(int startIndex, int endIndex) {
			Validate.isTrue(startIndex >= 0, "startIndex cannot be negative");
			Validate.isTrue(endIndex >= 0, "endIndex cannot be negative");
			Validate.isTrue(endIndex > startIndex, "endIndex must be greater than startIndex");
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}

		@Override
		public int getStartIndex(int totalElements) {
			return startIndex;
		}

		@Override
		public int getEndIndex(int totalElements) {
			return endIndex;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ExplicitRange [startIndex=");
			builder.append(startIndex);
			builder.append(", endIndex=");
			builder.append(endIndex);
			builder.append("]");
			return builder.toString();
		}
	}

	/**
	 * A {@link Range} that spans a single page, specified by a page number and the number of
	 * elements per page.
	 */
	public static class PageRange implements Range {

		// Gets trimmed to max page:
		private final int page;
		private final int elementsPerPage;

		public PageRange(int page, int elementsPerPage) {
			Validate.isTrue(page >= 1, "page must be positive");
			Validate.isTrue(elementsPerPage >= 1, "elementsPerPage must be positive");
			this.page = page;
			this.elementsPerPage = elementsPerPage;
		}

		public int getMaxPage(int totalElements) {
			return Math.max(1, (int) Math.ceil((double) totalElements / elementsPerPage));
		}

		public int getActualPage(int totalElements) {
			int maxPage = this.getMaxPage(totalElements);
			return Math.max(1, Math.min(page, maxPage));
		}

		@Override
		public int getStartIndex(int totalElements) {
			int actualPage = this.getActualPage(totalElements);
			return (actualPage - 1) * elementsPerPage;
		}

		@Override
		public int getEndIndex(int totalElements) {
			int actualPage = this.getActualPage(totalElements);
			return actualPage * elementsPerPage;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("PageRange [page=");
			builder.append(page);
			builder.append(", elementsPerPage=");
			builder.append(elementsPerPage);
			builder.append("]");
			return builder.toString();
		}
	}
}
