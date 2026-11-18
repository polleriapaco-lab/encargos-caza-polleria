# Encargos de Caza · La Pollería 2.0

Aplicación Android para registrar encargos, preparaciones, entregas parciales, avisos por WhatsApp y totales de temporada.

## Actualizaciones

- Incrementar `versionCode` y `versionName` en `app/build.gradle` al publicar una versión.
- El flujo de GitHub Actions compila el APK con la firma estable. Si falta la firma, la compilación se detiene; nunca crea otra automáticamente.
- Una tarea periódica mantiene activa la firma de compilación. Existe además una copia privada de la firma fuera del repositorio para restaurarla si se borra la caché.
- La aplicación incluye «💾 Copia»: antes de cambiar de tablet o reinstalar, guardar el archivo JSON y, después, usar «Recuperar copia».

**Migración desde 1.7:** la firma de la versión 1.7 no coincide con la de 1.8/1.9. Hay que extraer sus datos mediante ADB antes de sustituirla. No desinstalar 1.7 sin una copia verificada.
