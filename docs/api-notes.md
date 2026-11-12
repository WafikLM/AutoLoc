# Atelier 5 - Notes sur l'API REST (Spring MVC, Postman, SpringDoc)

Package ajouté : `web.controller` (9 contrôleurs, `GlobalExceptionHandler`, `OpenApiConfig`).
Préfixe commun défini une seule fois : `server.servlet.context-path=/api` (port `8089`, déjà utilisé par le projet).

- API : `http://localhost:8089/api/...`
- Swagger UI : `http://localhost:8089/api/swagger-ui.html`
- Description OpenAPI : `http://localhost:8089/api/v3/api-docs`
- Collection Postman : `postman/AutoLoc.postman_collection.json` + environnement `postman/local.postman_environment.json` (`baseUrl = http://localhost:8089`)

## A. Endpoints

Les 404 viennent de `ResourceNotFoundException`, les 400 de `IllegalArgumentException` (traduites par `GlobalExceptionHandler`) ; le contrôleur ne fabrique aucun code d'erreur lui-même.

| Contrôleur | Verbe et route | Service appelé | Succès | Erreurs |
|---|---|---|---|---|
| ContratController | GET /api/contrats | findAll() | 200 | — |
| ContratController | GET /api/contrats/{id} | findById(id) | 200 | 404 |
| ContratController | POST /api/contrats | create(contrat) | 201 | 400 (id fourni, montant négatif) |
| ContratController | PUT /api/contrats/{id} | update(id, contrat) | 200 | 404, 400 (montant négatif) |
| ContratController | DELETE /api/contrats/{id} | deleteById(id) | 204 | 404 |
| PaiementController | GET /api/paiements | findAll() | 200 | — |
| PaiementController | GET /api/paiements/{id} | findById(id) | 200 | 404 |
| PaiementController | POST/PUT/DELETE /api/paiements | aucun (lecture seule) | — | 405 |
| AgenceController | GET /api/agences | findAll() | 200 | — |
| AgenceController | GET /api/agences/{id} | findById(id) | 200 | 404 |
| AgenceController | POST /api/agences | create(agence) | 201 | 400 (id fourni, nom vide) |
| AgenceController | PUT /api/agences/{id} | update(id, agence) | 200 | 404, 400 (nom vide) |
| AgenceController | DELETE /api/agences/{id} | deleteById(id) | 204 | 404, 409 (agence encore liée) |
| EmployeController | GET /api/employes | findAll() | 200 | — |
| EmployeController | GET /api/employes/{id} | findById(id) | 200 | 404 |
| EmployeController | POST /api/employes | create(employe) | 201 | 400 (id fourni, nom ou rôle absent) |
| EmployeController | PUT /api/employes/{id} | update(id, employe) | 200 | 404, 400 |
| EmployeController | DELETE /api/employes/{id} | deleteById(id) | 204 | 404 |
| VehiculeController | GET /api/vehicules | findAll() | 200 | — |
| VehiculeController | GET /api/vehicules/{id} | findById(id) | 200 | 404 |
| VehiculeController | POST /api/vehicules | create(vehicule) | 201 | 400 (id fourni, tarif négatif) |
| VehiculeController | PUT /api/vehicules/{id} | update(id, vehicule) | 200 | 404, 400 |
| VehiculeController | DELETE /api/vehicules/{id} | deleteById(id) | 204 | 404, 409 (véhicule encore réservé) |
| EquipementController | GET /api/equipements | findAll() | 200 | — |
| EquipementController | GET /api/equipements/{id} | findById(id) | 200 | 404 |
| EquipementController | POST /api/equipements | create(equipement) | 201 | 400 (id fourni, libellé vide) |
| EquipementController | PUT /api/equipements/{id} | update(id, equipement) | 200 | 404, 400 |
| EquipementController | DELETE /api/equipements/{id} | deleteById(id) | 204 | 404 |
| ClientController | GET /api/clients | findAll() | 200 | — |
| ClientController | GET /api/clients/{id} | findById(id) | 200 | 404 |
| ClientController | POST /api/clients | create(client) | 201 | 400 (id fourni, nom ou e-mail vide) |
| ClientController | PUT /api/clients/{id} | update(id, client) | 200 | 404, 400 |
| ClientController | DELETE /api/clients/{id} | deleteById(id) | 204 | 404, 409 (client avec réservations) |
| ReservationController | GET /api/reservations | findAll() | 200 | — |
| ReservationController | GET /api/reservations/{id} | findById(id) | 200 | 404 |
| ReservationController | POST /api/reservations | create(reservation) | 201 | 400 (id fourni, fin avant début) |
| ReservationController | PUT /api/reservations/{id} | update(id, reservation) | 200 | 404, 400 |
| ReservationController | DELETE /api/reservations/{id} | deleteById(id) | 204 | 404 |
| MaintenanceController | GET /api/maintenances | findAll() | 200 | — |
| MaintenanceController | GET /api/maintenances/{id} | findById(id) | 200 | 404 |
| MaintenanceController | POST /api/maintenances | create(maintenance) | 201 | 400 (id fourni, fin avant début) |
| MaintenanceController | PUT /api/maintenances/{id} | update(id, maintenance) | 200 | 404, 400 |
| MaintenanceController | DELETE /api/maintenances/{id} | deleteById(id) | 204 | 404 |

