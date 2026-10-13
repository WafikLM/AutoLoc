# Atelier 4 - Notes sur la couche Service

Packages ajoutés : `exception` (ResourceNotFoundException) et `service` (9 interfaces `I...Service` + 9 classes `...ServiceImpl`).
Au redémarrage : aucune erreur et toujours `Found 9 JPA repository interfaces`.

## A. Modes d'injection comparés

Étape 2 : dans `ContratServiceImpl` j'ai remplacé temporairement `@RequiredArgsConstructor` + champ `final` par `@Autowired` sur l'attribut, puis j'ai restauré.

| Critère | Constructeur (`@RequiredArgsConstructor` + `final`) | Attribut (`@Autowired`) |
|---|---|---|
| Champ `final` possible ? | Oui | Non (le champ est rempli après la construction) |
| Dépendance visible dans la signature de la classe ? | Oui, elle est dans le constructeur | Non, elle est cachée dans un attribut privé |
| Utilisable avec `new` hors conteneur sans dépendance `null` ? | Oui, on est obligé de passer le repository | Non, `new ContratServiceImpl()` donne un repository `null` (NullPointerException à l'appel) |
| Signalé par SonarQube for IDE ? | Non | Oui, règle java:S6813 |

## B. Services

| Service | Dépendances injectées | Mode | Justification |
|---|---|---|---|
| ContratServiceImpl | IContratRepository | Constructeur | Champ `final` + `@RequiredArgsConstructor`. Refuse un id non null à la création et un montant total négatif. La suppression supprime les paiements par cascade (mapping `CascadeType.ALL` + `orphanRemoval`). |
| PaiementServiceImpl | IPaiementRepository | Constructeur | Lecture seule (`findById`, `findAll`) : un paiement est créé, modifié et supprimé en passant par son contrat. |
| AgenceServiceImpl | IAgenceRepository | Constructeur | Refuse la création ou la modification si le nom est vide. |
| EmployeServiceImpl | IEmployeRepository | Constructeur | Refuse un employé sans nom ou sans rôle. |
| VehiculeServiceImpl | IVehiculeRepository | Constructeur | Refuse un tarif journalier négatif. |
| EquipementServiceImpl | IEquipementRepository | Constructeur | Refuse un équipement sans libellé (pas d'attribut quantité dans mon modèle). |
| ClientServiceImpl | IClientRepository | Constructeur | Refuse un client sans nom ou sans e-mail. |
| ReservationServiceImpl | IReservationRepository | Constructeur | Refuse une date de fin avant la date de début. |
| MaintenanceServiceImpl | IMaintenanceRepository | Constructeur | Refuse une date de fin avant la date de début (pas d'attribut coût dans mon modèle). |

Pour tous les services : `@Service`, un seul champ `final` de type interface `I...Repository`, aucun attribut modifiable (sans état), aucun `new` de repository.
`create` refuse un id non null, `findById` et `deleteById` lèvent `ResourceNotFoundException` si l'id n'existe pas, `update` charge l'existant, recopie les attributs simples (sans toucher à l'id ni aux collections) puis `save`.
Les règles lèvent une `IllegalArgumentException` avec un message clair.

## C. Messages d'erreur

**Message 1** - `NoSuchBeanDefinitionException` (aucun bean de type `IContratService`), bean concerné : `ContratController`.
- Cause 1 : l'annotation `@Service` a été oubliée sur `ContratServiceImpl`, donc le scan ne crée pas de bean.
- Cause 2 : `ContratServiceImpl` est placé dans un package en dehors du package racine (`tn.esprit.wafik_lahbib_4cce10`), donc il n'est pas scanné.
- Correction : remettre `@Service` et déplacer la classe dans `tn.esprit.wafik_lahbib_4cce10.service`.

**Message 2** - `NoUniqueBeanDefinitionException` : deux beans du même type (`emailNotificateur` et `smsNotificateur`) pour un seul paramètre du constructeur de `NotificationController`.
- Correction 1 : mettre `@Primary` **sur la classe** du bean à utiliser par défaut (par exemple `EmailNotificateur`).
- Correction 2 : mettre `@Qualifier("smsNotificateur")` **sur le paramètre du constructeur** de `NotificationController` pour choisir explicitement le bean.

**Message 3** - dépendance circulaire (`BeanCurrentlyInCreationException`) entre `clientServiceImpl` et `reservationServiceImpl`.
- Avec l'injection par constructeur, pour créer `ClientServiceImpl` il faut déjà avoir `ReservationServiceImpl`, et pour créer `ReservationServiceImpl` il faut déjà avoir `ClientServiceImpl`. Aucun des deux ne peut être construit en premier, et comme les champs sont `final` Spring ne peut pas injecter plus tard : le cycle est impossible à contourner.
- Piste : revoir la conception. Les deux services ne doivent pas se référencer : chacun dépend des repositories (par exemple `ReservationServiceImpl` utilise `IClientRepository` au lieu de `IClientService`), ou on extrait la partie commune dans un troisième service utilisé par les deux.

## D. Anomalies SonarQube for IDE

Analyse de tout le projet (Analyze All Project Files) après avoir créé les services : `No issues to display`.
Les anomalies ci-dessous viennent des expériences des étapes 2 et 4 dans `ContratServiceImpl` :

| Anomalie | Règle / explication | Correction apportée |
|---|---|---|
| Remove this field injection and use constructor injection instead (étape 2) | java:S6813 - avec `@Autowired` sur l'attribut la dépendance est cachée, le champ ne peut pas être `final` et la classe est difficile à tester. | Retour à `@RequiredArgsConstructor` + champ `final`. |
| Remove this unused import 'lombok.RequiredArgsConstructor' (étape 2) | java:S1128 - après avoir enlevé `@RequiredArgsConstructor`, l'import n'était plus utilisé. | Annotation restaurée, l'import est de nouveau utilisé. |
| `RuntimeException` à la place de `ResourceNotFoundException` (étape 4) | java:S112 - lever une exception générique ne dit pas ce qui s'est passé et on ne peut pas la traiter à part. Remarque : avec le profil de règles par défaut de SonarQube for IDE, cette règle n'a pas été signalée dans mon analyse. | Remis `ResourceNotFoundException`, l'exception métier du package `exception`. |

Après avoir restauré le code, nouvelle analyse : `No issues to display`.

## E. Questions de compréhension

1. **Où est le `new` de `ContratServiceImpl` et qui l'exécute ?**
   Il n'est pas dans notre code. C'est le conteneur Spring qui fait l'instanciation au démarrage : le scan trouve `@Service`, puis Spring appelle le constructeur généré par Lombok en lui passant le proxy de `IContratRepository`.

2. **Pourquoi un contrôleur dépendra-t-il de `IContratService` et non de `ContratServiceImpl` ?**
   Pour dépendre d'une abstraction et pas d'une implémentation : on peut changer d'implémentation (ou en donner une de remplacement pour les tests) sans modifier le contrôleur. Couplage faible.

3. **Pourquoi un service singleton doit-il rester sans état ? Exemple de bug.**
   Il n'y a qu'une seule instance partagée par tous les threads (toutes les requêtes). Exemple : un attribut `private Contrat contratCourant;` dans le service. Deux requêtes en même temps écrivent dedans, et la requête A peut modifier ou renvoyer le contrat de la requête B.

4. **Pourquoi `update` charge-t-il l'entité existante avant `save` ?**
   Si on passe directement l'objet reçu à `save`, c'est un `merge` qui écrase toutes les colonnes (les attributs non envoyés deviennent `null`), et avec `orphanRemoval` une liste de paiements vide supprimerait tous les paiements du contrat. En chargeant l'existant, on vérifie aussi qu'il existe (`ResourceNotFoundException`) et on ne recopie que les attributs modifiables.

5. **`@Component` / `@Bean`, `@Primary` / `@Qualifier`, et `IPaiementService` ?**
   - `@Component` se met sur une classe : le scan la détecte et crée le bean. `@Bean` se met sur une méthode d'une classe `@Configuration` : on crée le bean nous-mêmes, utile pour une classe qu'on ne peut pas annoter (bibliothèque, JDK) ou une construction particulière.
   - `@Primary` se met sur la classe : c'est le bean choisi par défaut quand il y en a plusieurs du même type. `@Qualifier("nom")` se met au point d'injection : on choisit explicitement le bean par son nom.
   - `IPaiementService` est en lecture seule car `Paiement` est une composition de `Contrat` : un paiement n'existe que rattaché à son contrat, donc on le crée, le modifie ou le supprime en passant par le contrat (cascade + orphanRemoval).
