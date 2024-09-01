package lab11.graphs;


import edu.princeton.cs.algs4.IndexMinPQ;

import java.util.LinkedList;
import java.util.Queue;

/**
 *  @author Josh Hug
 */
public class MazeAStarPath extends MazeExplorer {
    private int s;
    private int t;
    private boolean targetFound = false;
    private Maze maze;

    //
    private IndexMinPQ<Integer> pq;

    public MazeAStarPath(Maze m, int sourceX, int sourceY, int targetX, int targetY) {
        super(m);
        maze = m;
        s = maze.xyTo1D(sourceX, sourceY);
        t = maze.xyTo1D(targetX, targetY);
        distTo[s] = 0;
        edgeTo[s] = s;
        pq = new IndexMinPQ<>(maze.V());
    }

    /** Estimate of the distance from v to the target. */
    private int h(int v) {
        return Math.abs(maze.toY(v) - maze.toY(t)) + Math.abs(maze.toX(v) - maze.toX(t));
    }

    /** Finds vertex estimated to be closest to target. */
    private int findMinimumUnmarked(int v) {
        // do not have to use
        return -1;
    }

    /** Performs an A star search from vertex s. */
    private void astar(int s) {
        marked[s] = true;

        if (s==t) targetFound = true;
        if (targetFound) return;

        pq.insert(s, distTo[s] + h(s));

        while (!pq.isEmpty()) {
            int v = pq.delMin();
            if (v == t) {
                targetFound = true;
                return;
            }
            for (int w : maze.adj(v)) {
                if (distTo[w] > distTo[v] + 1) {
                    distTo[w] = distTo[v] + 1;
                    edgeTo[w] = v;
                    marked[w] = true;
                    announce();
                    if (pq.contains(w)) {
                        pq.decreaseKey(w, distTo[w] + h(w));
                    } else {
                        pq.insert(w, distTo[w] + h(w));
                    }
                }
            }
        }
    }


    @Override
    public void solve() {
        astar(s);
    }
}

