package me.gravitinos.aigame.server.command.cmds;

import me.gravitinos.aigame.common.blocks.GameBlockType;
import me.gravitinos.aigame.common.entity.EntityPlayer;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.util.Vector;
import me.gravitinos.aigame.server.GameServer;
import me.gravitinos.aigame.server.command.Command;
import me.gravitinos.aigame.server.minigame.MazeGenerator;

import java.util.ArrayList;
import java.util.List;

public class CmdMaze extends Command {

    private List<Vector> mazeEndPoints = new ArrayList<>();

    public CmdMaze() {
        this.addAlias("maze");
    }

    @Override
    protected void execute() {
        long ms = System.currentTimeMillis();

        //Generate a maze
        int[][] maze = MazeGenerator.generate(41);

        long genMs = System.currentTimeMillis() - ms;

        //Place the maze
        MazeGenerator.placeMaze(maze, GameServer.getServer(), -60, -60);

        //Move everyone there
        for (EntityPlayer worldPlayer : GameServer.getServer().world.getPlayers()) {
            worldPlayer.setPosition(new Vector(-58.9, -58.9));
        }

        //Clear the endpoints and add a new root endpoint
        mazeEndPoints.clear();
        mazeEndPoints.add(new Vector(-59, -59));

        ms = System.currentTimeMillis() - ms;

        getPlayer().sendMessage("Created a 41x41 maze in " + ms + "ms. (gen " + genMs + "ms)");
    }

    public void tick() {
        if (mazeEndPoints.isEmpty()) {
            return;
        }

        List<Chunk> c = new ArrayList<>();

        while (mazeEndPoints.size() > 10)
            mazeEndPoints.remove(0);

        for (Vector mazeEndPoint : new ArrayList<>(mazeEndPoints)) {
            int[] arr = {-1, -1};
            for (int i = 0; i < 4; i++) {

                Vector v = new Vector(arr[0], arr[1]);

                if (GameServer.getServer().world.getBlockAt(v) == GameBlockType.AIR) {
                    GameServer.getServer().world.setBlockAt(v, GameBlockType.LIGHT_GREEN);
                    Chunk chunk = GameServer.getServer().world.getChunkAt((int) v.getX() >> 4, (int) v.getY() >> 4);
                    if (!c.contains(chunk)) {
                        c.add(chunk);
                    }
                    mazeEndPoints.add(v);
                }

                for (int j = 0; j < arr.length; j++) {
                    int a = arr[0];
                    arr[0] = -arr[0];
                    if (a == -1)
                        break;
                }
            }

            mazeEndPoints.remove(mazeEndPoint);


        }
    }
}
