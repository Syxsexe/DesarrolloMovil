# IMCApp

Aplicación Android para calcular el **Índice de Masa Corporal (IMC)**, con tres pantallas:
una animación de bienvenida, la calculadora y los datos del desarrollador.

Escrita en **Kotlin** con vistas XML y **Material Design 3**.

> Este proyecto es una de las ramas del repositorio
> [DesarrolloMovil](https://github.com/Syxsexe/DesarrolloMovil).

---

## Capturas

| 1. Animación | 2. Calculadora | 3. Desarrollador |
| :---: | :---: | :---: |
| <img src="docs/screenshots/01-splash.png" width="240" alt="Pantalla de animación"> | <img src="docs/screenshots/02-calculadora.png" width="240" alt="Calculadora de IMC"> | <img src="docs/screenshots/03-desarrollador.png" width="240" alt="Detalles del desarrollador"> |

---

## Las tres pantallas

### 1. Animación de bienvenida — `SplashActivity`

Es la pantalla de lanzamiento. Dura 2.6 segundos y luego abre la calculadora con un fundido.

- Fondo con degradado diagonal azul → verde agua.
- El logo entra con rebote (`OvershootInterpolator`) dentro de un anillo punteado que gira en bucle.
- Latido suave del icono, títulos que suben con desvanecido escalonado y barra de progreso
  que marca el tiempo restante.

### 2. Calculadora de IMC — `MainActivity`

- Campos de **peso (kg)** y **altura (cm)** con validación: campo vacío, texto no numérico
  y rangos fuera de lo razonable. Acepta coma o punto como separador decimal.
- El resultado aparece deslizándose y el número **cuenta desde 0** hasta el IMC final.
- Categoría según la OMS con color propio, mensaje orientativo y **rango de peso ideal**
  calculado para esa altura.
- Escala de colores de 15 a 40 con un marcador triangular que se desliza hasta tu posición.
- Botón **Limpiar** que reinicia el formulario y oculta el resultado con animación.

| IMC | Categoría |
| --- | --- |
| menor a 18.5 | Bajo peso |
| 18.5 – 24.9 | Peso normal |
| 25.0 – 29.9 | Sobrepeso |
| 30.0 – 34.9 | Obesidad grado I |
| 35.0 – 39.9 | Obesidad grado II |
| 40.0 o más | Obesidad grado III |

### 3. Detalles del desarrollador — `DeveloperActivity`

- Avatar con iniciales sobre un círculo con degradado.
- Tarjetas de *Sobre mí*, *Contacto* y *Tecnologías usadas*, que entran escalonadas.
- El correo y el enlace de GitHub son accionables: abren la app de correo o el navegador.

---

## Cómo ejecutarlo

1. Clonar esta rama:

   ```bash
   git clone -b imc-app https://github.com/Syxsexe/DesarrolloMovil.git
   ```

2. Abrir la carpeta en **Android Studio** y esperar la sincronización de Gradle.
3. Ejecutar en un emulador o dispositivo con **Android 8.0 (API 26)** o superior.

Desde la terminal:

```bash
./gradlew assembleDebug          # genera el APK de depuración
./gradlew testDebugUnitTest      # ejecuta las pruebas unitarias
```

---

## Estructura del proyecto

```
app/src/main/java/com/example/imcapp/
├── SplashActivity.kt        Pantalla 1: animación de bienvenida
├── MainActivity.kt          Pantalla 2: calculadora de IMC
├── DeveloperActivity.kt     Pantalla 3: detalles del desarrollador
├── ImcCalculator.kt         Lógica de cálculo, categorías y validaciones
└── ActivityTransitions.kt   Transiciones entre pantallas

app/src/main/res/
├── anim/                    Deslizamientos y fundidos entre Activities
├── drawable/                Degradados, formas e iconos vectoriales
├── layout/                  Las tres pantallas
└── values/                  Colores, textos y temas (con variante oscura)

app/src/test/java/com/example/imcapp/
└── ImcCalculatorTest.kt     Pruebas de la lógica de cálculo
```

La lógica de cálculo está aislada en `ImcCalculator`, sin dependencias de Android, para poder
probarla con pruebas unitarias normales: verifica el IMC, el peso ideal, los límites exactos de
cada categoría y el parseo de números con coma o punto.

---

## Tecnologías

- Kotlin
- Android SDK — `minSdk` 26, `targetSdk` 37
- Material Design 3 (`com.google.android.material`)
- ConstraintLayout
- Animaciones con `ViewPropertyAnimator`, `ObjectAnimator` y `ValueAnimator`
- JUnit 4
- Gradle 9.5 con AGP 9.3.2

---

## Aviso

El IMC es un indicador orientativo y no reemplaza un diagnóstico médico.

---

Autor: [@Syxsexe](https://github.com/Syxsexe)
