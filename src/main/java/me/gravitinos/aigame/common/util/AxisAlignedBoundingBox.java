package me.gravitinos.aigame.common.util;

public class AxisAlignedBoundingBox {

    private double sizeX;
    private double sizeY;

    private Vector position = new Vector(0,0);

    public AxisAlignedBoundingBox(double sizeX, double sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    public void updatePosition(Vector position) {
        this.position = position;
    }

    public boolean intersects(AxisAlignedBoundingBox other) {

    }

}
