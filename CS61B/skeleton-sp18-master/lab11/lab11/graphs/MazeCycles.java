package lab11.graphs;

/**
 *  @author Josh Hug
 */
public class MazeCycles extends MazeExplorer {
    /* Inherits public fields:
    public int[] distTo;
    public int[] edgeTo;
    public boolean[] marked;
    */
    public int[] pathTo;
    private  int s;
    private  Maze maze;
    private boolean found = false;
    public MazeCycles(Maze m) {
        super(m);
        maze = m;
        s = 0;
        distTo[s] = 0; // useless here
        edgeTo[s] = s;
        pathTo = new int[maze.V()];
        pathTo[s] = 0;
    }

    @Override
    public void solve() {
        // TODO: Your code here!
         dfs(s);
    }

    // Helper methods go here
    private void dfs(int v) {
        marked[v] = true;
        announce();

        for (int w : maze.adj(v)) {
            if (found) {
                return;
            }

            if (!marked[w]) {
                distTo[w] = distTo[v] + 1;
                pathTo[w] = v;
                dfs(w);
            }else if (w != pathTo[v]) {
                pathTo[w] = v;
                int current = v;
                edgeTo[current] = pathTo[current];
                announce();

                while (current!=w) {
                    current = pathTo[current];
                    edgeTo[current] = pathTo[current];
                    announce();
                }
                found = true;
                return;
            }

        }
    }

}

