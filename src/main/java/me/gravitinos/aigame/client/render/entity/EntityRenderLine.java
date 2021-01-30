package me.gravitinos.aigame.client.render.entity;

import me.gravitinos.aigame.common.entity.EntityLine;
import me.gravitinos.aigame.common.util.Vector;

import javax.sound.sampled.Line;
import java.awt.*;

public class EntityRenderLine extends EntityRender<EntityLine> {
    @Override
    public void draw(Graphics graphics, EntityLine entity, Vector position, double scaleFactor) {
        Vector position2 = position.add(entity.getShape().multiply(scaleFactor));
        Stroke s = ((Graphics2D)graphics).getStroke();
        ((Graphics2D)graphics).setStroke(new BasicStroke(entity.getThickness()));

        graphics.setColor(new Color(entity.getColour(), true));
        graphics.drawLine((int) position.getX(), (int) position.getY(), (int) position2.getX(), (int) position2.getY());

        ((Graphics2D)graphics).setStroke(s);
    }
}
