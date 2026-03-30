# Skill: android-debug-adb

## Propósito
Diagnóstico rápido de fallos en el dispositivo/emulador sin depender solo del IDE.

## Comandos esenciales

### Logcat filtrado por paquete
```bash
adb logcat --pid=$(adb shell pidof -s com.pedrogm.tdtflow)
# o por tag específico
adb logcat -s TdtViewModel:D ExoPlayer:W
# limpiar buffer antes de reproducir el bug
adb logcat -c && adb logcat | grep -E "tdtflow|ExoPlayer|Hilt"
```

### SharedPreferences — leer favoritos
```bash
adb shell run-as com.pedrogm.tdtflow \
  cat /data/data/com.pedrogm.tdtflow/shared_prefs/favorites_prefs.xml
```

### DataStore / archivos internos
```bash
adb shell run-as com.pedrogm.tdtflow ls /data/data/com.pedrogm.tdtflow/files/
```

### Forzar crash para probar Crashlytics
```bash
adb shell am start -n com.pedrogm.tdtflow/.MainActivity \
  --ez force_crash true
```

### Ver procesos activos
```bash
adb shell ps | grep tdtflow
```

### Instalar APK de debug y lanzar
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.pedrogm.tdtflow/.MainActivity
```

## Workflow de diagnóstico

1. **Reproducir** el bug con logcat limpio (`adb logcat -c`).
2. **Filtrar** solo los tags relevantes — nunca leer el logcat completo.
3. **Verificar estado persistido** (SharedPreferences, DataStore) si el bug parece de datos.
4. **Comparar** comportamiento en debug vs release (ProGuard puede ocultar clases).

## Paquete de la app
`com.pedrogm.tdtflow`
