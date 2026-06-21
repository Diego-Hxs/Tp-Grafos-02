JAVAC := javac
JAVA  := java
SRC   := src
OUT   := out

.PHONY: all testes aprox exata lista limpar

all: $(OUT)
	$(JAVAC) -d $(OUT) $(SRC)/*.java

$(OUT):
	mkdir -p $(OUT)

testes: all
	$(JAVA) -cp $(OUT) Testes

aprox: all
	$(JAVA) -cp $(OUT) Main aprox

# uso: make exata I=1        (instancia 1, timeout padrao 60s)
# uso: make exata I=5 S=120  (instancia 5, timeout 120s)
I ?= 1
S ?= 60
exata: all
	$(JAVA) -cp $(OUT) Main exata $(I) $(S)

lista: all
	$(JAVA) -cp $(OUT) Main lista

limpar:
	rm -rf $(OUT)
