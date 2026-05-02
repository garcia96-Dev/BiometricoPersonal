# 📱 Biométrico Personal — App Android

Control de asistencia personal con autenticación biométrica, ajustado a la **Ley 2101 de 2021** (reducción gradual de jornada laboral en Colombia).

---

## 🇨🇴 Ley 2101 de 2021 — Reducción de jornada

| Fecha           | Horas semanales |
|-----------------|-----------------|
| Hasta jul 2023  | 48h             |
| Jul 2023 - 2024 | 47h             |
| Jul 2024 - 2025 | 46h             |
| **Jul 2025 - 2026** | **44h ← VIGENTE** |
| Jul 2026 en adelante | 42h        |

La app detecta automáticamente la jornada vigente según la fecha actual.

### Reforma Laboral 2024
- Jornada nocturna: desde las **7:00 PM** (antes 9:00 PM)
- Recargo nocturno: **35%**

---

## ✨ Funcionalidades

### 🏠 Pantalla Principal
- Reloj en tiempo real
- Botón **ENTRADA** con autenticación biométrica (huella/Face ID)
- Botón **SALIDA** con autenticación biométrica
- Cálculo automático de horas trabajadas y horas extra
- Estado visual del turno (sin registro / en jornada / completado)

### 📅 Historial
- Lista de registros del mes actual
- Resumen mensual: total horas, extras y días asistidos
- Indicadores de color por estado del registro

### ⚙️ Ajustes (muy completos)
- **Datos personales**: nombre, empresa, cargo
- **Jornada laboral**: selección de la etapa de la Ley 2101, o personalizada
- **Horario diario**: hora entrada/salida con TimePicker, duración almuerzo
- **Días laborales**: selección de cada día con switch
- **Tolerancias**: minutos de gracia entrada/salida con slider
- **Recargos legales**: nocturno, extra diurna/nocturna, dominical/festivo
- **Seguridad**: toggle huella / PIN
- **Notificaciones**: recordatorios de entrada y salida

---

## 🛠️ Instalación en Android Studio

### Requisitos
- Android Studio Hedgehog o superior
- JDK 17
- Android SDK 26+
- Kotlin 1.9+

### Pasos
1. Abre Android Studio
2. File → Open → selecciona la carpeta `BiometricoPersonal`
3. Espera que Gradle sincronice
4. Conecta un dispositivo Android (API 26+) o usa el emulador
5. Run → Run 'app'

### Nota sobre MPAndroidChart
Agrega el repositorio JitPack en `settings.gradle`:
```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }  // ← Agregar esta línea
    }
}
```

---

## 📂 Estructura del proyecto

```
app/src/main/
├── java/com/biometrico/personal/
│   ├── MainActivity.kt
│   ├── data/
│   │   ├── model/          # RegistroAsistencia, ConfiguracionHorario
│   │   ├── database/       # Room DB, DAOs
│   │   └── repository/     # BiometricoRepository
│   └── ui/
│       ├── home/           # HomeFragment + ViewModel
│       ├── history/        # HistoryFragment + ViewModel
│       ├── settings/       # SettingsFragment + ViewModel
│       └── adapters/       # RecyclerView adapters
└── res/
    ├── layout/             # XMLs de pantallas
    ├── navigation/         # Grafo de navegación
    ├── menu/               # Menú bottom navigation
    └── values/             # Colores, strings, temas
```

---

## 📋 Tecnologías usadas

- **Kotlin** + **Coroutines**
- **Room Database** (SQLite)
- **ViewModel + LiveData** (arquitectura MVVM)
- **Navigation Component** + **Bottom Navigation**
- **AndroidX Biometric** (huella / Face ID)
- **Material Design 3**
- **View Binding**
