package me.gravitinos.aigame.client.render.entity;

import me.gravitinos.aigame.client.PlayerCamera;
import me.gravitinos.aigame.common.entity.EntityFire;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.util.Vector;

import java.awt.*;

public class EntityRenderFire extends EntityRender<EntityFire> {
    @Override
    public void draw(Graphics graphics, EntityFire entity, Vector position, double scaleFactor) {
        graphics.setColor(new Color(entity.color));
        graphics.fillOval((int) position.getX(), (int) position.getY(),
                (int) (entity.size * scaleFactor / PlayerCamera.BASE_SCALE_MULTIPLIER),
                (int) (entity.size * scaleFactor / PlayerCamera.BASE_SCALE_MULTIPLIER));
    }
}
