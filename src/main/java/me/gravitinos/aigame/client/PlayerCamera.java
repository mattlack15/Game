package me.gravitinos.aigame.client;

import lombok.Getter;
import lombok.Setter;
import me.gravitinos.aigame.common.util.Vector;

public class PlayerCamera {

    public static final double BASE_SCALE_MULTIPLIER = 20D;

    @Getter
    @Setter
    private Vector position;

    @Getter
    @Setter
    private double width;

    @Getter
    @Setter
    private double height;

    @Getter
    @Setter
    private double scale;

    public PlayerCamera(Vector position, double width, double height, double scale) {
        this.position = position;
        System.out.println("Width: " + width + " (" + (width / 2D) + ") height: " + height + "(" + (height / 2D) + ")");
        this.width = width;
        this.height = height;
        this.scale = scale;
    }

    public double scale(int pixels) {
        return pixels / BASE_SCALE_MULTIPLIER / scale;
    }

    public double scale(double worldLengths) {
        return worldLengths * (Math.floor(BASE_SCALE_MULTIPLIER * scale));
    }

    public static double scale(int pixels, double scale) {
        return pixels / BASE_SCALE_MULTIPLIER / scale;
    }

    public static double scale(double worldLengths, double scale) {
        return worldLengths * BASE_SCALE_MULTIPLIER * scale;
    }
    int i = 0;

    public Vector toScreenCoordinates(Vector vec) {
        double x = vec.getX();
        double y = vec.getY();
        double flooredScale = Math.floor(BASE_SCALE_MULTIPLIER * scale);
        return new Vector((x - position.getX() + width/2D) * flooredScale, (y - position.getY() + height/2D) * flooredScale);
    }

    public Vector fromScreenCoordinates(Vector vec) {
        double x = vec.getX();
        double y = vec.getY();
        return new Vector(x / BASE_SCALE_MULTIPLIER / scale - width / 2D + position.getX(), y / BASE_SCALE_MULTIPLIER / scale - height / 2D + position.getY());
    }

}
