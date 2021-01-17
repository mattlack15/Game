package me.gravitinos.aigame.common.item;

import lombok.Getter;
import me.gravitinos.aigame.client.PlayerCamera;
import me.gravitinos.aigame.common.util.Vector;

import java.awt.*;

public enum Material {

    WALL(1, (vec1, graphics, scale) -> {
        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect((int)Math.floor(vec1.getX()), (int)Math.floor(vec1.getY()), (int)Math.floor(PlayerCamera.scale(1D, scale)), (int)Math.floor(PlayerCamera.scale(1D, scale)));
        graphics.setColor(Color.BLACK);
        graphics.drawRect((int)Math.floor(vec1.getX()), (int)Math.floor(vec1.getY()), (int)Math.floor(PlayerCamera.scale(1D, scale)), (int)Math.floor(PlayerCamera.scale(1D, scale)));

    });

    public interface TriConsumer<P,N,S> {
        void accept(P e, N i, S x);
    }

    @Getter
    private int id;

    @Getter
    private TriConsumer<Vector, Graphics, Double> drawFunc;

    Material(int id, TriConsumer<Vector, Graphics, Double> drawFunc) {
        this.id = id;
        this.drawFunc = drawFunc;
    }
}
