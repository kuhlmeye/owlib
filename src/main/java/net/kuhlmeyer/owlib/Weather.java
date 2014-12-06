package net.kuhlmeyer.owlib;


import java.util.Date;

public class Weather {


	private Date timestamp;
	private Double temperatureIn;
	private Double temperatureOut;
	private Integer relHumidIn;
	private Integer relHumidOut;
	private Double windchill;
	private Double windspeed;
	private Double windDirection;
	private Double dewpoint;
	private Double relPressure;
	private Double rainLast1Hour;
	private Double rainLast24Hours;
	private String tendency;
	private String forecast;

	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public Double getTemperatureIn() {
		return temperatureIn;
	}
	public void setTemperatureIn(Double temperatureIn) {
		this.temperatureIn = temperatureIn;
	}
	public Double getTemperatureOut() {
		return temperatureOut;
	}
	public void setTemperatureOut(Double temperatureOut) {
		this.temperatureOut = temperatureOut;
	}
	public Integer getRelHumidIn() {
		return relHumidIn;
	}
	public void setRelHumidIn(Integer relHumidIn) {
		this.relHumidIn = relHumidIn;
	}
	public Integer getRelHumidOut() {
		return relHumidOut;
	}
	public void setRelHumidOut(Integer relHumidOut) {
		this.relHumidOut = relHumidOut;
	}
	public Double getWindchill() {
		return windchill;
	}
	public void setWindchill(Double windchill) {
		this.windchill = windchill;
	}
	public Double getWindspeed() {
		return windspeed;
	}
	public void setWindspeed(Double windspeed) {
		this.windspeed = windspeed;
	}
	public Double getWindDirection() {
		return windDirection;
	}
	public void setWindDirection(Double windDirection) {
		this.windDirection = windDirection;
	}
	public Double getDewpoint() {
		return dewpoint;
	}
	public void setDewpoint(Double dewpoint) {
		this.dewpoint = dewpoint;
	}
	public Double getRelPressure() {
		return relPressure;
	}
	public void setRelPressure(Double relPressure) {
		this.relPressure = relPressure;
	}
	public Double getRainLast1Hour() {
		return rainLast1Hour;
	}
	public void setRainLast1Hour(Double rainLast1Hour) {
		this.rainLast1Hour = rainLast1Hour;
	}
	public Double getRainLast24Hours() {
		return rainLast24Hours;
	}
	public void setRainLast24Hours(Double rainLast24Hours) {
		this.rainLast24Hours = rainLast24Hours;
	}
	public String getTendency() {
		return tendency;
	}
	public void setTendency(String tendency) {
		this.tendency = tendency;
	}
	public String getForecast() {
		return forecast;
	}
	public void setForecast(String forecast) {
		this.forecast = forecast;
	}		

	@Override
	public String toString() {
		return "WeatherDataEntry [timestamp=" + timestamp + ", temperatureIn=" + temperatureIn + ", temperatureOut=" + temperatureOut + ", relHumidIn=" + relHumidIn
				+ ", relHumidOut=" + relHumidOut + ", windchill=" + windchill + ", windspeed=" + windspeed + ", windDirection=" + windDirection + ", dewpoint=" + dewpoint
				+ ", relPressure=" + relPressure + ", rainLast1Hour=" + rainLast1Hour + ", rainLast24Hours=" + rainLast24Hours + ", tendency=" + tendency + ", forecast="
				+ forecast + "]";
	}
}
