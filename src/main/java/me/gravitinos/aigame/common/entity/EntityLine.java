package me.gravitinos.aigame.common.entity;

import me.gravitinos.aigame.common.datawatcher.DataWatcher;
import me.gravitinos.aigame.common.datawatcher.DataWatcherObject;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.util.Vector;

public class EntityLine extends GameEntity {

    public static DataWatcherObject W_COLOUR = DataWatcher.register(EntityLine.class);
    public static DataWatcherObject W_THICKNESS = DataWatcher.register(EntityLine.class);
    public static DataWatcherObject W_SHAPE = DataWatcher.register(EntityLine.class);
    public static DataWatcherObject W_DECAY = DataWatcher.register(EntityLine.class);

    public EntityLine(GameWorld world) {
        super(world);
        getDataWatcher().set(W_COLOUR, 0, 0);
        getDataWatcher().set(W_THICKNESS, 0, 0);
        getDataWatcher().set(W_SHAPE, new Vector(0, 0), 0);
        getDataWatcher().set(W_DECAY, 5);
    }

    public int getColour() {
        return getDataWatcher().get(W_COLOUR);
    }

    public int getThickness() {
        return getDataWatcher().get(W_THICKNESS);
    }

    public Vector getShape() {
        return getDataWatcher().get(W_SHAPE);
    }

    public int getDecay() {
        return getDataWatcher().get(W_DECAY);
    }

    public void setColour(int colour) {
        getDataWatcher().set(W_COLOUR, colour);
    }

    public void setThickness(int thickness) {
        getDataWatcher().set(W_THICKNESS, thickness);
    }

    public void setShape(Vector shape) {
        getDataWatcher().set(W_SHAPE, shape);
    }

    public void setDecay(int decay) {
        getDataWatcher().set(W_DECAY, decay);
    }

    @Override
    protected void doTick() {
        int colour = getColour();
        int opacity = colour >>> 24;
        colour &= 0xFFFFFF;
        if((opacity -= 10) <= 0) {
            this.remove();
            return;
        }
        colour |= opacity << 24;
        setColour(colour);
    }
}
