# Desarrollo Móvil

Repositorio de los proyectos de la asignatura de Desarrollo Móvil.
**Cada proyecto vive en su propia rama**; esta rama (`main`) solo contiene este índice.

## Proyectos

| Rama | Proyecto | Descripción |
| --- | --- | --- |
| [`imc-app`](../../tree/imc-app) | IMCApp | Calculadora de Índice de Masa Corporal con tres pantallas: animación de bienvenida, calculadora y detalles del desarrollador. Kotlin + Material 3. |

## Cómo abrir un proyecto

Clonar solo la rama que interesa:

```bash
git clone -b imc-app https://github.com/Syxsexe/DesarrolloMovil.git
```

O, si ya tienes el repositorio clonado:

```bash
git fetch origin
git switch imc-app
```

Cada rama es un proyecto de Android Studio independiente: se abre la carpeta raíz de la rama
directamente desde Android Studio y se sincroniza con Gradle.

## Cómo subir un proyecto nuevo

Desde la carpeta del proyecto nuevo:

```bash
git init -b nombre-del-proyecto
git add -A
git commit -m "Primer commit"
git remote add origin https://github.com/Syxsexe/DesarrolloMovil.git
git push -u origin nombre-del-proyecto
```

Después basta con agregar una fila a la tabla de arriba desde la rama `main`.

---

Autor: [@Syxsexe](https://github.com/Syxsexe)
