# Skill: android-kiss-expert

## Propósito
Mantener el código simple, legible y mantenible (Keep It Simple, Stupid).

## Reglas

1. **Si funciona en 5 líneas, no lo hagas en 15.** La abstracción solo se justifica cuando hay al menos 3 usos reales, no hipotéticos.
2. **Nombres que se explican solos** — si necesitas un comentario para explicar una variable, el nombre es malo.
3. **Funciones pequeñas con un único nivel de abstracción** — una función no debe mezclar lógica de negocio con formato de cadenas.
4. **No anticipar requisitos futuros** — solo implementar lo que existe hoy. YAGNI (You Ain't Gonna Need It).
5. **Preferir legibilidad sobre cleverness** — `filter { it.isActive }` es mejor que un one-liner con 4 operadores encadenados.

## Anti-patrones a evitar

| Anti-patrón | Alternativa simple |
|-------------|-------------------|
| `sealed class Result<T>` para un flujo que nunca falla | Devolver el tipo directamente |
| Wrapper genérico `Resource<T>` para todo | `StateFlow` con estado concreto |
| Factory + Builder para una clase con 2 campos | Constructor con parámetros nombrados |
| `BaseViewModel` con 10 métodos abstractos | ViewModel concreto con lo que necesita |
| Interface con un solo implementador sin tests | Clase directa |

## Test de simplicidad

Antes de añadir una abstracción, responde:
- ¿Tengo HOY dos o más lugares que la necesiten?
- ¿Será más difícil de leer para alguien nuevo?
- Si la respuesta a ambas es "no" / "sí", elimínala.
