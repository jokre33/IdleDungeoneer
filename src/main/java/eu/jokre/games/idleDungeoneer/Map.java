package eu.jokre.games.idleDungeoneer;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.joml.Vector2f;

/**
 * Created by jokre on 22-May-17.
 */
public class Map {
    private MapTile[][] tileArray = new MapTile[100][100];
    private WeightedGraph<MapTile, DefaultWeightedEdge> weightedGraph = new DefaultDirectedWeightedGraph<MapTile, DefaultWeightedEdge>(DefaultWeightedEdge.class);
    private int mapID;

    public Map(int mapID) {
        double edgeWeightStraight = 1;
        double edgeWeightDiagonal = Math.sqrt(Math.pow(edgeWeightStraight, 2) * 2);
        DefaultWeightedEdge e;
        this.mapID = mapID;
        for (int i = 0; i < tileArray.length; i++) {
            for (int h = 0; h < tileArray[i].length; h++) {
                tileArray[i][h] = new MapTile(new Vector2f(i, h));
                weightedGraph.addVertex(tileArray[i][h]);
                if (i > 0) {
                    e = weightedGraph.addEdge(tileArray[i][h], tileArray[i - 1][h]);
                    weightedGraph.setEdgeWeight(e, edgeWeightStraight);
                }
                if (h > 0) {
                    e = weightedGraph.addEdge(tileArray[i][h], tileArray[i][h - 1]);
                    weightedGraph.setEdgeWeight(e, edgeWeightStraight);
                }
                if (i > 0 && h > 0) {
                    e = weightedGraph.addEdge(tileArray[i][h], tileArray[i - 1][h - 1]);
                    weightedGraph.setEdgeWeight(e, edgeWeightStraight);
                }
            }
        }
    }

    public MapTile[][] getTileArray() {
        return tileArray;
    }
}
