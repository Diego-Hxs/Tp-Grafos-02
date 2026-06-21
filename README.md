# Tp-Grafos-02 — Problema dos k-Centros

Trabalho prático de Teoria dos Grafos. O objetivo é resolver o problema dos k-centros sobre as 40 instâncias de p-medianas da OR-Library, comparando uma solução exata (Branch and Bound) com uma heurística aproximada (Gonzalez, 1985).

## Problema

Dado um grafo conexo com pesos não negativos nas arestas e um inteiro `k`, o problema dos k-centros pede para escolher um conjunto `C` com no máximo `k` vértices (os centros) de forma a minimizar o raio

```
R(C) = max_{v in V} min_{c in C} d(v, c)
```

onde `d(v, c)` é a distância de menor caminho. O raio representa a pior distância que qualquer vértice tem até o centro mais próximo. O problema é NP-difícil: obter razão melhor que 2 em tempo polinomial implicaria P = NP, então a 2-aproximação do Gonzalez é ótima em termos de garantia.

## Algoritmo Aproximado: Gonzalez (1985)

A heurística farthest-first constrói os centros de forma gulosa:

1. Escolhe um vértice inicial `c1` (aqui, sempre o vértice 0, tornando a execução determinística).
2. Mantém `menorDist[v]` = menor distância de `v` a qualquer centro já escolhido.
3. Repete `k - 1` vezes: adiciona como próximo centro o vértice com maior `menorDist` (o ponto pior atendido) e atualiza `menorDist` rodando Dijkstra a partir do novo centro.
4. O raio da solução é `max_v menorDist[v]`.

### Garantia de 2-aproximação

Seja `r*` o raio ótimo. Os `k` centros escolhidos pelo Gonzalez, mais o vértice que define o raio final, formam `k + 1` pontos mutuamente a distância maior ou igual a `R(C)` entre si (todo centro foi escolhido exatamente porque era o mais distante dos anteriores). Pelo princípio da casa dos pombos, dois desses `k + 1` pontos precisam estar no mesmo cluster da solução ótima. Pela desigualdade triangular, esses dois pontos estão a distância no máximo `2 r*`. Logo `R(C) <= 2 r*`.

Isso é uma garantia de pior caso: na prática, os resultados ficaram bem abaixo de 2 (ver seção de resultados).

## Algoritmo Exato: Branch and Bound

A solução exata enumera todos os subconjuntos `C(n, k)` de centros possíveis. Para cada subconjunto completo (folha da árvore de enumeração), calcula o raio usando a matriz de distâncias all-pairs e atualiza o melhor resultado.

Detalhes importantes da implementação:

- **Sem poda por raio parcial**: não é possível podar no meio da árvore pelo raio parcial, porque adicionar mais centros só pode diminuir o raio (é monotonamente não-crescente). Um raio alto no meio do caminho não significa que o resultado final será alto.
- **Poda combinatorial**: se os vértices restantes no intervalo `[inicio, n)` são menos que os centros que ainda faltam escolher, aquele ramo não tem como completar `k` centros e é podado.
- **Limite superior inicial**: usa o resultado do Gonzalez como cota inicial, o que poda muitos ramos logo no começo.
- **Timeout**: suporta limite de tempo em segundos para instâncias grandes onde a enumeração completa é inviável.
- **Matriz APSP**: usa n execuções de Dijkstra para calcular as distâncias entre todos os pares antes de começar o B&B.

## Construção do Grafo e Arestas Paralelas

As instâncias da OR-Library (`pmed*.txt`) estão no formato:

```
n m k
u1 v1 custo1
u2 v2 custo2
...
```

A primeira linha traz o número de vértices `n`, o número de arestas `m` e o parâmetro `k`. As arestas seguintes são 1-indexadas e não direcionadas.

Um detalhe crítico: os arquivos contêm **arestas paralelas** (dois registros para o mesmo par de vértices com custos diferentes). Conforme a especificação oficial da OR-Library, o custo válido é o **último lido** para cada par. Ignorar isso e usar o custo mínimo entre as paralelas produziria caminhos mais curtos, raios menores, e resultados que não batem com os ótimos de referência da Tabela 1.

