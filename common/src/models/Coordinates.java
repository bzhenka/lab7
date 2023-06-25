package models;

import java.io.Serial;
import java.io.Serializable;

public class Coordinates implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private double x;
    private Float y;

    public Coordinates(double x, Float y) {
        this.x = x;
        this.y = y;
    }

    public Double getX() {
        return x;
    }

    public Float getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(Float y) {
        this.y = y;
    }
    @Override
    public String toString(){
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
