package byog.lab5;
import org.junit.Test;

import static org.junit.Assert.*;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

import java.util.Objects;
import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 * 但是首先得搞清楚怎么画六边形的图案！
 */
public class HexWorld {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;
    private static final Random RANDOM = new Random();
    public static class Position{
        int x, y;
        public Position(int m, int n) {
            x = m; // ()
            y = n;
        }
    }
//The process I used to write my hexagon building code:
//1. Realized that a hexagon is just a stack of rows of tiles with a very specific pattern, where each row is determined by:
//  a. Its width.
//  b. Its xOffset.
//
//2. Wrote methods for calculating width and xOffset of each row of a hexagon, along with JUnit tests to verify that
// these worked. The reason I chose to do JUnit tests is because that it was really easy, and I knew that
// it would make my life a lot better when I started visually debugging my hexagon code if I knew that
// I could trust these methods.
//
//3. Wrote the addHexagon method with a double nested for loop that tried to draw hexagons.
// Debugged for a while until it seemed to work. Unlike above, I did not bother writing JUnit tests,
// because writing the tests is just as hard as writing the actual method (think about why this is).
// Instead, I just wrote a main method that drew hexagons and made sure they looked good to my eyeballs.
//
//4. Changed the signature of addHexagon to take Position objects instead of x and y coordinates,
// for reasons that will become clear in part 2 of this lab.
//
//5. Revised my double for loop so that it uses a helper method that draws each row, so that
// my code is easier to read and maintain (and use for students).
//
//Note this solution uses exceptions, which we may not have covered at the time you're reading this.

    /**
     * Computes the width of row i for a size s hexagon.
     * @param s The size of the hex.
     * @param i The row number where i = 0 is the bottom row.
     * @return
     */
    public static int hexRowWidth(int s, int i) { // calculating width of row
        int effectiveI = i; // 有效的I
        if (i >= s) {
            effectiveI = 2 * s - 1 - effectiveI;
        }

        return s + 2 * effectiveI;
    }

    /**
     * Computes relative x coordinate of the leftmost tile in the ith
     * row of a hexagon, assuming that the bottom row has an x-coordinate
     * of zero. For example, if s = 3, and i = 2, this function
     * returns -2, because the row 2 up from the bottom starts 2 to the left
     * of the start position, e.g.
     *   xxxx
     *  xxxxxx
     * xxxxxxxx
     * xxxxxxxx <-- i = 2, starts 2 spots to the left of the bottom of the hex
     *  xxxxxx
     *   xxxx
     *
     * @param s size of the hexagon
     * @param i row num of the hexagon, where i = 0 is the bottom
     * @return
     */
    public static int hexRowOffset(int s, int i) { // 计算每行的起始点（相对于最底下的那一行）
        int effectiveI = i;
        if (i >= s) {
            effectiveI = 2 * s - 1 - effectiveI;
        }
        return -effectiveI;
    }

    /** Adds a row of the same tile.
     * @param world the world to draw on
     * @param p the leftmost position of the row 最左行的位置
     * @param width the number of tiles wide to draw
     * @param t the tile to draw
     */
    public static void addRow(TETile[][] world, Position p, int width, TETile t) {

        for (int xi = 0; xi < width; xi += 1) {
            int xCoord = p.x + xi;
            int yCoord = p.y;
            world[xCoord][yCoord] = TETile.colorVariant(t, 32, 32, 32, RANDOM);
        }
    }

    /**
     * Adds a hexagon to the world.
     * @param world the world to draw on
     * @param p the bottom left coordinate of the hexagon
     * @param s the size of the hexagon
     * @param t the tile to draw
     */
    public static void addHexagon(TETile[][] world, Position p, int s, TETile t) {
        // 画一个六边形
        if (s < 2) {
            throw new IllegalArgumentException("Hexagon must be at least size 2.");
        }
        // hexagons have 2*s rows. this code iterates up from the bottom row,
        // which we call row 0.
        for (int yi = 0; yi < 2 * s; yi += 1) {
            int thisRowY = p.y + yi;

            int xRowStart = p.x + hexRowOffset(s, yi);
            Position rowStartP = new Position(xRowStart, thisRowY);

            int rowWidth = hexRowWidth(s, yi);

            addRow(world, rowStartP, rowWidth, t);

        }
    }



