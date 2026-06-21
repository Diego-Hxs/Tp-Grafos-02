import java.util.Arrays;

public final class Exata {

    public static final class Resultado {
        public final int[] centros;
        public final int raio;
        public final long nos;
        public final boolean timeout;
        public final long tempoNanos;

        Resultado(int[] centros, int raio, long nos, boolean timeout, long tempoNanos) {
            this.centros = centros;
            this.raio = raio;
            this.nos = nos;
            this.timeout = timeout;
            this.tempoNanos = tempoNanos;
        }
    }

    private final int n;
    private final int k;
    private final int[][] dist;
    private final int[] menorDist;

    private int melhorRaio;
    private int[] melhoresCentros;
    private long nos;
    private long limiteMs;
    private long inicioMs;
    private boolean estourou;

    public Exata(int[][] dist, int k) {
        this.n = dist.length;
        this.k = k;
        this.dist = dist;
        this.menorDist = new int[n];
    }

    public Resultado resolver(int[] centrosIniciais, int raioInicial, int segundos) {
        long t0 = System.nanoTime();
        limiteMs = segundos > 0 ? segundos * 1000L : Long.MAX_VALUE;
        inicioMs = System.currentTimeMillis();
        nos = 0;
        estourou = false;

        melhorRaio = raioInicial;
        melhoresCentros = centrosIniciais.clone();
        Arrays.fill(menorDist, Dijkstra.INFINITO);

        ramificar(0, 0, new int[k]);

        return new Resultado(melhoresCentros, melhorRaio, nos, estourou, System.nanoTime() - t0);
    }

    private void ramificar(int profundidade, int inicio, int[] escolhidos) {
        if ((nos & 0xFFF) == 0 && System.currentTimeMillis() - inicioMs > limiteMs) {
            estourou = true;
            return;
        }
        nos++;

        int restantes = k - profundidade;
        if (n - inicio < restantes) {
            return;
        }

        for (int v = inicio; v <= n - restantes; v++) {
            escolhidos[profundidade] = v;
            int[] copia = aplicar(v);

            if (profundidade + 1 == k) {
                int raioParcial = raioAtual();
                if (raioParcial < melhorRaio) {
                    melhorRaio = raioParcial;
                    melhoresCentros = escolhidos.clone();
                }
            } else {
                ramificar(profundidade + 1, v + 1, escolhidos);
                if (estourou) {
                    restaurar(copia);
                    return;
                }
            }

            restaurar(copia);
        }
    }

    private int[] aplicar(int centro) {
        int[] copia = menorDist.clone();
        int[] linha = dist[centro];
        for (int v = 0; v < n; v++) {
            if (linha[v] < menorDist[v]) {
                menorDist[v] = linha[v];
            }
        }
        return copia;
    }

    private void restaurar(int[] copia) {
        System.arraycopy(copia, 0, menorDist, 0, n);
    }

    private int raioAtual() {
        int r = 0;
        for (int v = 0; v < n; v++) {
            if (menorDist[v] > r) {
                r = menorDist[v];
            }
        }
        return r;
    }
}