A implementação resolve isso em `Grafo.deArestas`: usa um `HashMap<Long, Integer>` com chave `a*n + b` (onde `a < b`), sobrescrevendo o valor toda vez que o mesmo par aparece. Após ler todas as arestas, o mapa contém exatamente um custo por par (o último), e aí o CSR é construído.

## Representação do Grafo e Otimizações

O grafo usa **CSR (Compressed Sparse Row)**: três vetores contíguos de inteiros (`inicio`, `destino`, `peso`). Para iterar os vizinhos de um vértice `v`, basta percorrer `destino[inicio[v]]` até `destino[inicio[v+1] - 1]`. A localidade de cache é muito melhor do que listas de adjacência com objetos Java.

O Dijkstra usa uma **fila de prioridade própria** (heap binário de `long`), onde cada elemento empacota a distância nos bits altos e o vértice nos 24 bits baixos. Isso evita boxing e o custo de `Integer` wrapper na fila, que seria o gargalo em grafos grandes.

O leitor de inteiros (`LeitorPmed`) usa `BufferedInputStream` com buffer de bytes em vez de `Scanner`, que é bem mais lento por ser baseado em regex.

### Complexidade do Gonzalez

- Tempo: `O(k * m * log n)` — k execuções de Dijkstra, cada uma com custo `O(m log n)`.
- Espaço: `O(n + m)`.

Para `n = 900` e `m ~ 16000` (pmed40), isso substitui as ~7,3 * 10^8 operações que Floyd-Warshall precisaria.

## Estrutura do Código

```
src/
  Grafo.java        grafo em CSR; fabrica deArestas (regra do ultimo custo)
  LeitorPmed.java   leitura do formato OR-Library
  Dijkstra.java     menor caminho de fonte unica (compartilhado)
  Distancias.java   matriz APSP via n execucoes de Dijkstra
  Gonzalez.java     heuristica farthest-first, 2-aproximacao
  Exata.java        Branch and Bound com timeout e cota do Gonzalez
  Raio.java         raio, validacao de centros, atribuicao de clusters
  Principal.java    execucao das instancias (modos aprox, exata, lista)
  Testes.java       testes de tabela (17 casos)
instancias/         pmed01.txt a pmed40.txt
```

## Compilacao e Execucao

```bash
javac -d out src/*.java
```

```bash
# roda os 17 testes de tabela
java -cp out Testes

# heuristica nas 40 instancias; grava resultados_aproximado.csv
java -cp out Principal aprox

# solucao exata na instancia i, com limite de s segundos (padrao 60s)
java -cp out Principal exata <i> [s]

# lista n, k e raio otimo de cada instancia
java -cp out Principal lista
```

## Testes

O arquivo `Testes.java` tem 17 casos divididos em tres grupos:

**Gonzalez determinístico** (8 casos): verifica raio exato para grafos pequenos com centro inicial fixo. Cobre vértice único, caminho P5 com k=2 e k=3, triângulo, estrela com dois pontos de partida diferentes, caminho pesado e grafo de dois grupos separados por aresta cara.

**Propriedades do Gonzalez** (4 casos): valida que os centros são válidos, que o raio coincide com o recálculo independente via `raioDe`, que o número de centros está correto, e que a execução é determinística.

**Exata ótima** (5 casos): para os mesmos grafos pequenos, verifica que o Branch and Bound encontra o raio verdadeiramente ótimo dentro do tempo limite.

Todos os 17 testes passam. Para rodar:

```bash
java -cp out Testes
```

## Resultados

Todas as 40 instâncias da OR-Library foram resolvidas pelo Gonzalez em milissegundos. A razão `raio_gonzalez / raio_otimo` ficou entre 1,237 (pmed11) e 1,787 (pmed16), com média em torno de 1,50. Todas dentro do limite teórico de 2, e em todos os casos o raio do Gonzalez foi maior ou igual ao ótimo de referência.

Para a solução exata, pmed01 (n=100, k=5) retorna raio 127, igual ao ótimo de referência. Instâncias maiores requerem timeout porque o espaço de busca cresce combinatorialmente.

O CSV com os resultados detalhados do Gonzalez é gerado em `resultados_aproximado.csv` pelo modo `aprox`.