    // 检查是否可以画
    public static boolean isDrawable(TETile[][] world, Position p, int s) {
        int W =world.length; // 参考RandomWorldDemo
        int H = world[0].length;
        // 越界
        if(p.x < s-1 || p.x > W-2*s+1 || p.y < 0 || p.y > H-2*s){
            return false;
        }
        // 检查size
        if (s < 2) {
            throw new IllegalArgumentException("Hexagon must be at least size 2.");
        }
        // 检查界内是否可以绘制六边形
        // hexagons have 2*s rows. this code iterates up from the bottom row,
        // which we call row 0.
        for (int yi = 0; yi < 2 * s; yi += 1) {
            int thisRowY = p.y + yi;

            int xRowStart = p.x + hexRowOffset(s, yi);
            Position rowStartP = new Position(xRowStart, thisRowY);

            int rowWidth = hexRowWidth(s, yi);

            /*
             *  check whether is drawn or not.
             */
            for (int xi = 0; xi < rowWidth; xi += 1) {
            int xCoord = rowStartP.x + xi;
            int yCoord = rowStartP.y;
            if (!Objects.equals(world[xCoord][yCoord].description(), "nothing")) {
                return false;
            }
        }
        }

        return true;
    }
    // random tiles
    private static TETile randomTile() { // private method
        int tileNum = RANDOM.nextInt(7); // generate random tiles, using switch case sentences
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.WATER;
            case 3: return Tileset.GRASS;
            case 4: return Tileset.SAND;
            case 5: return Tileset.TREE;
            default: return Tileset.MOUNTAIN;
        }
    }
    @Test
    public void testHexRowWidth() {
        assertEquals(3, hexRowWidth(3, 5));
        assertEquals(5, hexRowWidth(3, 4));
        assertEquals(7, hexRowWidth(3, 3));
        assertEquals(7, hexRowWidth(3, 2));
        assertEquals(5, hexRowWidth(3, 1));
        assertEquals(3, hexRowWidth(3, 0));
        assertEquals(2, hexRowWidth(2, 0));
        assertEquals(4, hexRowWidth(2, 1));
        assertEquals(4, hexRowWidth(2, 2));
        assertEquals(2, hexRowWidth(2, 3));
    }

    @Test
    public void testHexRowOffset() {
        assertEquals(0, hexRowOffset(3, 5));
        assertEquals(-1, hexRowOffset(3, 4));
        assertEquals(-2, hexRowOffset(3, 3));
        assertEquals(-2, hexRowOffset(3, 2));
        assertEquals(-1, hexRowOffset(3, 1));
        assertEquals(0, hexRowOffset(3, 0));
        assertEquals(0, hexRowOffset(2, 0));
        assertEquals(-1, hexRowOffset(2, 1));
        assertEquals(-1, hexRowOffset(2, 2));
        assertEquals(0, hexRowOffset(2, 3));
    }



    /**
     * 尝试镶嵌六边形
     */

    public static void main(String[] args) {
        int size = 3;

        // initialize the tile rendering engine with a window of size WIDTH x HEIGHT
        TERenderer ter = new TERenderer(); // creating a TERenderer object
        ter.initialize(WIDTH, HEIGHT); // call initial method


        // initialize tiles
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING; // select other in set
            }
        }


        // initial hex
        int originX = (WIDTH - size)/2;
        int originY = 0;
        Position p = new Position(originX ,originY);
        addHexagon(world,p,size,randomTile());
        // for loops to generate hex
        // figure of num

        // 不是特别成功的完全六边形镶嵌，但是不是proj2的重点

        int numOfRow = HEIGHT/(2*size);
        int numofCol = WIDTH/(3*size-2);
        int x,y;
        for (y = size; y < HEIGHT; y+=size) {

            for (x = originX - (y/size)*(2*size-1); x <= originX + (y/size)*(2*size-1); x += (2 * size -1)){
                Position test = new Position(x,y);
                if(isDrawable(world,test,size)){
                addHexagon(world,test,size,randomTile());
                }
            }
        }
        // draws the world to the screen
        ter.renderFrame(world);
    }
}