# FretPitch

Aplicación Android nativa para practicar el reconocimiento de notas musicales en el diapasón de la guitarra.

La app genera ejercicios aleatorios ("Do en la cuerda 3"), escucha el micrófono en tiempo real y evalúa si la nota tocada es correcta.

---

## Características

- **Práctica Totalmente Flexible**: Selección múltiple de cualquier combinación de cuerdas (1-6) y notas (Do-Si).
- **Detección de Pitch de Alta Precisión**: Motor basado en el algoritmo **YIN** para una detección monofónica robusta y con rechazo de errores de octava.
- **Alta Sensibilidad**: Optimizado para guitarras eléctricas desenchufadas y entornos silenciosos.
- **Diseño Moderno (Material 3)**: Interfaz limpia con soporte para **Dynamic Color** (Android 12+) y Modo Claro/Oscuro.
- **Persistencia de Datos**:
    - **DataStore**: Guarda tus preferencias de velocidad y selección de práctica.
    - **Room Database**: Historial persistente de sesiones para seguimiento del progreso.
- **Feedback Visual y Sonoro**: Animaciones "spring" fluidas y tonos de referencia generados mediante AudioTrack.
- **Estadísticas Detalladas**: Aciertos, errores y precisión por nota, cuerda y combinación.

---

## Arquitectura

```
com.fretpitch/
├── domain/           Modelos, casos de uso, interfaces de repositorio
├── data/             Implementaciones de Audio, Repositorios, Room DB y DataStore
├── presentation/     UI con Compose (M3), ViewModels y Tematización
└── di/               Módulos Hilt (App y Database)
```

| Patrón | Implementación |
|--------|---------------|
| MVVM | `MainViewModel` + `StateFlow<MainUiState>` |
| Clean Architecture | Domain puro → Data con Android → Presentation con Compose |
| Repository | `PitchDetector` e `UserPreferencesRepository` |
| Persistencia | Room (Sesiones) y DataStore (Preferencias) |
| DI | Hilt con `@Singleton`, `@Binds` y `@Provides` |

---

## Motor de Audio (YIN)

El sistema de detección ha evolucionado de una simple autocorrelación al algoritmo **YIN**, siguiendo estos pasos:

1. **Difference Function**: Calcula la diferencia cuadrática entre la señal y su versión desplazada.
2. **CMNDF**: Normalización acumulada para eliminar valles falsos y evitar saltos de octava.
3. **Absolute Threshold**: Búsqueda del primer mínimo local por debajo de un umbral de confianza estricto.
4. **Interpolación Parabólica**: Refinado del lag para obtener una frecuencia con precisión sub-muestral.

---

## Notas y Cuerdas

| Cuerda | Nota abierta | MIDI | Rango (trastes 0-12) |
|--------|-------------|------|----------------------|
| 1 (Mi agudo) | E4 | 64 | E4 → E5 |
| 2 (Si) | B3 | 59 | B3 → B4 |
| 3 (Sol) | G3 | 55 | G3 → G4 |
| 4 (Re) | D3 | 50 | D3 → D4 |
| 5 (La) | A2 | 45 | A2 → A3 |
| 6 (Mi grave) | E2 | 40 | E2 → E3 |

La app genera automáticamente el producto cartesiano de tu selección y filtra las combinaciones que exceden el traste 12.

---

## Stack Tecnológico

| Componente | Versión |
|-----------|---------|
| Gradle | 9.7.1 |
| AGP | 9.3.2 |
| Kotlin | 2.2.10 |
| Compose BOM | 2024.12.01 |
| Room | 2.8.4 |
| DataStore | 1.2.1 |
| Hilt | 2.60.1 |
| KSP | 2.3.9 |
| Target SDK | 35 |

---

## Calidad y Testing

El proyecto cuenta con una suite de **17 tests unitarios** que se ejecutan en la JVM, incluyendo tests de lógica pura (use cases y detección de pitch) y tests que usan **Robolectric** para validar la capa de datos (preferencias y base de datos) sin necesidad de emulador.

Ejecutar tests:
```bash
./gradlew test
```

---

## Licencia

Proyecto privado. v4.1.0 - por rafajcc & Vera Technology.
