package com.monk;

import com.monk.spi.MonitoringBackend;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.pmw.tinylog.Logger;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Plugin to use InfluxDB as MonitoringBackend
 * <p>
 * Uses dependency influxdb-java to establish connection
 *
 * @author ahatzold on 22.08.2017
 */
public class Influx implements MonitoringBackend {

	private InfluxDB influxDB;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void establishConnection(String connectionString, String username, String password) {
		this.influxDB = InfluxDBFactory.connect(connectionString, username, password);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pushSinglePoint(String measurement, Map<String, Double> fields, String timestamp, String extra) {

		//If no timestamp is given, use the current time
		//and convert the string to long in order to use it
		long tmstp;
		if (timestamp.isEmpty()) {
			Logger.info("No timestamp given. Using the current time.");
			tmstp = System.currentTimeMillis();
		} else {
			tmstp = Long.parseLong(timestamp);
		}

		//create the pointBuilder
		Point.Builder pointBuilder = Point.measurement(measurement)
				.time(tmstp, TimeUnit.MILLISECONDS);

		//and add all necessary fields
		StringBuilder fieldsForLog = new StringBuilder();
		for (Map.Entry<String, Double> entry : fields.entrySet()) {
			pointBuilder.addField(entry.getKey(), entry.getValue());
			fieldsForLog.append(entry.getKey());
			fieldsForLog.append("=");
			fieldsForLog.append(entry.getValue());
		}

		//last build the point
		Point point = pointBuilder.build();

		influxDB.setDatabase(getInfoFromExtra("db", extra));

		//write the point to the database
		Logger.info("Pushing following point: " +
				"measurement: " + measurement + ", " +
				"fields: " + fieldsForLog + ", " +
				"timestamp: " + tmstp);
		influxDB.write(point);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void closeConnection() {
		influxDB.close();
	}

	private String getInfoFromExtra(String tag, String extra) {

		//split the extra string
		if (!extra.isEmpty()) {
			String[] allExtras = extra.split("\\$");
			for (String info : allExtras) {
				String[] singleExtra = info.split("=");
				String key = singleExtra[0];
				String value = singleExtra[1];
				//and if you see the key = db
				//use it as the current database
				if (key.equals(tag)) {
					Logger.info("Using database '" + value + "' for this query");
					return value;
				} else {
					Logger.info("No database name given. Please specify one in 'extra'.");
				}
			}
		} else {
			Logger.info("'Extra' is empty. This can cause problems when using InfluxDB.");
		}
		return null;
	}

}
