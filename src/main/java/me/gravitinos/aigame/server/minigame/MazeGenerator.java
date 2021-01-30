package me.gravitinos.aigame.server.minigame;

import me.gravitinos.aigame.common.blocks.GameBlockType;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.packet.PacketOutMapChunk;
import me.gravitinos.aigame.server.GameServer;
import me.gravitinos.aigame.server.player.ServerPlayer;

import java.security.SecureRandom;
import java.util.*;

public class MazeGenerator {

    private static class Breadcrumb<T> {
        public T data = null;
        public T data2 = null;
        public Breadcrumb<T> prev = null;
    }

    public static int[][] generate(int size) {

        int[][] maze = new int[size][size];

        Breadcrumb<Integer> crumb = new Breadcrumb<>();
        crumb.data = size-2;
        crumb.data2 = size-2;

        SecureRandom rand = new SecureRandom();

        List<Breadcrumb<Integer>> possibilities = new ArrayList<>();

        int allowedLoops = 2;

        while (crumb != null) {
            //Check available neighbors
            int x = crumb.data;
            int y = crumb.data2;

            possibilities.clear();

            //Left
            if (x - 2 >= 1) {
                if (maze[x - 2][y] < allowedLoops) {
                    Breadcrumb<Integer> n = new Breadcrumb<>();
                    n.data = x - 2;
                    n.data2 = y;
                    possibilities.add(n);
                }
            }
            if (x + 2 < size-1) {
                if (maze[x + 2][y] < allowedLoops) {
                    Breadcrumb<Integer> n = new Breadcrumb<>();
                    n.data = x + 2;
                    n.data2 = y;
                    possibilities.add(n);
                }
            }
            if (y - 2 >= 1) {
                if (maze[x][y - 2] < allowedLoops) {
                    Breadcrumb<Integer> n = new Breadcrumb<>();
                    n.data = x;
                    n.data2 = y - 2;
                    possibilities.add(n);
                }
            }
            if (y + 2 < size-1) {
                if (maze[x][y + 2] < allowedLoops) {
                    Breadcrumb<Integer> n = new Breadcrumb<>();
                    n.data = x;
                    n.data2 = y + 2;
                    possibilities.add(n);
                }
            }

            if (possibilities.isEmpty()) {
                //Back up
                crumb = crumb.prev;
                continue;
            }

            Breadcrumb<Integer> next = possibilities.get(rand.nextInt(possibilities.size()));

            //Set the wall and square to visited
            for (int i = Math.min(next.data, x); i <= Math.max(next.data, x); i++) {
                for (int j = Math.min(next.data2, y); j <= Math.max(next.data2, y); j++) {
                    maze[i][j]++;
                }
            }

            next.prev = crumb;
            crumb = next;
        }

        return maze;
    }

    public static void placeMaze(int[][] maze, GameServer server, int x, int y) {
        HashSet<Chunk> set = new HashSet<>();
        Chunk chunk = server.world.getChunkAt(x >> 4, y >> 4);

        int cx = x >> 4;
        int cy = y >> 4;

        for (int i = x; i < x + maze.length; i++) {
            for (int j = y; j < y + maze[0].length; j++) {
                if(i >> 4 != cx || j >> 4 != cy) {
                    cx = i >> 4;
                    cy = j >> 4;
                    chunk = server.world.getChunkAt(cx, cy);
                }
                set.add(chunk);
                if (maze[i-x][j-y] == 0) {
                    chunk.setBlock(i & 15, j & 15, GameBlockType.WALL);
                } else {
                    chunk.setBlock(i & 15, j & 15, GameBlockType.AIR);
                }
            }
        }
        set.forEach(c -> {
            PacketOutMapChunk chunkPacket = new PacketOutMapChunk(c);
            server.world.getPlayers().forEach(p -> {
                if (((ServerPlayer) p).getChunkMap().isLoaded((int) c.getPosition().getX(), (int) c.getPosition().getY()))
                    p.getConnection().sendPacket(chunkPacket);
            });
        });
    }
}
