package cp2022.solution;

import java.util.Queue;
import java.util.LinkedList;
import java.util.Optional;

// Graph represents nodes from 0 ... N-1
// One node must have at most one outgoing edge.
public class SimpleGraph {
    private Integer [] neighbors;

    public SimpleGraph(int vertices) {
        this.neighbors = new Integer [vertices];

        for (int i = 0 ; i < vertices ; i++) {
            this.neighbors[i] = null;
        }
    }

    // Checks if there exists path v -> w.
    // If there is, it returns number of nodes included in this path.
    // If there isn't, it returns optional none.
    private Optional<Integer> isConnected(int v, int w) {
        Queue<Integer> bfsQueue = new LinkedList<>();

        boolean visited[] = new boolean [this.neighbors.length];

        bfsQueue.add(v);
        visited[v] = true;

        int verticesCycleCount = 0;        
        while(!bfsQueue.isEmpty()) {
            Integer nextV = bfsQueue.remove();
            verticesCycleCount++;

            if (nextV == w) {
                return Optional.of(verticesCycleCount);
            }

            Integer neighbor = this.neighbors[nextV];
            if (neighbor != null) {
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    bfsQueue.add(neighbor);
                }
            }
        }

        return Optional.empty();
    }

    // Adds edge v -> w, and checks if it creates a cycle.
    // If there is a cycle, it returns this cycle's length (in vertices count).
    public Optional<Integer> processEdge(int v, int w) {
        Optional<Integer> cycleLength = isConnected(w, v);

        if (cycleLength.isEmpty()) {
            this.neighbors[v] = w;
        }

        return cycleLength;
    }

    // Removes edge v -> w, if it exists.
    public void removeEdge(int v, int w) {
        if (this.neighbors[v] != null && this.neighbors[v] == w ) {
            this.neighbors[v] = null;
        }
    }
}
