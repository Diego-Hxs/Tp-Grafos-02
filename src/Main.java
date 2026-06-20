import java.io.*;
import java.util.*;

public class Main {

    // Pasta com os arquivos pmed01.txt ... pmed40.txt
    private static final String INSTANCES_FOLDER  = "instancias";

    // Limite de tempo por instância em segundos (0 = sem limite)
    private static final int    TIME_LIMIT_SECONDS = 60;

    // "select" para escolher na lista, "batch" para rodar todas
    private static final String MODE = "select";

    // Raios ótimos esperados por instância (índice 0 = pmed01), conforme Tabela 1 do enunciado
    private static final int[] OPTIMAL_RADIUS = {
        127, 98, 93, 74, 48,   // pmed01–05  (n=100)
         84, 64, 55, 37, 20,   // pmed06–10  (n=200)
         59, 51, 35, 26, 18,   // pmed11–15  (n=300)
         47, 39, 28, 18, 13,   // pmed16–20  (n=400)
         40, 38, 22, 15, 11,   // pmed21–25  (n=500)
         38, 32, 18, 13,  9,   // pmed26–30  (n=600)
         30, 29, 15, 11,  0,   // pmed31–34  (n=700) — pmed35 não existe nessa série
         30, 27, 15,            // pmed35–37  (n=800)
         29, 23, 13            // pmed38–40  (n=900)
    };

    // =========================================================================

    public static void main(String[] args) throws Exception {
        if (MODE.equals("batch")) {
            runBatch();
        } else {
            runSelect();
        }
    }

    private static void runSelect() throws Exception {
        File dir    = new File(INSTANCES_FOLDER);
        File[] files = dir.listFiles((d, name) -> name.matches("pmed\\d+\\.txt"));

        if (files == null || files.length == 0) {
            System.out.println("Nenhum arquivo pmed*.txt encontrado em: " + INSTANCES_FOLDER);
            System.out.println("Baixe as instâncias em: http://people.brunel.ac.uk/~mastjjb/jeb/orlib/files");
            return;
        }
        Arrays.sort(files, Comparator.comparingInt(f -> extractNumber(f.getName())));

        // Exibe a lista
        System.out.println("=".repeat(60));
        System.out.println("  Instâncias disponíveis");
        System.out.println("=".repeat(60));
        System.out.printf("  %-4s %-16s %5s %5s %14s%n", "Nº", "Arquivo", "n", "k", "Raio esperado");
        System.out.println("  " + "-".repeat(50));

        for (int i = 0; i < files.length; i++) {
            String name  = files[i].getName();
            int num      = extractNumber(name);     // 1–40
            int expected = num > 0 && num <= OPTIMAL_RADIUS.length ? OPTIMAL_RADIUS[num - 1] : -1;

            // Lê só o cabeçalho para mostrar n e k sem carregar a matriz inteira
            int[] header = readHeader(files[i].getPath());
            System.out.printf("  %-4d %-16s %5d %5d %14s%n",
                    i + 1, name, header[0], header[2],
                    expected > 0 ? String.valueOf(expected) : "?");
        }

        // Pede a escolha
        System.out.println();
        System.out.print("Digite o número da instância (1-" + files.length + "): ");
        Scanner sc = new Scanner(System.in);

        int choice;
        try {
            choice = sc.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Entrada inválida.");
            return;
        }

        if (choice < 1 || choice > files.length) {
            System.out.println("Opção fora do intervalo.");
            return;
        }

        File chosen = files[choice - 1];
        int num     = extractNumber(chosen.getName());
        int expected = num > 0 && num <= OPTIMAL_RADIUS.length ? OPTIMAL_RADIUS[num - 1] : -1;

        System.out.println();
        runSingle(chosen.getPath(), expected);
    }


    private static void runSingle(String path, int expectedRadius) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("Problema dos k-Centros — Solução Exata (Branch and Bound)");
        System.out.println("=".repeat(60));
        System.out.println("Arquivo      : " + path);
        if (TIME_LIMIT_SECONDS > 0)
            System.out.println("Tempo limite : " + TIME_LIMIT_SECONDS + " s");

        InstanceReader.Instance inst = InstanceReader.read(path);
        System.out.printf("Instância    : n=%d, k=%d%n", inst.n, inst.k);

        KCenterSolver solver = new KCenterSolver(inst.n, inst.k, inst.dist);

        // Upper bound inicial
        int[] greedyCenters = solver.gonzalezHeuristic();
        int greedyRadius    = solver.computeRadius(greedyCenters);
        System.out.printf("%nHeurística de Gonzalez (upper bound): raio = %d%n", greedyRadius);

        // Branch and Bound
        long t0       = System.currentTimeMillis();
        int optRadius = solver.solve(TIME_LIMIT_SECONDS);
        long elapsed  = System.currentTimeMillis() - t0;

        int[] bestCenters = solver.getBestCenters();

