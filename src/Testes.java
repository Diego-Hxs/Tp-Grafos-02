public final class Testes {

    private static int total;
    private static int falhas;

    public static void main(String[] args) {
        gonzalezDeterministico();
        gonzalezPropriedades();
        exataOtima();
        System.out.println("-".repeat(56));
        System.out.printf("%d testes, %d falhas%n", total, falhas);
        if (falhas > 0) {
            System.exit(1);
        }
    }

    private static void gonzalezDeterministico() {
        verificarGonzalez("vertice unico, k=1", construir(1, 1, new int[][] {}), 0, 0);
        verificarGonzalez("caminho P5, k=2, inicio 0", caminho(5, 1, 2), 2, 0);
        verificarGonzalez("caminho P5, k=3, inicio 0", caminho(5, 1, 3), 1, 0);

        Grafo triangulo = construir(3, 1, new int[][] {{0, 1, 1}, {1, 2, 1}, {0, 2, 1}});
        verificarGonzalez("triangulo unitario, k=1", triangulo, 1, 0);

        Grafo estrela = construir(4, 1, new int[][] {{0, 1, 1}, {0, 2, 1}, {0, 3, 1}});
        verificarGonzalez("estrela, k=1, centro como inicio", estrela, 1, 0);
        verificarGonzalez("estrela, k=1, folha como inicio", estrela, 2, 1);

        Grafo pesado = construir(3, 2, new int[][] {{0, 1, 5}, {1, 2, 5}});
        verificarGonzalez("caminho pesado, k=2, inicio 0", pesado, 5, 0);

        Grafo grupos = construir(4, 2, new int[][] {{0, 1, 1}, {2, 3, 1}, {1, 2, 100}});
        verificarGonzalez("dois grupos, k=2, inicio 0", grupos, 1, 0);
    }

    private static void gonzalezPropriedades() {
        Grafo g = construir(6, 3,
                new int[][] {{0, 1, 4}, {1, 2, 1}, {2, 3, 7}, {3, 4, 2}, {4, 5, 3}, {0, 5, 9}});
        Gonzalez.Resultado r = Gonzalez.resolver(g);
        afirmar("centros validos", Raio.centrosValidos(g.n, r.centros, g.k));
        afirmar("raio coerente com recomputo", r.raio == Gonzalez.raioDe(g, r.centros));
        afirmar("centros cobrem k", r.centros.length == Math.min(g.k, g.n));

        Gonzalez.Resultado a = Gonzalez.resolver(g, 0);
        Gonzalez.Resultado b = Gonzalez.resolver(g, 0);
        afirmar("execucao deterministica", a.raio == b.raio);
    }

    private static void exataOtima() {
        verificarExata("exata caminho P5, k=2", caminho(5, 1, 2), 1);
        verificarExata("exata triangulo, k=1", construir(3, 1, new int[][] {{0, 1, 1}, {1, 2, 1}, {0, 2, 1}}), 1);
        verificarExata("exata estrela, k=1", construir(4, 1, new int[][] {{0, 1, 1}, {0, 2, 1}, {0, 3, 1}}), 1);
        verificarExata("exata dois grupos, k=2", construir(4, 2, new int[][] {{0, 1, 1}, {2, 3, 1}, {1, 2, 100}}), 1);
        verificarExata("exata caminho pesado, k=2", construir(3, 2, new int[][] {{0, 1, 5}, {1, 2, 5}}), 5);
    }

    private static Grafo caminho(int n, int peso, int k) {
        int[][] arestas = new int[n - 1][3];
        for (int i = 0; i < n - 1; i++) {
            arestas[i] = new int[] {i, i + 1, peso};
        }
        return construir(n, k, arestas);
    }

    private static void verificarGonzalez(String nome, Grafo g, int esperado, int inicio) {
        Gonzalez.Resultado r = Gonzalez.resolver(g, inicio);
        boolean ok = r.raio == esperado
                && Raio.centrosValidos(g.n, r.centros, g.k)
                && r.raio == Gonzalez.raioDe(g, r.centros);
        registrar(nome, ok, esperado, r.raio);
    }

    private static void verificarExata(String nome, Grafo g, int esperado) {
        Gonzalez.Resultado guloso = Gonzalez.resolver(g);
        int[][] dist = Distancias.matriz(g);
        Exata.Resultado r = new Exata(dist, g.k).resolver(guloso.centros, guloso.raio, 0);
        boolean ok = r.raio == esperado
                && !r.timeout
                && Raio.centrosValidos(g.n, r.centros, g.k)
                && r.raio == Raio.de(dist, r.centros);
        registrar(nome, ok, esperado, r.raio);
    }

    private static void registrar(String nome, boolean ok, int esperado, int obtido) {
        total++;
        if (!ok) {
            falhas++;
        }
        System.out.printf("[%s] %-36s esperado=%d obtido=%d%n",
                ok ? "ok" : "X", nome, esperado, obtido);
    }

    private static void afirmar(String nome, boolean ok) {
        total++;
        if (!ok) {
            falhas++;
        }
        System.out.printf("[%s] %s%n", ok ? "ok" : "X", nome);
    }

    private static Grafo construir(int n, int k, int[][] arestas) {
        int m = arestas.length;
        int[] origem = new int[m];
        int[] alvo = new int[m];
        int[] custo = new int[m];
        for (int e = 0; e < m; e++) {
            origem[e] = arestas[e][0];
            alvo[e] = arestas[e][1];
            custo[e] = arestas[e][2];
        }
        return Grafo.deArestas(n, k, origem, alvo, custo);
    }

    private Testes() {
    }
}
