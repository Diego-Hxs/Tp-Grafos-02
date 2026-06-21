import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class Principal {

    private static final String PASTA = "instancias";

    private static final int[] RAIO_OTIMO = {
        127, 98, 93, 74, 48,
        84, 64, 55, 37, 20,
        59, 51, 35, 26, 18,
        47, 39, 28, 18, 13,
        40, 38, 22, 15, 11,
        38, 32, 18, 13, 9,
        30, 29, 15, 11,
        30, 27, 15,
        29, 23, 13
    };

    public static void main(String[] args) throws IOException {
        String modo = args.length > 0 ? args[0] : "aprox";
        switch (modo) {
            case "aprox" -> aproximado();
            case "exata" -> exata(Integer.parseInt(args[1]), args.length > 2 ? Integer.parseInt(args[2]) : 60);
            case "lista" -> lista();
            default -> System.out.println("uso: aprox | exata <i> [segundos] | lista");
        }
    }

    private static void aproximado() throws IOException {
        System.out.printf("%-10s %5s %5s %10s %10s %8s %12s%n",
                "instancia", "n", "k", "gonzalez", "otimo", "razao", "tempo(ms)");
        System.out.println("-".repeat(64));

        StringBuilder csv = new StringBuilder("instancia,n,k,raio_gonzalez,raio_otimo,razao,tempo_ms\n");
        double piorRazao = 0;
        boolean garantia = true;

        for (int i = 1; i <= 40; i++) {
            String nome = String.format("pmed%02d", i);
            Grafo g = LeitorPmed.ler(Path.of(PASTA, nome + ".txt").toString());
            Gonzalez.Resultado r = Gonzalez.resolver(g);

            int otimo = RAIO_OTIMO[i - 1];
            double razao = (double) r.raio / otimo;
            double ms = r.tempoNanos / 1_000_000.0;

            piorRazao = Math.max(piorRazao, razao);
            if (r.raio < otimo || razao > 2.0 + 1e-9
                    || !Raio.centrosValidos(g.n, r.centros, g.k)
                    || r.raio != Gonzalez.raioDe(g, r.centros)) {
                garantia = false;
            }

            System.out.printf(Locale.ROOT, "%-10s %5d %5d %10d %10d %8.3f %12.3f%n",
                    nome, g.n, g.k, r.raio, otimo, razao, ms);
            csv.append(String.format(Locale.ROOT, "%s,%d,%d,%d,%d,%.4f,%.3f%n",
                    nome, g.n, g.k, r.raio, otimo, razao, ms));
        }

        try (PrintWriter pw = new PrintWriter("resultados_aproximado.csv")) {
            pw.print(csv);
        }

        System.out.println("-".repeat(64));
        System.out.printf(Locale.ROOT, "pior razao gonzalez/otimo : %.3f%n", piorRazao);
        System.out.printf("garantia 2-aproximacao    : %s%n", garantia ? "OK" : "FALHOU");
        System.out.println("resultados salvos em      : resultados_aproximado.csv");
    }

    private static void exata(int indice, int segundos) throws IOException {
        String nome = String.format("pmed%02d", indice);
        Grafo g = LeitorPmed.ler(Path.of(PASTA, nome + ".txt").toString());
        int otimo = RAIO_OTIMO[indice - 1];

        Gonzalez.Resultado guloso = Gonzalez.resolver(g);
        int[][] dist = Distancias.matriz(g);

        Exata solver = new Exata(dist, g.k);
        Exata.Resultado r = solver.resolver(guloso.centros, guloso.raio, segundos);

        System.out.printf("instancia    : %s (n=%d, k=%d)%n", nome, g.n, g.k);
        System.out.printf("gonzalez     : raio %d%n", guloso.raio);
        System.out.printf("exata        : raio %d%n", r.raio);
        System.out.printf("otimo        : %d  %s%n", otimo,
                r.timeout ? "(limite de tempo atingido)" : r.raio == otimo ? "OK" : "DIVERGE");
        System.out.printf(Locale.ROOT, "nos          : %d%n", r.nos);
        System.out.printf(Locale.ROOT, "tempo        : %.3f s%n", r.tempoNanos / 1_000_000_000.0);
        System.out.printf("centros validos: %s%n", Raio.centrosValidos(g.n, r.centros, g.k));

        int[] ordenados = r.centros.clone();
        java.util.Arrays.sort(ordenados);
        StringBuilder sb = new StringBuilder();
        for (int c : ordenados) {
            sb.append(c + 1).append(' ');
        }
        System.out.println("centros      : " + sb.toString().trim());

        List<List<Integer>> grupos = Raio.clusters(dist, r.centros);
        for (int i = 0; i < r.centros.length; i++) {
            System.out.printf("  cluster %d (centro %d): %d vertices%n",
                    i + 1, r.centros[i] + 1, grupos.get(i).size());
        }
    }

    private static void lista() throws IOException {
        System.out.printf("%-10s %5s %5s %10s%n", "instancia", "n", "k", "otimo");
        for (int i = 1; i <= 40; i++) {
            String nome = String.format("pmed%02d", i);
            Grafo g = LeitorPmed.ler(Path.of(PASTA, nome + ".txt").toString());
            System.out.printf("%-10s %5d %5d %10d%n", nome, g.n, g.k, RAIO_OTIMO[i - 1]);
        }
    }

    private Principal() {
    }
}
