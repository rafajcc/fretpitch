# FretPitch

Aplicación Android nativa para practicar el reconocimiento de notas musicales en el diapasón de la guitarra.

La app genera ejercicios aleatorios ("Do en la cuerda 3"), escucha el micrófono en tiempo real y evalúa si la nota tocada es correcta.

---

## Características

- **3 modos de práctica**: Una nota, una cuerda, todas las notas y cuerdas
- **Detección de pitch en tiempo real** mediante autocorrelación con interpolación parabólica
- **Toggle de sostenidos** para incluir/excluir notas con #
- **Velocidad ajustable** de 1 a 10 segundos por nota con flechas arriba/abajo
- **Feedback inmediato**: tick verde (acierto) o X roja (fallo) con animación spring
- **Sonidos de feedback**: tonos ascendentes (acierto) o descendentes (fallo) generados por AudioTrack
- **Estadísticas de sesión**: aciertos/fallos totales, por nota, por cuerda y por combinación
- **Permisos de micrófono** con manejo de denegación y redirección a configuración

---

## Arquitectura

```
com.fretpitch/
├── domain/           Modelos, casos de uso, interfaces
├── data/             AudioCapture, PitchDetector, TonePlayer, FrequencyMapper
├── presentation/     Theme, Components, Screens, ViewModel
└── di/               Módulos Hilt
```

| Patrón | Implementación |
|--------|---------------|
| MVVM | `MainViewModel` + `StateFlow<MainUiState>` |
| Clean Architecture | Domain puro → Data con Android → Presentation con Compose |
| Repository | `PitchDetector` (interfaz) → `PitchDetectorImpl` (implementación) |
| Use Cases | `GenerateExerciseUseCase`, `CalculateStatsUseCase` |
| DI | Hilt con `@Singleton`, `@HiltViewModel`, `@Binds` |

---

## Detección de Pitch

El algoritmo de detección funciona así:

1. **AudioCapture**: Graba a 44100 Hz, mono, PCM 16-bit con buffer de 4096 samples
2. **Ventana de Hann**: Reduce leakage espectral
3. **Autocorrelación normalizada**: Busca el lag con mayor correlación entre 80 Hz y 1100 Hz
4. **Interpolación parabólica**: Refina el lag para precisión sub-muestral
5. **Comparación**: Compara la frecuencia detectada con la objetivo usando una tolerancia de ±50 cents

No depende de librerías externas — el algoritmo está implementado directamente en `PitchDetectorImpl.kt`.

---

## Notas y Cuerdas

| Cuerda | Nota abierta | MIDI | Rango (trastes 0-12) |
|--------|-------------|------|----------------------|
| 1 (Mi grave) | E4 | 64 | E4 → E5 |
| 2 (Si) | B3 | 59 | B3 → B4 |
| 3 (Sol) | G3 | 55 | G3 → G4 |
| 4 (Re) | D3 | 50 | D3 → D4 |
| 5 (La) | A2 | 45 | A2 → A3 |
| 6 (Mi agudo) | E2 | 40 | E2 → E3 |

Notas disponibles: Do, Do#, Re, Re#, Mi, Fa, Fa#, Sol, Sol#, La, La#, Si

La app solo genera combinaciones válidas — una nota que no se puede tocar en una cuerda dada nunca aparecerá como ejercicio.

---

## Stack Tecnológico

| Componente | Versión |
|-----------|---------|
| Gradle | 9.5.0 |
| AGP | 9.3.2 |
| Kotlin | 2.2.10 |
| Compose BOM | 2024.05.00 |
| Hilt | 2.60.1 |
| KSP | 2.2.10-2.0.2 |
| Min SDK | 33 (Android 13) |
| Target SDK | 34 |

---

## Construcción y Ejecución

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/rafajcc/fretpitch.git
   ```

2. Abrir en Android Studio

3. Sync Gradle (File → Sync Project with Gradle Files)

4. Conectar un dispositivo Android 13+ o crear un emulador con micrófono

5. Run ▶

---

## Estructura de Archivos

```
fretpitch/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/
│       │   ├── drawable/          Iconos vectoriales
│       │   ├── mipmap-anydpi-v26/ Adaptive icon
│       │   └── values/            strings.xml, themes.xml
│       └── java/com/fretpitch/
│           ├── FretPitchApp.kt           @HiltAndroidApp
│           ├── MainActivity.kt           Activity principal
│           ├── di/AppModule.kt           Bindings Hilt
│           ├── domain/
│           │   ├── model/                Note, GuitarString, Exercise, AppMode, SessionResult
│           │   ├── usecase/              GenerateExerciseUseCase, CalculateStatsUseCase
│           │   └── repository/           PitchDetector (interfaz)
│           ├── data/
│           │   ├── audio/                AudioCapture, PitchDetectorImpl, TonePlayer
│           │   └── mapper/               FrequencyMapper
│           └── presentation/
│               ├── model/                MainUiState, FeedbackState
│               ├── viewmodel/            MainViewModel
│               ├── component/            NoteDisplay, SpeedControl, ModeSelector,
│               │                         FeedbackOverlay, StatsPanel, PermissionHandler
│               ├── screen/               MainScreen, StatsScreen
│               └── theme/                Color, Type, Theme
```

---

## Licencia

Proyecto privado.
