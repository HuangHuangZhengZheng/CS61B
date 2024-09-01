package byog.lab5;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world that contains RANDOM tiles.
 */
public class RandomWorldDemo {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private static final long SEED = 2873123; // seed
    private static final Random RANDOM = new Random(SEED); // random

    /**
     * Fills the given 2D array of tiles with RANDOM tiles.
     * @param tiles
     */
    public static void fillWithRandomTiles(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = randomTile(); // write random tiles
            }
        }
    }

    /** Picks a RANDOM tile with a 33% change of being
     *  a wall, 33% chance of being a flower, and 33%
     *  chance of being empty space.
     */
    private static TETile randomTile() { // private method
        int tileNum = RANDOM.nextInt(3); // generate random tiles, using switch case sentences
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.NOTHING;
            default: return Tileset.NOTHING;
        }
    }

    public static void main(String[] args) {
        // start a new world
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        // start a two dimension array
        TETile[][] randomTiles = new TETile[WIDTH][HEIGHT];
        // fill tiles
        fillWithRandomTiles(randomTiles);
        // render
        ter.renderFrame(randomTiles);
    }


}
