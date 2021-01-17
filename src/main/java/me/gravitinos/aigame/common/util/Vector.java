package me.gravitinos.aigame.common.util;

public class Vector {
    private final double x;
    private final double y;
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public Vector setX(double x) {
        return new Vector(x, this.y);
    }

    public Vector setY(double y) {
        return new Vector(this.x, y);
    }

    public Vector add(Vector vec) {
        return new Vector(x + vec.x, y + vec.y);
    }

    public Vector add(double x, double y) {
        return new Vector(this.x + x, this.y + y);
    }

    public Vector subtract(Vector vec) {
        return new Vector(x - vec.x, y - vec.y);
    }
    public Vector multiply(Vector vec) {
        return new Vector(x * vec.x, y * vec.y);
    }
    public Vector divide(Vector vec) {
        return new Vector(x / vec.x, y / vec.y);
    }
    public Vector add(double num) {
        return new Vector(x + num, y + num);
    }
    public Vector subtract(double num) {
        return new Vector(x - num, y - num);
    }
    public Vector multiply(double num) {
        return new Vector(x * num, y * num);
    }
    public boolean isZero() {
        return this.x == 0D && this.y == 0D;
    }
    public Vector divide(double num) {
        return new Vector(x / num, y / num);
    }
    public double distance(Vector vec) {
        return Math.sqrt(Math.pow(vec.x - this.x, 2.0D) + Math.pow(vec.y - this.y, 2.0D));
    }
    public double distanceSquared(Vector vec)  {
        return ((vec.x - this.x) * (vec.x - this.x)) + ((vec.y - this.y) * (vec.y - this.y));
    }
    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }
    public Vector normalize() {
        return this.divide(this.length());
    }

    @Override
    public boolean equals(Object o) {
        if(o == this)
            return true;
        if(o instanceof Vector) {
            boolean result = ((Vector) o).x == this.x && ((Vector) o).y == this.y;
            return result;
        }
        return false;
    }

    public Vector floor() {
        return new Vector(Math.floor(this.x), Math.floor(this.y));
    }
    public Vector ceil() {
        return new Vector(Math.ceil(this.x), Math.ceil(this.y));
    }

    public Vector round() {
        return new Vector(Math.round(this.x), Math.round(this.y));
    }

    public Vector round(int decimalPlaces) {
        double d = Math.pow(10, decimalPlaces);
        return new Vector(Math.round(this.x * d) / d, Math.round(this.y * d) / d);
    }

    public double sum() {
        return x + y;
    }

    public Vector abs() {
        return new Vector(Math.abs(this.x), Math.abs(this.y));
    }

    @Override
    public String toString() {
        return "{" + this.x + ", " + this.y + "}";
    }
}
