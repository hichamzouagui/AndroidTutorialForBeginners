# Gestion énergie — application Android

Application Android (Java + SQLite) de **suivi et de maîtrise de la consommation
électrique d'un foyer**. Elle croise deux sources d'information :

1. une **estimation** calculée à partir des appareils déclarés (puissance × durée d'usage) ;
2. la **mesure réelle** issue des relevés de compteur saisis par l'utilisateur.

L'écart entre les deux indique si l'inventaire des appareils est réaliste.

## Fonctionnalités

| Écran | Contenu |
| --- | --- |
| **Tableau de bord** | kWh, facture et CO2 du mois, suivi du budget, histogramme des périodes mesurées, répartition par appareil, piste d'économie chiffrée |
| **Appareils** | Inventaire (puissance, heures/jour, jours/semaine, quantité, catégorie), activation/désactivation, suppression avec annulation |
| **Relevés** | Historique des index de compteur, consommation et coût de chaque période, moyenne journalière, facture projetée |
| **Réglages** | Prix du kWh, abonnement mensuel, facteur d'émission CO2, budget mensuel |

Détails de calcul :

- un mois vaut 30,4375 jours (365,25 / 12), pour que les mois courts et longs soient comparables ;
- la facture ajoute l'abonnement à l'énergie consommée ;
- un budget est signalé « proche » dès 90 % et « dépassé » au-delà de 100 % ;
- un relevé incohérent (index qui recule, date antérieure) est ignoré dans les calculs
  au lieu de faire échouer l'écran ;
- les valeurs par défaut (0,2516 €/kWh, 13,09 € d'abonnement, 56 g CO2/kWh) sont
  indicatives : il faut reprendre les chiffres de sa propre facture.

## Architecture

```
app/src/main/java/com/hichamzouagui/energymanager/
├── core/     logique métier pure (aucune dépendance Android, testée en JVM)
│             Appliance, Tariff, EnergyCalculator, ConsumptionSummary,
│             ApplianceShare, MeterSnapshot, MeterPeriod, BudgetStatus, SavingsAdvice
├── data/     persistance SQLite : Device, MeterReading, EnergyContract,
│             EnergyDatabaseHelper, EnergyRepository
├── prefs/    UserSettings (tarif et budget dans les SharedPreferences)
├── ui/       MainActivity + 4 fragments, adaptateurs, boîtes de dialogue,
│             widget/BarChartView (histogramme dessiné à la main)
└── util/     Formatters (affichage fr-FR), Numbers (lecture des saisies)
```

Le choix structurant : **tout le calcul vit dans `core/`, sans import Android**.
Cette couche est donc couverte par de vrais tests unitaires JVM, sans émulateur
ni Robolectric.

## Compiler et lancer

Aucune dépendance externe en dehors d'AndroidX et de Material Components.

```bash
# Ouvrir le dossier EnergyManagerApp dans Android Studio (Flamingo ou plus récent),
# ou en ligne de commande, avec un SDK Android 34 installé :
gradle wrapper --gradle-version 8.2   # génère gradlew (non versionné ici)
./gradlew assembleDebug
./gradlew installDebug
```

- AGP 8.2.2, Gradle 8.2, Java 17
- `compileSdk` / `targetSdk` 34, `minSdk` 24

À la première ouverture, la base est préremplie avec cinq appareils courants
(réfrigérateur, lave-linge, box, éclairage, chauffe-eau) pour que le tableau de
bord soit immédiatement parlant ; ils sont modifiables et supprimables.

## Tests

```bash
./gradlew test        # tests unitaires JVM
```

31 tests couvrent les conversions puissance/énergie/euros/CO2, l'agrégation et le
tri de la répartition, les périodes de compteur (y compris relevés incohérents),
les seuils de budget, les économies simulées, les saisies invalides et
l'aller-retour entre préremplissage et relecture des champs numériques.

## Limites connues

- Saisie manuelle des relevés : aucune liaison avec un compteur communicant (Linky, API fournisseur).
- Électricité uniquement : ni gaz, ni eau, ni production photovoltaïque.
- Mono-foyer, données locales, sans synchronisation ni export.
- Les requêtes SQLite sont exécutées sur le thread principal : volumétrie faible
  par nature (quelques dizaines de lignes), mais à revoir en cas d'import massif.
