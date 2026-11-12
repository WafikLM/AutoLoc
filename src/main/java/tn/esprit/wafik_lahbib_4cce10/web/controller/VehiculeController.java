package tn.esprit.wafik_lahbib_4cce10.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.wafik_lahbib_4cce10.domain.Vehicule;
import tn.esprit.wafik_lahbib_4cce10.service.IVehiculeService;

@Tag(name = "Véhicules", description = "Gestion du parc de véhicules")
@RestController
@RequestMapping("/vehicules")
@RequiredArgsConstructor
public class VehiculeController {

    // dépendance vers l'interface du service (jamais le repository ni l'implémentation)
    private final IVehiculeService vehiculeService;

    @Operation(summary = "Lister les véhicules")
    @ApiResponse(responseCode = "200", description = "Liste renvoyée (éventuellement vide)")
    @GetMapping
    public List<Vehicule> findAll() {
        return vehiculeService.findAll();
    }

    @Operation(summary = "Consulter un véhicule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Véhicule trouvé"),
            @ApiResponse(responseCode = "404", description = "Véhicule introuvable")
    })
    @GetMapping("/{id}")
    public Vehicule findById(@Parameter(description = "Identifiant du véhicule") @PathVariable Long id) {
        return vehiculeService.findById(id);
    }

    @Operation(summary = "Créer un véhicule", description = "Le corps ne doit pas contenir d'identifiant.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Véhicule créé"),
            @ApiResponse(responseCode = "400", description = "Identifiant fourni ou règle non respectée (tarif journalier négatif)")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vehicule create(@RequestBody Vehicule vehicule) {
        return vehiculeService.create(vehicule);
    }

    @Operation(summary = "Modifier un véhicule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Véhicule modifié"),
            @ApiResponse(responseCode = "400", description = "Règle non respectée (tarif journalier négatif)"),
            @ApiResponse(responseCode = "404", description = "Véhicule introuvable")
    })
    @PutMapping("/{id}")
    public Vehicule update(@Parameter(description = "Identifiant du véhicule") @PathVariable Long id,
                       @RequestBody Vehicule vehicule) {
        return vehiculeService.update(id, vehicule);
    }

    @Operation(summary = "Supprimer un véhicule")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Véhicule supprimé"),
            @ApiResponse(responseCode = "404", description = "Véhicule introuvable")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@Parameter(description = "Identifiant du véhicule") @PathVariable Long id) {
        vehiculeService.deleteById(id);
    }

    @Operation(summary = "Ajouter un véhicule avec ses équipements et l'affecter à une agence",
            description = "Les équipements sans identifiant sont créés en cascade ; ceux avec identifiant doivent exister.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Véhicule créé et affecté"),
            @ApiResponse(responseCode = "400", description = "Identifiant fourni, tarif négatif ou libellé vide"),
            @ApiResponse(responseCode = "404", description = "Agence ou équipement introuvable")
    })
    @PostMapping("/agence/{idAgence}")
    @ResponseStatus(HttpStatus.CREATED)
    public Vehicule addAndAssignAgence(@RequestBody Vehicule vehicule,
                                       @Parameter(description = "Identifiant de l'agence") @PathVariable Long idAgence) {
        return vehiculeService.addAndAssignAgence(vehicule, idAgence);
    }

    @Operation(summary = "Affecter un véhicule à une agence")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Véhicule affecté"),
            @ApiResponse(responseCode = "404", description = "Véhicule ou agence introuvable")
    })
    @PutMapping("/{idVehicule}/agence/{idAgence}")
    public Vehicule assignAgence(@Parameter(description = "Identifiant du véhicule") @PathVariable Long idVehicule,
                                 @Parameter(description = "Identifiant de l'agence") @PathVariable Long idAgence) {
        return vehiculeService.assignAgence(idVehicule, idAgence);
    }

    @Operation(summary = "Affecter un équipement à un véhicule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Équipement ajouté au véhicule"),
            @ApiResponse(responseCode = "400", description = "Équipement déjà affecté"),
            @ApiResponse(responseCode = "404", description = "Véhicule ou équipement introuvable")
    })
    @PutMapping("/{idVehicule}/equipements/{idEquipement}")
    public Vehicule assignEquipement(@Parameter(description = "Identifiant du véhicule") @PathVariable Long idVehicule,
                                     @Parameter(description = "Identifiant de l'équipement") @PathVariable Long idEquipement) {
        return vehiculeService.assignEquipement(idVehicule, idEquipement);
    }

    @Operation(summary = "Retirer un équipement d'un véhicule", description = "L'équipement reste en base.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Équipement retiré du véhicule"),
            @ApiResponse(responseCode = "400", description = "Équipement non affecté à ce véhicule"),
            @ApiResponse(responseCode = "404", description = "Véhicule ou équipement introuvable")
    })
    @DeleteMapping("/{idVehicule}/equipements/{idEquipement}")
    public Vehicule unassignEquipement(@Parameter(description = "Identifiant du véhicule") @PathVariable Long idVehicule,
                                       @Parameter(description = "Identifiant de l'équipement") @PathVariable Long idEquipement) {
        return vehiculeService.unassignEquipement(idVehicule, idEquipement);
    }
}
