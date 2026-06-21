import java.util.Arrays;

final class Dijkstra {

    static final int INFINITO = Integer.MAX_VALUE >> 1;

    private Dijkstra() {
    }

    static void apartirDe(Grafo g, int origem, int[] distancia, FilaMin fila) {
        Arrays.fill(distancia, INFINITO);
        distancia[origem] = 0;
        fila.limpar();
        fila.inserir(origem);

        int[] inicio = g.inicio;
        int[] destino = g.destino;
        int[] peso = g.peso;

        while (!fila.vazia()) {
            long topo = fila.remover();
            int d = (int) (topo >>> 24);
            int u = (int) (topo & 0xFFFFFF);
            if (d > distancia[u]) {
                continue;
            }
            int fim = inicio[u + 1];
            for (int p = inicio[u]; p < fim; p++) {
                int v = destino[p];
                int nova = d + peso[p];
                if (nova < distancia[v]) {
                    distancia[v] = nova;
                    fila.inserir(((long) nova << 24) | v);
                }
            }
        }
    }

    static final class FilaMin {
        private long[] dados;
        private int tamanho;

        FilaMin(int capacidade) {
            dados = new long[Math.max(16, capacidade)];
        }

        void limpar() {
            tamanho = 0;
        }

        boolean vazia() {
            return tamanho == 0;
        }

        void inserir(long valor) {
            if (tamanho == dados.length) {
                dados = Arrays.copyOf(dados, dados.length << 1);
            }
            int i = tamanho++;
            dados[i] = valor;
            while (i > 0) {
                int pai = (i - 1) >> 1;
                if (dados[pai] <= dados[i]) {
                    break;
                }
                long troca = dados[pai];
                dados[pai] = dados[i];
                dados[i] = troca;
                i = pai;
            }
        }

        long remover() {
            long raiz = dados[0];
            long ultimo = dados[--tamanho];
            if (tamanho > 0) {
                dados[0] = ultimo;
                int i = 0;
                while (true) {
                    int esq = 2 * i + 1;
                    int dir = 2 * i + 2;
                    int menor = i;
                    if (esq < tamanho && dados[esq] < dados[menor]) {
                        menor = esq;
                    }
                    if (dir < tamanho && dados[dir] < dados[menor]) {
                        menor = dir;
                    }
                    if (menor == i) {
                        break;
                    }
                    long troca = dados[menor];
                    dados[menor] = dados[i];
                    dados[i] = troca;
                    i = menor;
                }
            }
            return raiz;
        }
    }
}
