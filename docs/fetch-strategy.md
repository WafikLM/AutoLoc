# Stratégie de fetch et de cascade — AutoLoc

Atelier 2 — Associations JPA : Cascade et Fetch

## Règle générale

- **Fetch** : toutes les associations sont en `LAZY`. Les `@ManyToOne` et `@OneToOne`, qui sont `EAGER` par défaut, sont forcés en `LAZY` pour ne pas charger toute la chaîne d'objets liés à chaque lecture. Les collections (`@OneToMany`, `@ManyToMany`) gardent leur `LAZY` par défaut.
- **Cascade** : une cascade n'est posée que si l'enfant ne peut pas vivre sans son parent (composition). Dans tous les autres cas, aucune cascade de suppression.
- **Côté propriétaire** : c'est toujours le côté qui porte la clé étrangère (`@ManyToOne`, ou `@OneToOne` avec `@JoinColumn`). Le côté inverse utilise `mappedBy` et ne crée ni colonne ni table.

## Choix par association

| Association | Côté propriétaire (clé étrangère) | Fetch | Cascade | Justification |
|---|---|---|---|---|
| Contrat → Paiement | Paiement (`contrat_id_contrat`) | LAZY | ALL + orphanRemoval | Un paiement n'existe que rattaché à son contrat ; supprimer le contrat supprime ses paiements, et retirer un paiement de la liste le supprime en base. Les paiements ne sont chargés qu'à l'appel de `getPaiements()`. |
| Agence → Vehicule | Vehicule (`id_agence`) | LAZY | Aucune | Un véhicule survit à son agence (il peut être réaffecté) : supprimer une agence ne doit jamais supprimer sa flotte. Une agence peut avoir beaucoup de véhicules, on ne les charge que si besoin. |
| Agence → Employe | Employe (`id_agence`) | LAZY | Aucune | Un employé a son propre cycle de vie (embauche, mutation) et se gère par son propre repository. Lire une agence ne doit pas charger tout son personnel. |
| Vehicule ↔ Equipement | Vehicule (table `vehicule_equipement`) | LAZY | Aucune | Les équipements sont partagés entre plusieurs véhicules : supprimer un véhicule ne doit pas supprimer un équipement utilisé ailleurs. Un `Set` évite les doublons dans la table de jointure. |
| Client → Reservation | Reservation (`id_client`) | LAZY | PERSIST | Enregistrer un client enregistre aussi ses nouvelles réservations. Pas de REMOVE : l'historique des réservations ne doit pas disparaître avec le client. |
| Reservation → Vehicule | Reservation (`id_vehicule`) | LAZY | Aucune | Le véhicule existe indépendamment des réservations et est partagé par plusieurs d'entre elles. Association unidirectionnelle : la réservation connaît son véhicule, pas l'inverse. |
| Reservation ↔ Contrat | Contrat (`id_reservation`) | LAZY | ALL (côté Reservation) | Le contrat est issu d'une réservation et n'a pas de sens sans elle : il est enregistré, mis à jour et supprimé avec sa réservation. |
| Vehicule → Maintenance | Maintenance (`id_vehicule`) | LAZY | PERSIST | Enregistrer un véhicule enregistre ses nouvelles maintenances. Pas de REMOVE : l'historique d'entretien est conservé. |

## Remarques

- **Nom des colonnes** : pour Contrat → Paiement, aucun `@JoinColumn` n'est précisé (étapes guidées) ; Hibernate génère `contrat_id_contrat`. Pour les autres associations, `@JoinColumn` fixe les noms demandés dans l'énoncé (`id_agence`, `id_client`, `id_vehicule`, `id_reservation`).
- **LAZY vs EAGER** (étape 5) : avec `EAGER`, les paiements seraient chargés à chaque lecture d'un contrat, même sans être utilisés. Avec `LAZY`, la lecture d'un contrat reste légère ; un accès à la collection hors transaction lève une `LazyInitializationException`, à traiter par une requête dédiée (Atelier 7).
- **cascade REMOVE vs orphanRemoval** : REMOVE supprime les paiements quand on supprime le contrat ; `orphanRemoval` supprime un paiement dès qu'il est retiré de la liste du contrat, même si le contrat reste. Les deux sont complémentaires.
- **Lombok** : `@Getter` / `@Setter` / `@NoArgsConstructor` / `@AllArgsConstructor` uniquement. Pas de `@Data` : son `toString` / `hashCode` parcourrait les deux côtés des associations bidirectionnelles (`StackOverflowError`).
- **`@OneToOne` côté inverse** : sur Reservation (côté `mappedBy`), Hibernate peut tout de même charger le contrat pour savoir s'il existe ; le `LAZY` est pleinement effectif sur le côté propriétaire (Contrat).
