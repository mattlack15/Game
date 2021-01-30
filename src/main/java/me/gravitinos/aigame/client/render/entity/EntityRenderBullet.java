package me.gravitinos.aigame.client.render.entity;

import me.gravitinos.aigame.common.entity.EntityBullet;
import me.gravitinos.aigame.common.util.Vector;

import java.awt.*;

public class EntityRenderBullet extends EntityRender<EntityBullet> {
    @Override
    public void draw(Graphics graphics, EntityBullet entity, Vector position, double scaleFactor) {
        graphics.setColor(Color.orange);
        graphics.fillOval((int) position.getX(), (int) position.getY(), (int) (0.3 * scaleFactor), (int) (0.3 * scaleFactor));
    }
}
