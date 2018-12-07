package com.yuleshchenko.weather.util;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ForecastRealm extends RealmObject {

    // Fields (columns of DB):
    @PrimaryKey
    private long dt;              // date stamp
    private String dtTxt;         // date
    private double temp;          // temperature
    private double tempMax;       // max temperature
    private double tempMin;       // mix temperature
    private String main;          // main
    private String icon;          // icon code
    private double humidity;      // humidity
    private double wind;          // wind speed (m/s)
    private double deg;           // wind direction (degree)
    private double pressure;      // pressure

    public ForecastRealm() {
    }

    // Getters and Setters:
    public long getDt() {return dt;}
    public void setDt(long dt) {this.dt = dt;}

    public String getDtTxt() { return dtTxt; }
    public void setDtTxt(String dtTxt) { this.dtTxt = dtTxt; }

    public double getTemp() { return temp; }
    public void setTemp(double temp) { this.temp = temp; }

    public double getTempMax() { return tempMax; }
    public void setTempMax(double tempMax) { this.tempMax = tempMax; }

    public double getTempMin() { return tempMin; }
    public void setTempMin(double tempMin) { this.tempMin = tempMin; }

    public String getMain() { return main; }
    public void setMain(String main) { this.main = main; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public double getHumidity() { return humidity; }
    public void setHumidity(double humidity) { this.humidity = humidity; }

    public double getWind() { return wind; }
    public void setWind(double wind) { this.wind = wind; }

    public double getDeg() { return deg; }
    public void setDeg(double deg) { this.deg = deg; }

    public double getPressure() { return pressure; }
    public void setPressure(double pressure) { this.pressure = pressure; }
}
