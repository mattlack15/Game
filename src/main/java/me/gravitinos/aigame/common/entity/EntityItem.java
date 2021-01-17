package me.gravitinos.aigame.common.entity;

import lombok.Getter;
import lombok.Setter;
import me.gravitinos.aigame.client.PlayerCamera;
import me.gravitinos.aigame.common.item.ItemStack;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.util.Vector;

import java.awt.*;

public class EntityItem extends GameEntity{

    @Getter
    @Setter
    private ItemStack itemStack;

    @Getter
    @Setter
    private double scale = 0.8;

    public EntityItem(GameWorld world, ItemStack itemStack) {
        super(world);
        this.itemStack = itemStack;
    }

    @Override
    protected void doTick() {
        if(scale < 0.8) {
            scale += 0.02D;
        }
    }

}
