package me.gravitinos.aigame.server.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerChunkMap {
    private static class ChunkLoc {
        public final int x;
        public final int y;
        public ChunkLoc(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private Map<ChunkLoc, Object> loadedChunks = new ConcurrentHashMap<>();

    public boolean isLoaded(int cx, int cy) {
        return loadedChunks.containsKey(new ChunkLoc(cx, cy));
    }

    public void setLoaded(int cx, int cy) {
        loadedChunks.put(new ChunkLoc(cx, cy), null);
    }

    public void clean(int x, int y, int renderDistance) {
        List<ChunkLoc> locations = new ArrayList<>(loadedChunks.keySet());
        for (ChunkLoc loc : locations) {
            if(Math.abs(loc.x - x) > renderDistance || Math.abs(loc.y - y) > renderDistance) {
                loadedChunks.remove(loc);
            }
        }
    }
}
