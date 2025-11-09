# Structure du Projet

## Organisation des Dossiers

```
src/
├── LexicalAnalyzer/          # Analyseur Lexical
│   ├── Main.java             # Point d'entrée de l'analyseur lexical
│   ├── LexicalAnalyzer.java  # Généré par JFlex
│   ├── LexicalUnit.java      # Enum des unités lexicales
│   ├── Symbol.java           # Classe représentant un symbole
│   ├── SymbolTable.java      # Table des symboles
│   ├── TokenSequence.java    # Séquence de tokens
│   └── lexical_analyzer.flex # Spécification JFlex
│
└── Parser/                   # Analyseur Syntaxique
    ├── Parser.java           # Analyseur syntaxique
    ├── ParseException.java   # Exception de parsing
    └── Token.java            # Enum des tokens pour le parser

test/                         # Fichiers de test
├── Euclid.ycc               # Programme exemple
├── testProgram.ycc          # Programme de test
├── LexicalAnalyzerOutput.txt # Sortie de l'analyseur lexical
└── ParsingOutput.txt        # Sortie du parser

dist/                         # Fichiers JAR générés
├── part1.jar                # JAR de l'analyseur lexical
└── parser.jar               # JAR du parser

bin/                          # Fichiers compilés (.class)
```

## Commandes Make

### Compilation

**Windows:**
```powershell
make lexical_analyzer_win    # Compile l'analyseur lexical
make parser_win              # Compile le parser
```

**Unix/Linux/Mac:**
```bash
make lexical_analyzer_unix   # Compile l'analyseur lexical
make parser_unix             # Compile le parser
```

### Exécution

```bash
make run_lexical             # Exécute l'analyseur lexical (génère LexicalAnalyzerOutput.txt)
make run_parser              # Exécute le parser (lit LexicalAnalyzerOutput.txt)
```

### Nettoyage

**Windows:**
```powershell
make clean_win               # Nettoie tous les fichiers générés
```

**Unix/Linux/Mac:**
```bash
make clean_unix              # Nettoie tous les fichiers générés
```

### Documentation

```bash
make doc                     # Génère la documentation Javadoc
```

## Flux de Travail

1. **Compiler l'analyseur lexical**
   ```
   make lexical_analyzer_win
   ```

2. **Exécuter l'analyseur lexical** (génère les tokens)
   ```
   make run_lexical
   ```
   → Crée `test/LexicalAnalyzerOutput.txt`

3. **Compiler le parser**
   ```
   make parser_win
   ```

4. **Exécuter le parser** (analyse syntaxique)
   ```
   make run_parser
   ```
   → Lit `test/LexicalAnalyzerOutput.txt`
   → Crée `test/ParsingOutput.txt`

## Packages Java

- **LexicalAnalyzer** : Contient toutes les classes de l'analyseur lexical
- **Parser** : Contient toutes les classes de l'analyseur syntaxique
