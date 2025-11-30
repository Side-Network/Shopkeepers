package com.nisovin.shopkeepers.util.java;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcUtils {

	private JdbcUtils() {
	}

	public static void setParameters(
			PreparedStatement preparedStatement,
			int parameterIndexOffset,
			Object... parameters
	) throws SQLException {
		Validate.notNull(preparedStatement, "preparedStatement is null");
		Validate.notNull(parameters, "parameters is null!");
		Validate.isTrue(parameterIndexOffset >= 0, "parameterIndexOffset must be positive!");

		int index = 1 + parameterIndexOffset;
		for (Object parameter : parameters) {
			preparedStatement.setObject(index, parameter);
			++index;
		}
	}
}
