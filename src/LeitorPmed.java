import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class LeitorPmed {

    private LeitorPmed() {
    }

    public static Grafo ler(String caminho) throws IOException {
        try (InputStream entrada = new BufferedInputStream(new FileInputStream(caminho), 1 << 16)) {
            LeitorInteiros in = new LeitorInteiros(entrada);

            int n = in.proximo();
            int m = in.proximo();
            int k = in.proximo();

            int[] origem = new int[m];
            int[] alvo = new int[m];
            int[] custo = new int[m];

            for (int e = 0; e < m; e++) {
                origem[e] = in.proximo() - 1;
                alvo[e] = in.proximo() - 1;
                custo[e] = in.proximo();
            }

            return Grafo.deArestas(n, k, origem, alvo, custo);
        }
    }

    private static final class LeitorInteiros {
        private final InputStream entrada;
        private final byte[] buffer = new byte[1 << 16];
        private int tamanho;
        private int posicao;

        LeitorInteiros(InputStream entrada) {
            this.entrada = entrada;
        }

        int proximo() throws IOException {
            int c = ler();
            while (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                c = ler();
            }
            boolean negativo = c == '-';
            if (negativo) {
                c = ler();
            }
            int valor = 0;
            while (c >= '0' && c <= '9') {
                valor = valor * 10 + (c - '0');
                c = ler();
            }
            return negativo ? -valor : valor;
        }

        private int ler() throws IOException {
            if (posicao == tamanho) {
                tamanho = entrada.read(buffer);
                posicao = 0;
                if (tamanho <= 0) {
                    return -1;
                }
            }
            return buffer[posicao++];
        }
    }
}
