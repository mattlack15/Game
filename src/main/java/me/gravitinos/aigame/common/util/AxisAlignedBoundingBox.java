package me.gravitinos.aigame.common.util;

public class AxisAlignedBoundingBox {

    private double sizeX;
    private double sizeY;

    private Vector position = new Vector(1,1);

    public AxisAlignedBoundingBox(double sizeX, double sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    public double getSizeX() {
        return sizeX;
    }

    public double getSizeY() {
        return sizeY;
    }

    public void updatePosition(Vector position) {
        this.position = position;
    }

    public boolean intersects(AxisAlignedBoundingBox other) {
        return !(other.position.getX() > this.position.getX() + this.sizeX
                || other.position.getX() + other.sizeX < this.position.getX()
                || other.position.getY() > this.position.getY() + this.sizeY
                || other.position.getY() + other.sizeY < this.position.getY());
    }

}
