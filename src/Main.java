import java.util.Scanner;

public final class Main {

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            Principal.main(args);
            return;
        }

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println();
            System.out.println("=== k-Centros OR-Library ===");
            System.out.println("1. Heuristica de Gonzalez (40 instancias)");
            System.out.println("2. Solucao exata (Branch and Bound)");
            System.out.println("3. Testes de tabela");
            System.out.println("4. Listar instancias");
            System.out.println("0. Sair");
            System.out.print("> ");

            String linha = sc.nextLine().trim();
            switch (linha) {
                case "1" -> Principal.main(new String[]{"aprox"});
                case "2" -> {
                    System.out.print("Numero da instancia (1-40): ");
                    String inst = sc.nextLine().trim();
                    System.out.print("Timeout em segundos [60]: ");
                    String tempo = sc.nextLine().trim();
                    String s = tempo.isEmpty() ? "60" : tempo;
                    Principal.main(new String[]{"exata", inst, s});
                }
                case "3" -> Testes.main(new String[]{});
                case "4" -> Principal.main(new String[]{"lista"});
                case "0" -> { return; }
                default -> System.out.println("Opcao invalida.");
            }
        }
    }
}
