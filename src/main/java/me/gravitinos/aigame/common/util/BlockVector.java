package me.gravitinos.aigame.common.util;

import java.util.Objects;

public class BlockVector {
    private final int x;
    private final int y;
    public BlockVector(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public BlockVector setX(int x) {
        return new BlockVector(x, this.y);
    }

    public BlockVector setY(int y) {
        return new BlockVector(this.x, y);
    }

    public BlockVector add(BlockVector vec) {
        return new BlockVector(x + vec.x, y + vec.y);
    }
    public BlockVector subtract(BlockVector vec) {
        return new BlockVector(x - vec.x, y - vec.y);
    }
    public BlockVector multiply(BlockVector vec) {
        return new BlockVector(x * vec.x, y * vec.y);
    }
    public BlockVector divide(BlockVector vec) {
        return new BlockVector(x / vec.x, y / vec.y);
    }
    public BlockVector add(int num) {
        return new BlockVector(x + num, y + num);
    }
    public BlockVector subtract(int num) {
        return new BlockVector(x - num, y - num);
    }
    public BlockVector multiply(int num) {
        return new BlockVector(x * num, y * num);
    }
    public BlockVector divide(int num) {
        return new BlockVector(x / num, y / num);
    }
    public double distance(BlockVector vec) {
        return Math.sqrt(Math.pow(vec.x - this.x, 2.0D) + Math.pow(vec.y - this.y, 2.0D));
    }
    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }

    @Override
    public boolean equals(Object o) {
        if(o == this)
            return true;
        if(o instanceof BlockVector) {
            boolean result = ((BlockVector) o).x == this.x && ((BlockVector) o).y == this.y;
            return result;
        }
        return false;
    }

    @Override
    public String toString() {
        return "BlockVector{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
