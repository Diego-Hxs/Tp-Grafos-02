import java.io.*;
import java.util.*;

/**
 * Leitor de instâncias no formato da OR-Library (problema das p-medianas).
 *
 * Formato do arquivo (conforme http://people.brunel.ac.uk/~mastjjb/jeb/orlib/pmedinfo.html):
 *
 *   Linha 1: <número de vértices> <número de arestas> <p>
 *   Linhas seguintes: <i> <j> <d_ij>   (uma aresta por linha)
 *
 * Como o grafo é não-dirigido e completo, a matriz de distâncias é simétrica.
 * Vértices no arquivo são indexados a partir de 1; internamente usamos 0-indexado.
 *
 * Referência do formato:
 *   Beasley, J.E. (1990). OR-Library: distributing test problems by electronic mail.
 *   Journal of the Operational Research Society, 41(11), 1069–1072.
 *   https://doi.org/10.1057/jors.1990.166
 */
public class InstanceReader {

    public static class Instance {
        public final int n;       // número de vértices
        public final int k;       // número de centros (p)
        public final int[][] dist; // matriz de distâncias n×n

        public Instance(int n, int k, int[][] dist) {
            this.n = n;
            this.k = k;
            this.dist = dist;
        }
    }

    /**
     * Lê uma instância pmed a partir de um arquivo.
     *
     * @param path caminho do arquivo
     * @return instância carregada
     */
    public static Instance read(String path) throws IOException {
        try (Scanner sc = new Scanner(new File(path))) {
            int n = sc.nextInt();
            int edges = sc.nextInt();
            int k = sc.nextInt();

            int[][] dist = new int[n][n];

            // Inicializa com 0 na diagonal; distâncias não informadas ficam 0
            // (grafo pode não ser completo no arquivo, mas na prática todas as arestas são dadas)
            for (int i = 0; i < n; i++) {
                Arrays.fill(dist[i], Integer.MAX_VALUE / 2);
                dist[i][i] = 0;
            }

            for (int e = 0; e < edges; e++) {
                int i = sc.nextInt() - 1;  // converte para 0-indexado
                int j = sc.nextInt() - 1;
                int d = sc.nextInt();
                dist[i][j] = d;
                dist[j][i] = d;
            }

            // Floyd-Warshall para fechar o grafo (garante distâncias de menor caminho)
            // Necessário se o grafo não for originalmente completo
            floydWarshall(dist, n);

            return new Instance(n, k, dist);
        }
    }

    /**
     * Floyd-Warshall para calcular as distâncias de menor caminho entre todos os pares.
     *
     * Referência:
     *   Cormen et al. (2009). Introduction to Algorithms (3rd ed.), Capítulo 25.2. MIT Press.
     *
     * Complexidade: O(n³)
     */
    private static void floydWarshall(int[][] dist, int n) {
        for (int via = 0; via < n; via++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][via] + dist[via][j] < dist[i][j]) {
                        dist[i][j] = dist[i][via] + dist[via][j];
                    }
                }
            }
        }
    }
}
