import java.util.*;

public class KCenterSolver {

    private final int n;
    private final int k;
    private final int[][] dist;

    private int   bestRadius;
    private int[] bestCenters;
    private long  nodesExplored;

    private long    timeLimitMs;
    private long    startTime;
    private boolean timeLimitReached;

    // minDist[v] = distância de v ao centro mais próximo já escolhido
    private int[] minDist;

    public KCenterSolver(int n, int k, int[][] dist) {
        this.n    = n;
        this.k    = k;
        this.dist = dist;
    }

    public int solve(int timeLimitSeconds) {
        timeLimitMs      = timeLimitSeconds > 0 ? timeLimitSeconds * 1000L : Long.MAX_VALUE;
        startTime        = System.currentTimeMillis();
        nodesExplored    = 0;
        timeLimitReached = false;

        // Upper bound inicial: heurística de Gonzalez
        int[] greedyCenters = gonzalezHeuristic();
        bestRadius  = computeRadius(greedyCenters);
        bestCenters = greedyCenters.clone();

        // minDist começa com MAX (nenhum centro escolhido)
        minDist = new int[n];
        Arrays.fill(minDist, Integer.MAX_VALUE);

        int[] chosen = new int[k];
        branchAndBound(0, 0, chosen);

        return bestRadius;
    }

    public int[]   getBestCenters()      { return bestCenters != null ? bestCenters.clone() : new int[0]; }
    public long    getNodesExplored()    { return nodesExplored; }
    public boolean isTimeLimitReached()  { return timeLimitReached; }


    private void branchAndBound(int depth, int startIdx, int[] chosen) {

        // Checagem de tempo a cada 4096 nós
        if ((nodesExplored & 0xFFF) == 0) {
            if (System.currentTimeMillis() - startTime > timeLimitMs) {
                timeLimitReached = true;
                return;
            }
        }
        nodesExplored++;

        int remaining = k - depth;
        int available = n - startIdx;

        // Poda 1: vértices insuficientes para completar k centros
        if (available < remaining) return;

        // Enumera o próximo centro candidato
        for (int v = startIdx; v <= n - remaining; v++) {
            chosen[depth] = v;

            int[] snapshot = updateMinDist(v);
            int partialRadius = currentRadius();

            if (depth + 1 == k) {
                // Última posição: raio parcial é o raio final da solução
                if (partialRadius < bestRadius) {
                    bestRadius  = partialRadius;
                    bestCenters = chosen.clone();
                }
            } else {
                // Posições intermediárias: desce sem podar pelo raio parcial
                // (raio parcial > raio final é esperado e não indica inviabilidade)
                branchAndBound(depth + 1, v + 1, chosen);
                if (timeLimitReached) {
                    restoreMinDist(snapshot);
                    return;
                }
            }

            restoreMinDist(snapshot);
        }
    }


    /**
     * Adiciona centro c: atualiza minDist[v] = min(minDist[v], dist[c][v]).
     * Retorna snapshot para backtrack. Complexidade: O(n).
     */
    private int[] updateMinDist(int c) {
        int[] snapshot = minDist.clone();
        int[] row = dist[c];
        for (int v = 0; v < n; v++) {
            if (row[v] < minDist[v]) minDist[v] = row[v];
        }
        return snapshot;
    }

    private void restoreMinDist(int[] snapshot) {
        System.arraycopy(snapshot, 0, minDist, 0, n);
    }

    /** Raio atual = max(minDist[v]). Complexidade: O(n). */
    private int currentRadius() {
        int r = 0;
        for (int v = 0; v < n; v++) {
            if (minDist[v] > r) r = minDist[v];
        }
        return r;
    }



    /** Calcula raio de uma solução completa. Complexidade: O(n * |centers|). */
    public int computeRadius(int[] centers) {
        int radius = 0;
        for (int v = 0; v < n; v++) {
            int minD = Integer.MAX_VALUE;
            for (int c : centers) {
                if (dist[v][c] < minD) minD = dist[v][c];
            }
            if (minD > radius) radius = minD;
        }
        return radius;
    }

    /** Verifica validade: tamanho <= k, índices em [0,n-1], sem duplicatas. */
    public boolean isValidSolution(int[] centers) {
        if (centers == null || centers.length == 0 || centers.length > k) return false;
        Set<Integer> seen = new HashSet<>();
        for (int c : centers) {
            if (c < 0 || c >= n) return false;
            if (!seen.add(c))    return false;
        }
        return true;
    }

    /** Retorna clusters: clusters[i] = vértices (0-idx) com centro mais próximo = centers[i]. */
    public List<List<Integer>> getClusters(int[] centers) {
        List<List<Integer>> clusters = new ArrayList<>();
        for (int i = 0; i < centers.length; i++) clusters.add(new ArrayList<>());
        for (int v = 0; v < n; v++) {
            int bestC = 0, bestD = Integer.MAX_VALUE;
            for (int i = 0; i < centers.length; i++) {
                int d = dist[v][centers[i]];
                if (d < bestD) { bestD = d; bestC = i; }
            }
            clusters.get(bestC).add(v);
        }
        return clusters;
    }

    public int[] gonzalezHeuristic() {
        int[] centers         = new int[k];
        boolean[] isCenter    = new boolean[n];
        int[] minDistToCenter = new int[n];
        Arrays.fill(minDistToCenter, Integer.MAX_VALUE);

        centers[0]  = 0;
        isCenter[0] = true;
        for (int v = 0; v < n; v++) minDistToCenter[v] = dist[v][0];

        for (int i = 1; i < k; i++) {
            int farthest = -1, maxD = -1;
            for (int v = 0; v < n; v++) {
                if (!isCenter[v] && minDistToCenter[v] > maxD) {
                    maxD = minDistToCenter[v];
                    farthest = v;
                }
            }
            centers[i]         = farthest;
            isCenter[farthest] = true;
            for (int v = 0; v < n; v++) {
                int d = dist[v][farthest];
                if (d < minDistToCenter[v]) minDistToCenter[v] = d;
            }
        }
        return centers;
    }
}