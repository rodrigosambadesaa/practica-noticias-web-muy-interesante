# Noticias Muy Interesante

Aplicación Android que carga el feed RSS de Muy Interesante y conserva las noticias para lectura offline.

## Flujo de conectividad

- El observer pasivo de `ConnectivityAndInternetAccess` actualiza el estado visual sin generar tráfico.
- Antes de cada operación remota (carga inicial, refresh, paginación y retry) se consulta una sola vez `isConnected()`, basado en la red activa y `NetworkCapabilities`.
- Sin una red utilizable no se lanza ninguna petición: se muestra la caché y el estado offline.
- Con red utilizable se ejecuta directamente la petición RSS real. Esa petición gestiona redirects, timeouts, códigos HTTP y excepciones.
- Un código HTTP válido no dispara un diagnóstico adicional. Solo un fallo ambiguo sin respuesta HTTP válida ejecuta `checkInternetAsyncDefault(...)` para distinguir entre feed caído y problema general de Internet.
- Las imágenes también usan la caché y el mismo guard barato, sin sondeo activo previo.

## Verificación

```text
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

La release se firma con una keystore persistente mediante `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` y `ANDROID_KEY_PASSWORD`. El workflow de GitHub Actions obtiene esa keystore desde `ANDROID_KEYSTORE_BASE64`.
