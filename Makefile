MAIN = Main.java
BINDIR = bin/

all:
	javac -d $(BINDIR) $(MAIN)

run: all
	java -cp bin/ Main
