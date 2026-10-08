# Atelier 3 - Notes sur la couche Repository

## 1. Choix des interfaces

Toutes les interfaces sont dans le package `repository`, avec le préfixe `I`.
J'ai pris `JpaRepository` partout parce que c'est l'interface la plus complète :
on a le CRUD, les `List` à la place des `Iterable`, le tri, la pagination et `flush`.

| Interface | Étend | Justification |
|---|---|---|
| IContratRepository | `JpaRepository<Contrat, Long>` | CRUD complet, `findAll` renvoie une `List`, `saveAndFlush` disponible. |
| IPaiementRepository | `JpaRepository<Paiement, Long>` | Sert surtout à lire les paiements. Pour créer ou retirer un paiement on passe par le Contrat (composition). |
| IAgenceRepository | `JpaRepository<Agence, Long>` | CRUD simple sur les agences, `findAll` en `List` pour les afficher. |
| IEmployeRepository | `JpaRepository<Employe, Long>` | CRUD des employés, le tri (par nom) sera utile plus tard. |
| IVehiculeRepository | `JpaRepository<Vehicule, Long>` | Il peut y avoir beaucoup de véhicules, donc la pagination et le tri sont utiles. |
| IEquipementRepository | `JpaRepository<Equipement, Long>` | Petite table de référence, CRUD avec des `List`. |
| IClientRepository | `JpaRepository<Client, Long>` | CRUD des clients + pagination quand la liste devient longue. |
| IReservationRepository | `JpaRepository<Reservation, Long>` | CRUD des réservations, tri par date possible avec `Sort`. |
| IMaintenanceRepository | `JpaRepository<Maintenance, Long>` | CRUD de l'historique d'entretien d'un véhicule. |

Remarques :

- `CrudRepository` aurait suffi pour le CRUD mais `findAll()` renvoie un `Iterable`, moins pratique qu'une `List`.
- `PagingAndSortingRepository` tout seul ne contient plus le CRUD depuis Spring Data 3.
- Je n'utilise pas `deleteAllInBatch` sur les contrats : la suppression en lot ne passe pas par le contexte de persistance, donc la cascade et l'orphanRemoval ne sont pas appliqués (les paiements resteraient, ou la clé étrangère bloquerait la requête).
- Pas besoin de `@Repository` : Spring Data crée le proxy au démarrage.

## 2. Ce que j'ai vu dans les logs

Au démarrage : `Found 9 JPA repository interfaces`, et aucune nouvelle instruction DDL.

Avec le petit programme de test dans la classe principale (CRUD sur un contrat avec 2 paiements) :

| Appel | SQL dans les logs | Explication |
|---|---|---|
| `save(contrat)` (id null) | 1 `insert into contrat` puis 2 `insert into paiement` | Entité nouvelle = persist. Les paiements sont enregistrés avec le contrat grâce à la cascade. L'id est rempli dans l'objet retourné. |
| `findById(id)` | 1 `select ... from contrat where id_contrat=?` | Renvoie un `Optional<Contrat>`. Les paiements ne sont pas chargés (LAZY). |
| `count()` | `select count(*) from contrat` | |
| `save(contrat)` (id renseigné, un paiement retiré de la liste) | `select` du contrat avec ses paiements, `update contrat`, `delete from paiement` | Entité existante = merge : select puis update. Le paiement retiré de la liste est supprimé en base (orphanRemoval). |
| `deleteById(id)` | `select` contrat, `select` paiements, `delete from paiement`, `delete from contrat` | Le paiement restant est supprimé avant le contrat (cascade REMOVE). |
| `existsById(id)` | `select count(*) ... where id_contrat=?` | Renvoie `false` après la suppression. |

## 3. Anomalies SonarQube for IDE

Analyse de tout le projet (Analyze All Project Files) : 11 problèmes, tous dans `WafikLahbib4cce10Application.java`.
Rien de signalé dans les entités et les repositories.

| Anomalie SonarQube for IDE | Règle / explication | Correction apportée |
|---|---|---|
| Remove this unused import 'java.util.List' | java:S1128 - un import non utilisé alourdit le code pour rien. | Import supprimé. |
| Replace this use of System.out by a logger (5 fois) | java:S106 - `System.out` ne permet pas de gérer les niveaux de log ni le format. | Ajout de `@Slf4j` (Lombok) et remplacement par `log.info(...)` avec des `{}`. |
| Remove this field injection and use constructor injection instead | java:S6813 - avec `@Autowired` sur l'attribut la dépendance est cachée et la classe est plus difficile à tester. | Attribut `private final` + constructeur, `@Autowired` supprimé. |
| Call "Optional#isPresent()" before accessing the value | java:S3655 - `findById(id).get()` lève une exception si l'Optional est vide. | Je garde l'`Optional` et je teste `isPresent()` avant le `get()`. |
| Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method (3 fois) | java:S8688 - `LocalDate.now()` dépend du fuseau de la machine. | `LocalDate.now(ZoneId.of("Africa/Tunis"))`, calculé une seule fois. |

Après les corrections j'ai relancé l'analyse : `No issues to display`.
