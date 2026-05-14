# Lab 16 — Chronomètre Service Android

## Présentation

Application Android utilisant un Foreground Service et un Bound Service pour implémenter un chronomètre persistant avec notification en temps réel.

---

## Ce qui a été réalisé

### Fonctionnalités
| Fonctionnalité | Description |
|---|---|
| Foreground Service | Service persistant avec notification live |
| Bound Service | Communication Activity ↔ Service |
| Notification | Mise à jour du temps en temps réel |
| START_STICKY | Redémarrage automatique si tué |
| Handler | Mise à jour UI toutes les secondes |

### Personnalisation HC
- Classe : `HCChronometreService`
- Binder : `HCLocalBinder`
- Channel : `hc_chrono_channel`
- Action stop : `HC_STOP`
- Palette : bleu foncé / orange

---

## Démonstration

https://github.com/user-attachments/assets/493c6e2b-e1e3-47a0-833a-31896a87488b


---

## Technologies utilisées
- Android Studio
- Java
- Foreground Service
- Bound Service
- NotificationCompat
- ScheduledExecutorService
- Handler / Runnable