Remarque : le 409 est un ajout au `GlobalExceptionHandler` (`DataIntegrityViolationException`). Sans lui, supprimer une agence qui a encore des véhicules donnait un 500 (violation de clé étrangère).

Corps imbriqués (adaptés aux noms de mon modèle) : `{ "agence": {"idAgence": 1} }`, `{ "client": {"idClient": 1}, "vehicule": {"idVehicule": 1} }`, `{ "vehicule": {"idVehicule": 1} }`.

## B. Annotations Jackson

Toutes les entités portent aussi `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` : une association `LAZY` (ex. `Contrat.reservation`, `Employe.agence`) est un proxy Hibernate que Jackson ne sait pas sérialiser (`No serializer found ... ByteBuddyInterceptor`).

| Association | Annotation retenue | Justification |
|---|---|---|
| Contrat.paiements / Paiement.contrat | `@JsonManagedReference("contrat-paiements")` côté liste, `@JsonBackReference("contrat-paiements")` côté parent | Composition (`cascade = ALL`, `orphanRemoval`) : les paiements sont créés dans le JSON du contrat. À la lecture, Jackson renseigne `paiement.contrat`, donc la clé étrangère n'est pas nulle en base. |
| Reservation.contrat (côté inverse du OneToOne) | `@JsonIgnore` | Sinon Contrat → Reservation → Contrat → ... (boucle). Le contrat désigne sa réservation, pas l'inverse. |
| Client.reservations | `@JsonIgnore` | Côté inverse ; la réservation est créée seule et désigne son client par `{"idClient": 1}`. Pas de `@JsonBackReference` sur `Reservation.client` (sinon le POST créerait une réservation sans client). |
| Agence.vehicules, Agence.employes | `@JsonIgnore` | Côté inverse ; véhicules et employés sont créés seuls avec `{"agence": {"idAgence": 1}}`. |
| Vehicule.maintenances | `@JsonIgnore` | Côté inverse ; une maintenance est créée seule avec son véhicule. |
| Equipement.vehicules | `@JsonIgnore` | Côté inverse du ManyToMany ; `Vehicule.equipements` (propriétaire) reste sérialisé. |

