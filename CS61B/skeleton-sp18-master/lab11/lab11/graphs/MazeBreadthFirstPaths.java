package lab11.graphs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 *  @author Josh Hug
 */
public class MazeBreadthFirstPaths extends MazeExplorer {
    /* Inherits public fields:
    public int[] distTo;
    public int[] edgeTo;
    public boolean[] marked;
    */
    private int s;
    private int t;
    private Maze maze;

    public MazeBreadthFirstPaths(Maze m, int sourceX, int sourceY, int targetX, int targetY) {
        super(m);
        // Add more variables here!
        maze = m;
        s = maze.xyTo1D(sourceX, sourceY);
        t = maze.xyTo1D(targetX, targetY);
        distTo[s] = 0;
        edgeTo[s] = s;
    }

    /** Conducts a breadth first search of the maze starting at the source. */
    private void bfs(int v) {
        // TODO: Your code here. Don't forget to update distTo, edgeTo, and marked, as well as call announce()
        Queue<Integer> q = new LinkedList<>();
        q.add(v);
        // mark v
        marked[v] = true;
        announce();

        if (v == t) {
            return;
        }

        while (!q.isEmpty()) {
            int p = q.remove();
            for (int w : maze.adj(p)){
                if (!marked[w]) {
                    edgeTo[w] = p;
                    announce();
                    distTo[w] = distTo[p] + 1;
                    marked[w] = true;
                    announce();
                    q.add(w);
                    if (w == t) {
                        return;
                    }
                }
            }
        }
    }


    @Override
    public void solve() {
         bfs(s);
    }
}

