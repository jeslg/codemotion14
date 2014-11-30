codemotion14
============

Código que será utilizado en el workshop *Programación funcional con Scala y aplicaciones para la Web* impartido en [CodeMotion 2014](http://2014.codemotion.es/es/). Las diapositivas de la charla se encuentran en [este enlace](https://github.com/jeslg/codemotion14/blob/master/doc/codemotion14.pdf).

### Instrucciones para descargar el software necesario

1. Clonar este repositorio `git clone http://github.com/jeslg/codemotion14.git`
2. Ejecutar `codemotion14/activator` (`codemotion14/activator.bat` para Windows)
3. Entraremos en una consola, donde debemos ejecutar `test`. Este proceso puede llevarnos algo de tiempo la primera vez que se ejecuta, ya que requiere descargar todas las dependencias del proyecto. (Desde la organización nos comunican que no se podrá descargar ningún software durante el workshop, por lo que realizar este paso previamente será imprescindible)
4. Si todo ha ido bien, obtendremos varias trazas de color verde, indicando que los tests han ejecutado correctamente.
5. También será necesario **cualquier editor de texto**, preferiblemente con resaltado de sintaxis para Scala (EMACS, Sublime, vim, gedit...)
6. Finalmente, para probar el código que iremos desplegando, **requeriremos un cliente REST**. Durante la charla, utilizaremos *RESTClient* en Firefox.

**(*) MUY IMPORTANTE: Todavía estamos aplicando los últimos cambios al proyecto. No te olvides de actualizar el repositorio el viernes por la noche (mediante `git pull`) ¡Muchas gracias!**

### Descripción de la charla

El objetivo del workshop es introducir las abstracciones y principios básicos de la programación funcional a programadores noveles en este paradigma, utilizando Scala como lenguaje de referencia y el framework de desarrollo Web Play. La idea es mostrar cómo los problemas a los que se enfrenta un desarrollador Web pueden ser resueltos mediante funciones "puras", tipos algebraicos, pattern matching, functores, mónadas, etc. Se utilizarán para ello ejemplos sencillos de la programación con Play sobre BodyParsers, Iteratees, AsynchResults, etc. El workshop se desarrollará en base a una metodología de live-coding de tal manera que todo aquel que quiera pueda seguir con su portátil los ejemplos presentados. En resumen, el workshop tiene un doble propósito: enseñar qué significa programar funcionalmente y cuáles son sus ventajas, y mostrar cómo se aplica esta filosofía a problemas familiares para los asistentes como es el desarrollo Web. 