Différence `@JsonBackReference` / `@JsonIgnore` :
- `@JsonBackReference` ne fonctionne qu'avec un `@JsonManagedReference`. La propriété n'est pas écrite, mais Jackson la **remplit automatiquement** à la lecture avec l'objet parent. C'est fait pour une composition où l'enfant arrive dans le JSON du parent.
- `@JsonIgnore` ignore la propriété **en écriture et en lecture**. On l'utilise sur la liste du côté inverse quand l'enfant est créé seul et désigne son parent par identifiant. L'attribut propriétaire (`Vehicule.agence`...) reste lisible et écrit.

## C. Postman et Swagger

| Outil | Ce qu'il apporte | Exemple concret dans l'atelier |
|---|---|---|
| Postman | Tester : requêtes enregistrées et rejouables, variables d'environnement (`{{baseUrl}}`, `{{contratId}}`), assertions automatiques (onglet Scripts > After response), scénarios enchaînés. | Scénario de cascade : « Créer un contrat avec ses paiements » mémorise `paiementId`, « Supprimer le contrat avec ses paiements » renvoie 204, puis GET `/api/paiements/{{paiementId}}` renvoie 404 (cascade REMOVE vérifiée par un test). |
| Swagger UI | Documenter et explorer : description OpenAPI générée depuis le code (toujours synchronisée), groupes `@Tag`, résumés, codes documentés, schémas déduits des entités, bouton « Try it out ». | Le schéma `Contrat` affiche `paiements` mais pas `Paiement.contrat` (masqué par `@JsonBackReference`) ; le groupe « Paiements » ne montre que deux GET. |

Les deux sont complémentaires : Postman vérifie le comportement, Swagger décrit le contrat de l'API.

## D. Anomalies SonarQube for IDE

À compléter avec le résultat réel de l'analyse (clic droit sur `web.controller` > Analyze with SonarQube for IDE).

| Règle | Explication | Correction |
|---|---|---|
| java:S6813 - injection par attribut (`@Autowired` sur un champ) | Dépendance cachée, champ non `final`, `null` hors conteneur. | Tous les contrôleurs utilisent un champ `private final I...Service` + `@RequiredArgsConstructor` (injection par constructeur). |
| java:S1128 - import inutile | Les extraits de l'énoncé importent des annotations non utilisées par tous les contrôleurs (ex. `@ResponseStatus`/`HttpStatus` dans `PaiementController`). | Imports supprimés : `PaiementController` n'importe que ce qu'il utilise (GET uniquement). |
| java:S4684 - entité persistante utilisée comme argument d'une méthode `@RequestMapping` | `@RequestBody Contrat` expose l'entité JPA : un client peut envoyer des champs internes (identifiant, associations). | Conservé volontairement et provisoirement (énoncé) ; limité par les services (`create` refuse un identifiant, `update` ne recopie que les attributs simples). Corrigé au Chapitre 10 par des DTO. |

## E. Questions

1. **Composant qui reçoit les requêtes ?** Le `DispatcherServlet`. Il interroge les `HandlerMapping` (construits au démarrage à partir des `@RequestMapping`/`@GetMapping`...) avec le verbe HTTP et le chemin, trouve la méthode du contrôleur, la fait appeler par un `HandlerAdapter` (qui lit `@PathVariable`, `@RequestBody`...), puis un `HttpMessageConverter` (Jackson) écrit la valeur retournée en JSON.
2. **Pourquoi `IContratService` ?** Organisation en couches : le contrôleur ne parle qu'au service (les règles métier et la gestion des 404/400 y sont), jamais au repository. Dépendre de l'interface et non de `ContratServiceImpl` réduit le couplage : Spring injecte n'importe quelle implémentation (autre implémentation, mock pour un test) sans modifier le contrôleur.
3. **201 / 204 / 404 ?** 201 Created signale qu'une nouvelle ressource a été créée (le corps contient son identifiant). 204 No Content signale un succès sans corps à renvoyer (la ressource n'existe plus). Une ressource absente est une erreur du client (mauvais identifiant) : 404 ; 500 voudrait dire un bug du serveur. Le `GlobalExceptionHandler` traduit `ResourceNotFoundException` en 404.
4. **Pourquoi seulement des GET pour Paiement ?** `IPaiementService` est en lecture seule : un paiement fait partie de la composition Contrat → Paiement ; il est créé, modifié et supprimé à travers son contrat (cascade ALL, orphanRemoval). Un `POST /api/paiements` reçoit **405 Method Not Allowed** (la route existe mais pas pour ce verbe).
5. **Risques de l'exposition des entités ?** Couplage de l'API au schéma de base (un renommage casse les clients), champs internes visibles ou modifiables (identifiants, associations), boucles de sérialisation et proxys LAZY, requêtes SQL déclenchées pendant la sérialisation, pas de validation adaptée à chaque cas d'usage. `Contrat.paiements` utilise `@JsonManagedReference` car c'est une composition : les paiements arrivent dans le JSON du contrat et doivent recevoir leur parent à la lecture. `Agence.vehicules` utilise `@JsonIgnore` car un véhicule est créé seul et désigne son agence par identifiant : la liste côté agence ne sert ni en lecture ni en écriture.

