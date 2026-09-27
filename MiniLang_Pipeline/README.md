# MiniLang Pipeline – EIF400 Paradigmas de Programación

Pipeline poliglota de tres etapas que lee un mini-lenguaje de
transformación de datos (`programa.mini`), lo valida y traduce con
**Java**, lo ejecuta con estilo funcional en **Python** y genera una
firma de verificación en **MIPS Assembly**.

**Autores:** Randall AC · Keilor MC
**Curso:** EIF400 Paradigmas de Programación · II Ciclo 2026
**Modalidad:** Parte B del examen parcial (parejas)

---

## 1. Visión general del pipeline

```
programa.mini
      |
      v
+-----------------------------+
| JAVA                        |  Lexer + Parser + jerarquía OOP
| (análisis y traducción)     |  Herencia y polimorfismo (toIR)
+-------------+---------------+
              |  programa.ir        (solo si el programa es válido)
              v
+-----------------------------+
| PYTHON                      |  Estilo funcional
| (ejecución)                 |  filter() / map() / reduce()
+-------------+---------------+
              |  resultado.txt      (traza + resultado final)
              v
+-----------------------------+
| MIPS (MARS)                 |  Registros, memoria, ciclos,
| (verificación)              |  aritmética, lógica, saltos
+-------------+---------------+
              |  firma.txt          (checksum de verificación)
              v
```

Ninguna etapa puede sustituirse por archivos fabricados a mano:
cada archivo intermedio es generado únicamente por la etapa anterior.

---

## 2. Requisitos

| Herramienta | Versión probada | Nota |
|-------------|-----------------|------|
| JDK | 21 | La etapa Java se compila con `javac` 21 |
| Python | 3.13 | Debe estar en el `PATH` |
| Java runtime para MARS | 8 o 21 | `Mars.jar` ya viene incluido en el repo |
| Git | 2.x | Opcional, para clonar el repo |

> **Nota importante:** si el `java` del `PATH` es una versión antigua
> (p. ej. 1.8), el archivo `run_pipeline.bat` resuelve automáticamente
> un JDK 21 (vía `JAVA_HOME` o la ruta estándar de instalación).

---

## 3. Estructura del proyecto

```
MiniLang_Pipeline/
├── src/minilang/               Etapa 1 – Java
│   ├── MiniLang_Pipeline.java  Orquestador y punto de entrada
│   ├── lexer/                  Lexer, Token, TokenType
│   ├── parser/                 Parser (gramática y estructura)
│   ├── model/                  Jerarquía OOP (Instruction + 5 subclases)
│   ├── ir/                     IRGenerator (contrato Java → Python)
│   └── exception/              LexerException, ParserException
├── python/etapa2_functional.py Etapa 2 – Python funcional
├── mips/etapa3_checksum.asm    Etapa 3 – MIPS (MARS)
├── test_cases/                 6 casos de prueba (.mini)
├── output/                     Artefactos generados (.ir, .txt)
├── Mars.jar                    Simulador MIPS (MARS 4.5)
├── run_pipeline.bat            Compila, prueba y regenera artefactos
└── README.md                   Este documento
```

---

## 4. Ejecución rápida (recomendada)

Desde la raíz del proyecto, doble click en:

```
run_pipeline.bat
```

