import java.util.HashMap;
import java.util.Map;

public final class Grafo {

    public final int n;
    public final int arestas;
    public final int k;
    public final int[] inicio;
    public final int[] destino;
    public final int[] peso;

    private Grafo(int n, int arestas, int k, int[] inicio, int[] destino, int[] peso) {
        this.n = n;
        this.arestas = arestas;
        this.k = k;
        this.inicio = inicio;
        this.destino = destino;
        this.peso = peso;
    }

    public static Grafo deArestas(int n, int k, int[] origem, int[] alvo, int[] custo) {
        Map<Long, Integer> ultimo = new HashMap<>(2 * origem.length);
        for (int e = 0; e < origem.length; e++) {
            int a = origem[e];
            int b = alvo[e];
            if (a == b) {
                continue;
            }
            if (a > b) {
                int t = a;
                a = b;
                b = t;
            }
            ultimo.put((long) a * n + b, custo[e]);
        }

        int m = ultimo.size();
        int[] da = new int[m];
        int[] db = new int[m];
        int[] dc = new int[m];
        int[] grau = new int[n];
        int e = 0;
        for (Map.Entry<Long, Integer> entrada : ultimo.entrySet()) {
            long chave = entrada.getKey();
            int a = (int) (chave / n);
            int b = (int) (chave % n);
            da[e] = a;
            db[e] = b;
            dc[e] = entrada.getValue();
            grau[a]++;
            grau[b]++;
            e++;
        }

        int[] inicio = new int[n + 1];
        for (int v = 0; v < n; v++) {
            inicio[v + 1] = inicio[v] + grau[v];
        }

        int[] destino = new int[2 * m];
        int[] peso = new int[2 * m];
        int[] cursor = new int[n];
        System.arraycopy(inicio, 0, cursor, 0, n);

        for (int i = 0; i < m; i++) {
            int a = da[i];
            int b = db[i];
            int w = dc[i];
            destino[cursor[a]] = b;
            peso[cursor[a]] = w;
            cursor[a]++;
            destino[cursor[b]] = a;
            peso[cursor[b]] = w;
            cursor[b]++;
        }

        return new Grafo(n, m, k, inicio, destino, peso);
    }
}