        System.out.println();
        System.out.println("-".repeat(60));
        System.out.printf("Raio encontrado  : %d%n", optRadius);
        if (expectedRadius > 0)
            System.out.printf("Raio esperado    : %d  %s%n",
                    expectedRadius, optRadius == expectedRadius ? "✓ CORRETO" : "✗ DIVERGE");
        System.out.printf("Nós explorados   : %,d%n", solver.getNodesExplored());
        System.out.printf("Tempo            : %.3f s%n", elapsed / 1000.0);
        if (solver.isTimeLimitReached())
            System.out.println("⚠ Limite de tempo atingido — solução pode não ser ótima.");
        System.out.println("-".repeat(60));

        // Centros (1-indexado)
        int[] display = bestCenters.clone();
        Arrays.sort(display);
        StringBuilder sb = new StringBuilder();
        for (int c : display) sb.append(c + 1).append(" ");
        System.out.println("Centros (1-idx)  : " + sb.toString().trim());
        System.out.println("Solução válida   : " + (solver.isValidSolution(bestCenters) ? "SIM" : "NÃO"));

        printClustering(inst, bestCenters, solver);
    }

    

    private static void runBatch() throws Exception {
        File dir    = new File(INSTANCES_FOLDER);
        File[] files = dir.listFiles((d, name) -> name.matches("pmed\\d+\\.txt"));
        if (files == null || files.length == 0) {
            System.out.println("Nenhum arquivo pmed*.txt encontrado em: " + INSTANCES_FOLDER);
            return;
        }
        Arrays.sort(files, Comparator.comparingInt(f -> extractNumber(f.getName())));

        String csv = "instancia,n,k,raio_esperado,raio_guloso,raio_exato,correto,nos_explorados,tempo_ms,timeout\n";

        System.out.printf("%-14s %5s %5s %13s %10s %10s %8s %15s %10s %8s%n",
                "Instancia", "n", "k", "R_esperado", "R_guloso", "R_exato",
                "OK?", "Nos", "Tempo(s)", "Timeout");
        System.out.println("-".repeat(100));

        for (File f : files) {
            InstanceReader.Instance inst;
            try {
                inst = InstanceReader.read(f.getPath());
            } catch (Exception e) {
                System.err.println("Erro ao ler " + f.getName() + ": " + e.getMessage());
                continue;
            }

            int num      = extractNumber(f.getName());
            int expected = num > 0 && num <= OPTIMAL_RADIUS.length ? OPTIMAL_RADIUS[num - 1] : -1;

            KCenterSolver solver = new KCenterSolver(inst.n, inst.k, inst.dist);
            int greedyRadius     = solver.computeRadius(solver.gonzalezHeuristic());

            long t0       = System.currentTimeMillis();
            int optRadius = solver.solve(TIME_LIMIT_SECONDS);
            long elapsed  = System.currentTimeMillis() - t0;
            boolean timeout = solver.isTimeLimitReached();
            boolean correct = expected > 0 && optRadius == expected;

            System.out.printf("%-14s %5d %5d %13d %10d %10d %8s %15s %10.3f %8s%n",
                    f.getName(), inst.n, inst.k, expected, greedyRadius, optRadius,
                    correct ? "✓" : (timeout ? "?" : "✗"),
                    String.format("%,d", solver.getNodesExplored()),
                    elapsed / 1000.0, timeout ? "SIM" : "nao");

            csv += String.format("%s,%d,%d,%d,%d,%d,%s,%d,%d,%s%n",
                    f.getName(), inst.n, inst.k, expected, greedyRadius, optRadius,
                    correct ? "sim" : "nao", solver.getNodesExplored(), elapsed,
                    timeout ? "sim" : "nao");
        }

        try (PrintWriter pw = new PrintWriter("resultados_exato.csv")) {
            pw.print(csv);
        }
        System.out.println("\nResultados salvos em: resultados_exato.csv");
    }


    private static void printClustering(InstanceReader.Instance inst, int[] centers, KCenterSolver solver) {
        System.out.println("\nClusterização:");
        System.out.printf("  %-8s %-10s %s%n", "Cluster", "Centro", "Vértices");
        System.out.println("  " + "-".repeat(50));

        List<List<Integer>> clusters = solver.getClusters(centers);
        for (int ci = 0; ci < centers.length; ci++) {
            List<Integer> members = clusters.get(ci);
            List<Integer> display = new ArrayList<>();
            for (int v : members) display.add(v + 1);
            String membersStr = display.size() <= 10
                    ? display.toString()
                    : display.subList(0, 10) + "... (" + display.size() + " total)";
            System.out.printf("  %-8d %-10d %s%n", ci + 1, centers[ci] + 1, membersStr);
        }
    }

    /** Lê apenas a primeira linha do arquivo para obter n, edges, k sem alocar a matriz. */
    private static int[] readHeader(String path) throws IOException {
        try (Scanner sc = new Scanner(new File(path))) {
            return new int[]{ sc.nextInt(), sc.nextInt(), sc.nextInt() };
        }
    }

    /** Extrai o número do nome do arquivo (ex: "pmed03.txt" → 3). */
    private static int extractNumber(String name) {
        try {
            return Integer.parseInt(name.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}