package me.gravitinos.aigame.client.render.entity;

import me.gravitinos.aigame.common.entity.EntityPlayer;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.util.Vector;

import java.awt.*;

public class EntityRenderPlayer extends EntityRender<EntityPlayer> {
    @Override
    public void draw(Graphics graphics, EntityPlayer entity, Vector position, double scaleFactor) {
        graphics.setColor(Color.RED);
        graphics.fillRect((int) position.getX(), (int) position.getY(), (int) (entity.getSize() * scaleFactor), (int) (entity.getSize() * scaleFactor));
    }
}
