# Misión Principal - Actividad Práctica 2
## Librería para la manejo de Excepciones de sentencias SQL en archivos SCV.

Se necesita un framework de Excepciones para manejar las distintas situaciones anómalas (Excepciones) de la librería para el menejo de sentencias SQL (Práctica 1). Se deberán establecer excepciones marcadas y no marcadas dentro del framework.

Del mismo modo se deberá implementar un sistema interactivo (interprete interactivo) para consultar tablas (archivos CSV) de diferentes bases de datos (carpetas). Del mismo modo se deberán agregar funciones *BUIT-IN* (Ver anexo).

---

## Entregables

  1. Archivo JAR auto-ejecutable del sistema.
  2. Código fuente, éste se mantendrá en el respositorio.
  3. Como parte del repositorio se deberán programar todas las pruebas unitarias necesarias para probar el funcionamiento del sistema.
  4. Se deberá realizar un reporte que contenga la explicación del funcionamiento del sistema, así como la explicación a detalle de las pruebas unitarias realizadas. *EL REPORTE SE SUBIRÁ EN FORMATO PDF EN CLASSROOM.

---

  > **Nota**
  > Se deberán generar y cachar todas las posibles excepciones. Dado que si el programa no responde o se cierra debido a una excepción. La práctica será marcada como no entregada.

---


# Anexo

  > SQL viene incluido con una serie de funciones integradas. Las funciones integradas son simplemente funciones que ya vienen implementadas en el servidor SQL. Estas funciones nos permiten realizar diferentes tipos de manipulaciones en los datos. Las funciones integradas se pueden clasificar básicamente en las siguientes categorías más utilizadas.
  >
  > **Funciones de cadenas** – operan en tipos de datos de cadena
  > **Funciones numéricas** : opere en tipos de datos numéricos
  > **Funciones de fecha** : operan en tipos de datos de fecha
  > **Funciones agregadas** : opere en todos los tipos de datos anteriores y produzca conjuntos de resultados resumidos.

### Funciones de cadena

 El script que se muestra a continuación demuestra el uso de la función “UCASE”.

   ```SQL
   SELECT `movie_id`,` title`, UCASE (`title`) FROM` movies`;
   ```


Aqui `UCASE ('title')` es la función incorporada que toma el título como parámetro y lo devuelve en letras mayúsculas con el nombre de alias `upper_case_title`.


## Funciones numéricas
Podemos realizar cálculos matemáticos sobre datos numéricos en las declaraciones SQL.

### Operadores aritméticos

SQL admite los siguientes operadores aritmáticos que se pueden usar para realizar cálculos en las sentencias de SQL.

| **Nombre** | **Descripción** |
|------------|-----------------|
| DIV        | División entera |
| /          | División        |
| –          | Sustracción     |
| +          | Adición         |
| *          | Multiplicación  |
| % o MOD    | Módulo          |

### División entera (DIV)

 ```SQL
 SELECT 23 DIV 6;
 ```
Ejecutar el script anterior nos da los siguientes resultados.

`3`

### Operador de división (/)

Veamos ahora el ejemplo del operador de división. Modificaremos el ejemplo de DIV.

 ```SQL
 SELECT 23/6;
 ```
Ejecutar el script anterior nos da los siguientes resultados.

`3.8333`

### Operador de resta (-)

Veamos ahora el ejemplo del operador de resta. Utilizaremos los mismos valores que en los dos ejemplos anteriores

 ```SQL
 SELECT 23 - 6;
 ```
Ejecutar el script anterior nos da `17`

### Operador adicional (+)

Veamos ahora el ejemplo del operador de suma. Modificaremos el ejemplo anterior.

 ```SQL
 SELECT 23 + 6;
 ```
Ejecutar el script anterior nos da `29`

### Operador de multiplicación (*)

Veamos ahora el ejemplo del operador de multiplicación. Usaremos los mismos valores que en los ejemplos anteriores.

 ```SQL
 SELECT 23 * 6 AS `multiplication_result`;
 ```
Ejecutar el script anterior nos da los siguientes resultados.

```
multiplication_result
138
``` 

### Operador Modulo (-)

El operador de módulo divide N por M y nos da el recordatorio. Veamos ahora el ejemplo del operador de módulo. Usaremos los mismos valores que en los ejemplos anteriores.

 ```SQL
 SELECT 23% 6;
 ```
OR

 ```SQL
 SELECT 23 MOD 6;
 ```
 

Ejecutar el script anterior nos da `5`

Veamos ahora algunas de las funciones numéricas comunes en SQL.

FLOOR : esta función elimina decimales de un número y lo redondea al número más bajo más cercano. El script que se muestra a continuación demuestra su uso.

 ```SQL
 SELECT FLOOR (23/6) AS `floor_result`;
 ```
 

Ejecutar el script anterior nos da los siguientes resultados.
```
Floor_result
3
```

ROUND : esta función redondea un número con decimales al número entero más cercano. El script que se muestra a continuación demuestra su uso.

 ```SQL
 SELECT ROUND (23/6) AS `round_result`;
 ```
Ejecutar el script anterior nos da los siguientes resultados.
```
Round_result
4
``` 

