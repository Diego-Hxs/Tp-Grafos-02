import java.util.Arrays;

public final class Gonzalez {

    public static final class Resultado {
        public final int[] centros;
        public final int raio;
        public final long tempoNanos;

        Resultado(int[] centros, int raio, long tempoNanos) {
            this.centros = centros;
            this.raio = raio;
            this.tempoNanos = tempoNanos;
        }
    }

    private Gonzalez() {
    }

    public static Resultado resolver(Grafo g) {
        return resolver(g, 0);
    }

    public static Resultado resolver(Grafo g, int primeiroCentro) {
        long inicio = System.nanoTime();
        int n = g.n;
        int k = Math.min(g.k, n);

        int[] menorDist = new int[n];
        Arrays.fill(menorDist, Dijkstra.INFINITO);
        int[] distancia = new int[n];
        boolean[] eCentro = new boolean[n];
        int[] centros = new int[k];
        Dijkstra.FilaMin fila = new Dijkstra.FilaMin(2 * g.arestas + n);

        int atual = primeiroCentro;
        for (int i = 0; i < k; i++) {
            centros[i] = atual;
            eCentro[atual] = true;
            Dijkstra.apartirDe(g, atual, distancia, fila);

            for (int v = 0; v < n; v++) {
                if (distancia[v] < menorDist[v]) {
                    menorDist[v] = distancia[v];
                }
            }

            if (i + 1 < k) {
                int proximo = -1;
                int maior = -1;
                for (int v = 0; v < n; v++) {
                    if (!eCentro[v] && menorDist[v] > maior) {
                        maior = menorDist[v];
                        proximo = v;
                    }
                }
                if (proximo < 0) {
                    centros = Arrays.copyOf(centros, i + 1);
                    break;
                }
                atual = proximo;
            }
        }

        int raio = 0;
        for (int v = 0; v < n; v++) {
            if (menorDist[v] > raio) {
                raio = menorDist[v];
            }
        }

        return new Resultado(centros, raio, System.nanoTime() - inicio);
    }

    public static int raioDe(Grafo g, int[] centros) {
        int n = g.n;
        int[] menorDist = new int[n];
        Arrays.fill(menorDist, Dijkstra.INFINITO);
        int[] distancia = new int[n];
        Dijkstra.FilaMin fila = new Dijkstra.FilaMin(2 * g.arestas + n);
        for (int c : centros) {
            Dijkstra.apartirDe(g, c, distancia, fila);
            for (int v = 0; v < n; v++) {
                if (distancia[v] < menorDist[v]) {
                    menorDist[v] = distancia[v];
                }
            }
        }
        int raio = 0;
        for (int v = 0; v < n; v++) {
            if (menorDist[v] > raio) {
                raio = menorDist[v];
            }
        }
        return raio;
    }
}
