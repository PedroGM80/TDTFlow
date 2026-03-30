# Skill: android-clean-solid

## Propósito
Aplicar Clean Architecture y principios SOLID en cada cambio de código.

## Capas y responsabilidades

| Capa | Módulo | Qué contiene | Qué NO contiene |
|------|--------|--------------|-----------------|
| Domain | `domain/` | Modelos, interfaces de repositorio, UseCases | Nada de Android, nada de UI |
| Data | `data/` | Implementaciones de repositorio, red, persistencia | Lógica de negocio |
| UI | `app/` | ViewModels, Composables, DI | Acceso directo a red o BD |

## Reglas estrictas

1. **ViewModel → UseCase → Repository** — nunca saltarse un escalón.
2. **Inyección por constructor siempre** — sin `object` singleton accedido directamente, sin `getInstance()` en código de producción.
3. **Un UseCase = una acción** — `GetChannelsUseCase`, `AddFavoriteUseCase`, nunca `FavoritesUseCase` que haga todo.
4. **El dominio es puro JVM** — ninguna clase del módulo `domain` puede importar `android.*`.

## Checklist antes de escribir código (SOLID)

- [ ] **S** — ¿Esta clase tiene una única razón para cambiar?
- [ ] **O** — ¿Puedo extender el comportamiento sin modificar la clase?
- [ ] **L** — ¿Las subclases/implementaciones son intercambiables?
- [ ] **I** — ¿La interfaz expone solo lo que el cliente necesita?
- [ ] **D** — ¿Dependo de abstracciones, no de implementaciones concretas?

## Señales de alerta

- Un ViewModel que importa `Retrofit`, `Room` o `SharedPreferences` directamente.
- Un repositorio que contiene `if (isUserLoggedIn)` — eso es lógica de dominio.
- Un UseCase que llama a otro UseCase — extraer servicio de dominio.
