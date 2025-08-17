package com.dsl.simulator.Product;

public class Satellite {
    private String satelliteName;
    private int x;
    private int y;

    public String getSatelliteName() {
        return satelliteName;
    }

    public void setSatelliteName(String satelliteName) {
        this.satelliteName = satelliteName;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
    public void setpostion(int x, int y) {
        this.x=x;
        this.y=y;
    }

    @Override
    public String toString() {
        return "Satellite{" +
                "satelliteName='" + satelliteName + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }


}
