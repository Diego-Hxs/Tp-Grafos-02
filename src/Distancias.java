public final class Distancias {

    private Distancias() {
    }

    public static int[][] matriz(Grafo g) {
        int n = g.n;
        int[][] dist = new int[n][n];
        Dijkstra.FilaMin fila = new Dijkstra.FilaMin(2 * g.arestas + n);
        for (int origem = 0; origem < n; origem++) {
            Dijkstra.apartirDe(g, origem, dist[origem], fila);
        }
        return dist;
    }
}