## F. Services d'affectation du véhicule (travail demandé en séance)

| Méthode de `IVehiculeService` | Route | Succès | Erreurs |
|---|---|---|---|
| `addAndAssignAgence(Vehicule vehicule, Long idAgence)` | POST /api/vehicules/agence/{idAgence} | 201 | 400 (id fourni, tarif négatif, libellé vide), 404 (agence ou équipement introuvable) |
| `assignAgence(Long idVehicule, Long idAgence)` | PUT /api/vehicules/{idVehicule}/agence/{idAgence} | 200 | 404 |
| `assignEquipement(Long idVehicule, Long idEquipement)` | PUT /api/vehicules/{idVehicule}/equipements/{idEquipement} | 200 | 400 (déjà affecté), 404 |
| `unassignEquipement(Long idVehicule, Long idEquipement)` | DELETE /api/vehicules/{idVehicule}/equipements/{idEquipement} | 200 | 400 (non affecté), 404 |

- `addAndAssignAgence` : le véhicule arrive en JSON avec sa liste `equipements` ; l'agence est chargée par son identifiant puis affectée (`setAgence`). Les équipements sans identifiant sont créés **en cascade** grâce à `cascade = CascadeType.PERSIST` ajouté sur `Vehicule.equipements` (pas de `REMOVE` : un équipement est partagé entre véhicules). Un équipement avec identifiant doit exister et est repris depuis la base.
- `assignAgence` / `assignEquipement` / `unassignEquipement` : les deux objets existent déjà ; on modifie seulement le lien (`setAgence`, ajout ou retrait dans la collection `equipements`, donc dans la table `vehicule_equipement`). `unassignEquipement` ne supprime pas l'équipement de la base.
- `VehiculeServiceImpl` reçoit en plus `IAgenceRepository` et `IEquipementRepository` (injection par constructeur) ; les méthodes sont `@Transactional`.
- Tests : dossier Postman « Véhicules - affectations (agence, équipements) ».

## G. Service `Contrat addWithPayments(Contrat c)`

| Méthode de `IContratService` | Route | Succès | Erreurs |
|---|---|---|---|
| `addWithPayments(Contrat contrat)` | POST /api/contrats/avec-paiements | 201 | 400 (id fourni, aucun paiement, montant de paiement ≤ 0, somme des paiements > montant total) |

- Le contrat arrive en JSON avec sa liste `paiements`. Pour chaque paiement, le service fait `paiement.setContrat(contrat)` (côté propriétaire de la relation : sinon la clé étrangère serait nulle), puis un **seul** `contratRepository.save(contrat)` : les paiements sont insérés par la cascade (`CascadeType.ALL` sur `Contrat.paiements`). Méthode `@Transactional`.
- Supprimer le contrat supprime aussi ses paiements (cascade REMOVE / orphanRemoval).
- Tests : dossier Postman « Contrats - addWithPayments ».