RAND : esta función se usa para generar un número aleatorio, su valor cambia cada vez que se llama a la función. El script que se muestra a continuación demuestra su uso.

 ```SQL
 SELECT RAND() AS `random_result`;
 ```

## Funciones agregadas

### COUNT función

La función `COUNT` devuelve el número total de valores en el campo especificado. Funciona tanto en tipos de datos numéricos como no numéricos. Todas las funciones agregadas por defecto excluyen valores nulos antes de trabajar en los datos.

`COUNT (*)` es una implementación especial de la función `COUNT` que devuelve el recuento de todas las filas en una tabla especificada. `COUNT (*)` también considera nulos y duplicados.

La tabla que se muestra a continuación muestra los datos en la tabla movierentals

| **número de referencia** | **Fecha de Transacción** | **Fecha de regreso** | **número de socio** | **movie_id** | **movie_ returned** |
|----------------------|----------------------|------------------|-----------------|----------|-----------------|
| 11                   | 20-06-2012           | NULO             | 1               | 1        | 0               |
| 12                   | 22-06-2012           | 25-06-2012       | 1               | 2        | 0               |
| 13                   | 22-06-2012           | 25-06-2012       | 3               | 2        | 0               |
| 14                   | 21-06-2012           | 24-06-2012       | 2               | 2        | 0               |
| 15                   | 23-06-2012           | NULO             | 3               | 3        | 0               |

Supongamos que queremos obtener el número de veces que la película con ID 2 se ha alquilado

```SQL
SELECT COUNT (`movie_id`) FROM` movierentals` WHERE `movie_id` = 2;
```
 
Ejecutar la consulta anterior en SQL workbench contra myflixdb nos da los siguientes resultados.
```SQL
COUNT (‘movie_id’)
3
```

### Palabra clave DISTINCT

La palabra clave `DISTINCT` que nos permite omitir los duplicados de nuestros resultados. Esto se logra agrupando valores similares juntos.

Para apreciar el concepto de Distinct, vamos a ejecutar una consulta simple
```SQL
 SELECT `movie_id` FROM` movierentals`;


movie_id
1
2
2
2
3
```
Ahora ejecutemos la misma consulta con la palabra clave distinct –

```SQL
SELECT DISTINCT `movie_id` FROM` movierentals`;
```
 
Como se muestra a continuación, distinct omite registros duplicados de los resultados.

```
movie_id
1
2
3
```

### Función MIN

La función `MIN` devuelve el valor más pequeño en el campo de tabla especificado .

Como ejemplo, supongamos que queremos saber el año en el que se lanzó la película más antigua de nuestra biblioteca, podemos usar la función `MIN`` de SQL para obtener la información deseada.

La siguiente consulta nos ayuda a lograr eso

```SQL
SELECT MIN (`year_released`) FROM` movies`;
```
Ejecutar la consulta anterior en SQL workbench contra myflixdb nos da los siguientes resultados.

```SQL
MIN (‘año liberado’)
2005
```

### Función MAX

Tal como su nombre lo sugiere, la función `MAX` es lo opuesto a la función `MIN`. Se devuelve el valor más grande desde el campo de la tabla especificada .

Supongamos que queremos obtener el año en que se lanzó la última película en nuestra base de datos. Podemos usar fácilmente la función `MAX` para lograr eso.

El siguiente ejemplo devuelve el último año de la película publicado.

```SQL
SELECT MAX (`year_released`) FROM` movies`;
```
Ejecutar la consulta anterior en SQL workbench utilizando myflixdb nos da los siguientes resultados.

```SQL
MAX (‘year_released’)
2012
```

### Función SUMA

Supongamos que queremos un informe que dé la cantidad total de pagos realizados hasta el momento. Podemos usar la función `SUMA` de SQL que devuelve la suma de todos los valores en la columna especificada . SUM funciona solo en campos numéricos . Los valores nulos se excluyen del resultado devuelto.

La siguiente tabla muestra los datos en la tabla de pagos.

| **ID de pago** | **número de socio** | **fecha de pago** | **descripción**              | **cantidad pagada** | **número de referencia_externo** |
|----------------|---------------------|-------------------|------------------------------|---------------------|----------------------------------|
| 1              | 1                   | 23-07-2012        | Pago de alquiler de película | 2500                | 11                               |
| 2              | 1                   | 25-07-2012        | Pago de alquiler de película | 2000                | 12                               |
| 3              | 3                   | 30-07-2012        | Pago de alquiler de película | 6000                | NULO                             |

a consulta que se muestra a continuación obtiene todos los pagos realizados y los resume para devolver un único resultado.

```SQL
SELECT SUM(`amount_paid`) FROM` payments`;
```
Ejecutar la consulta anterior en MySQL workbench contra myflixdb da los siguientes resultados.

```
SUM (‘amount_paid’)
10500
```

### Función AVG

La función de SQL AVG devuelve el promedio de los valores en una columna especificada. Al igual que la función SUMA, funciona solo en tipos de datos numéricos .

Supongamos que queremos encontrar el monto promedio pagado. Podemos usar la siguiente consulta:

```SQL
SELECT AVG(`amount_paid`) FROM` payments`;
```
Ejecutando la consulta anterior en MySQL workbench, nos da los siguientes resultados.

```
AVG (‘amount_paid’)
3500
```