El script:
1. Compila la etapa Java con `javac` (carpeta `build\classes`).
2. Ejecuta la suite de 6 casos de prueba (PASS/FAIL por caso).
3. Regenera los artefactos canónicos en `output\`
   (`programa.ir`, `resultado.txt`, `firma.txt`).

Salida esperada al final:

```
RESULTS: PASS=6  FAIL=0
[OK] Demo artifacts: programa.ir, resultado.txt, firma.txt
```

---

## 5. Ejecución manual etapa por etapa

Ejecutar **siempre desde la raíz del proyecto** (las rutas de los
archivos son relativas).

### Etapa 1 – Java

```cmd
"C:\Program Files\Java\jdk-21\bin\java.exe" -cp build\classes minilang.MiniLang_Pipeline
```

Argumentos opcionales: `[entrada.mini] [salida.ir]`.
Si no se indican, usa `programa.mini` y `output\programa.ir`.

### Etapa 2 – Python

```cmd
python python\etapa2_functional.py
```

Argumentos opcionales: `[entrada.ir] [salida.txt]`.
Si no se indican, busca `programa.ir` en `output\` y escribe
`output\resultado.txt`.

### Etapa 3 – MIPS (MARS)

```cmd
java -jar Mars.jar mips\etapa3_checksum.asm sm pa
```

Lee `output\resultado.txt` y escribe `output\firma.txt`.

---

## 6. Contratos de archivos

### 6.1 `programa.mini` (entrada)

```
DATA 3 8 5 10 12
FILTER > 5
MAP * 2
REDUCE SUM
PRINT
```

### 6.2 `programa.ir` (Java → Python)

Una línea por instrucción, formato `CLAVE: payload`:

```
DATA: 3,8,5,10,12
FILTER: >,5
MAP: *,2
REDUCE: SUM
PRINT: 
```

### 6.3 `resultado.txt` (Python → MIPS)

Traza corta de operaciones + resultado final:

```
STEP 0 DATA: [3, 8, 5, 10, 12]
STEP 1 FILTER > 5: [8, 10, 12]
STEP 2 MAP * 2: [16, 20, 24]
STEP 3 REDUCE SUM: 60
RESULT: 60
```

### 6.4 `firma.txt` (salida final MIPS)

```
RESULT: 60
OPERATIONS: 3
BYTESUM: 6458
CHECKSUM: 80
FIRMA: OK
```

Fórmula del checksum (extensión documentada del ejemplo del examen):

```
checksum = resultado
checksum = checksum XOR operaciones
checksum = checksum + 17
```

Para el ejemplo: `60 XOR 3 = 63`, `63 + 17 = 80`.

---

## 7. Gramática y extensiones documentadas

Gramática base del examen:

```
<programa>    ::= <data> <operacion> {<operacion>} "PRINT"
<data>        ::= "DATA" <numero> {<numero>}
<filter>      ::= "FILTER" <comparador> <numero>
<map>         ::= "MAP" <aritmetico> <numero>
<reduce>      ::= "REDUCE" ("SUM" | "MAX" | "MIN")
<comparador>  ::= ">" | "<" | ">=" | "<=" | "=="
<aritmetico>  ::= "+" | "-" | "*"
<numero>      ::= entero no negativo
```

**Extensiones agregadas por la pareja (probadas en la suite):**

| Extensión       | Detalle |
|-----------------|---------|
| `REDUCE AVG`    | Promedio como operador de reducción adicional |
| `MAP /`         | División entera truncada hacia cero |
| `FILTER !=`     | Comparador de desigualdad adicional |
| `=` normalizado | Un `=` solo se normaliza a `==` en el modelo |

**Decisiones de robustez documentadas:**

- `MAP / 0` se rechaza en el Parser con número de línea.
- `REDUCE SUM` sobre lista vacía produce `0`.
- `REDUCE MAX/MIN/AVG` sobre lista vacía es error controlado.
- El marcador EOF de DOS (carácter 26) se ignora al tokenizar.
- `programa.ir` **solo** se genera si el programa es válido.

---

## 8. Casos de prueba y resultados esperados

| # | Archivo                        | Propósito                | Resultado esperado |
|---|--------------------------------|--------------------------|--------------------|
| 1 | `test1_valid_full.mini`        | Programa válido completo | Pipeline completo. `RESULT: 60`, `CHECKSUM: 80` |
| 2 | `test2_invalid_operator.mini`  | Operador inválido `%`    | LEXER ERROR línea 2. Pipeline se detiene, sin `.ir` |
| 3 | `test3_missing_data.mini`      | Programa sin DATA        | PARSER ERROR línea 1. Pipeline se detiene, sin `.ir` |
| 4 | `test4_reduce_max.mini`        | REDUCE MAX               | `RESULT: 10`, `CHECKSUM: 26` |
| 5 | `test5_filter_empty.mini`      | FILTER deja lista vacía  | `RESULT: 0`, `CHECKSUM: 20` |
| 6 | `test6_double_map_filter.mini` | MAP/FILTER consecutivos  | `RESULT: 135`, ops 5, `CHECKSUM: 147` |

Los casos 2 y 3 cumplen el requisito de "al menos un caso de error
donde el pipeline se detiene correctamente" con número de línea.

---

## 9. Manejo de errores

- **Lexer:** caracteres desconocidos, operadores inválidos y caracteres
  de control → `LEXER ERROR: [Lexer Error at line N, col M] ...`
- **Parser:** gramática violada, estructura inválida, división por
  cero → `PARSER ERROR: [Parser Error at line N] ...`
- **Python:** IR malformado, operadores desconocidos, archivo
  ausente → `PIPELINE ERROR: ...` con número de línea del IR.
- **MIPS:** archivo de entrada ausente o sin operaciones → mensaje
  `MIPS ERROR: ...` y código de salida distinto de cero.

En todos los casos el pipeline se detiene en la etapa fallida y
**no** se generan artefactos intermedios falsos.

---

## 10. Solución de problemas

| Síntoma                           | Causa / solución |
|-----------------------------------|------------------|
| `UnsupportedClassVersionError`    | El `java` del PATH es antiguo. Usar JDK 21 o el `.bat` que lo resuelve solo |
| MARS no encuentra `resultado.txt` | Ejecutar MARS desde la raíz del proyecto (rutas relativas) |
| `build\classes` no existe         | El `.bat` compila solo; o en NetBeans: Run → Clean and Build |
| Artefactos faltantes en `output\` | Ejecutar `run_pipeline.bat` completo |

---