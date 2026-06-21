import java.util.ArrayList;
import java.util.List;

public final class Raio {

    private Raio() {
    }

    public static int de(int[][] dist, int[] centros) {
        int raio = 0;
        for (int v = 0; v < dist.length; v++) {
            int menor = Dijkstra.INFINITO;
            for (int c : centros) {
                if (dist[c][v] < menor) {
                    menor = dist[c][v];
                }
            }
            if (menor > raio) {
                raio = menor;
            }
        }
        return raio;
    }

    public static boolean centrosValidos(int n, int[] centros, int k) {
        if (centros == null || centros.length == 0 || centros.length > k) {
            return false;
        }
        boolean[] visto = new boolean[n];
        for (int c : centros) {
            if (c < 0 || c >= n || visto[c]) {
                return false;
            }
            visto[c] = true;
        }
        return true;
    }

    public static List<List<Integer>> clusters(int[][] dist, int[] centros) {
        List<List<Integer>> grupos = new ArrayList<>();
        for (int i = 0; i < centros.length; i++) {
            grupos.add(new ArrayList<>());
        }
        for (int v = 0; v < dist.length; v++) {
            int melhor = 0;
            int menor = Dijkstra.INFINITO;
            for (int i = 0; i < centros.length; i++) {
                int d = dist[centros[i]][v];
                if (d < menor) {
                    menor = d;
                    melhor = i;
                }
            }
            grupos.get(melhor).add(v);
        }
        return grupos;
    }
}
