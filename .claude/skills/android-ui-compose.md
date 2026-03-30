# Skill: android-ui-compose

## Propósito
Garantizar calidad, rendimiento y consistencia en Jetpack Compose.

## Reglas obligatorias

### State Hoisting
- El estado **siempre sube**, los eventos **siempre bajan**.
- Un `@Composable` no llama a un `ViewModel` directamente — recibe el estado y lambdas.
- Excepción permitida: el composable raíz de una pantalla puede recibir el ViewModel.

```kotlin
// Correcto
fun ChannelCard(channel: Channel, isSelected: Boolean, onClick: () -> Unit)

// Incorrecto
fun ChannelCard(viewModel: TdtViewModel, channelIndex: Int)
```

### Recomposiciones
- Usar `remember` para valores derivados costosos dentro de un composable.
- Usar `derivedStateOf` cuando el valor depende de otro estado y cambia con menos frecuencia.
- Las lambdas en `items()` de `LazyList` deben ser estables — usar `key =` siempre.

### Modifier
Orden lógico estándar:
1. `fillMaxSize` / `fillMaxWidth` / `size`
2. `padding`
3. `clip` / `background`
4. `border`
5. `clickable` / gestos
6. Resto (alineación, semántica, etc.)

### Previews
- Usar `@PreviewLightDark` en todos los componentes reutilizables.
- La preview recibe datos estáticos, nunca un ViewModel.

### Animaciones
- `AnimatedVisibility(visible = flag)` — no anidar dentro de `if (flag)`.
- `AnimatedContent` para transiciones entre estados de contenido.

## Señales de alerta

- Un `@Composable` con más de ~150 líneas — extraer sub-composables.
- `LazyVerticalGrid` sin `key` en `items` — provoca recomposiciones innecesarias.
- `mutableStateOf` en el cuerpo de un composable sin `remember` — estado que se reinicia en cada recomposición.
