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
import tn.esprit.wafik_lahbib_4cce10.domain.Contrat;
import tn.esprit.wafik_lahbib_4cce10.service.IContratService;

@Tag(name = "Contrats", description = "Gestion des contrats de location (les paiements suivent par cascade)")
@RestController
@RequestMapping("/contrats")
@RequiredArgsConstructor
public class ContratController {

    // dépendance vers l'interface du service (jamais le repository ni l'implémentation)
    private final IContratService contratService;

    @Operation(summary = "Lister les contrats")
    @ApiResponse(responseCode = "200", description = "Liste renvoyée (éventuellement vide)")
    @GetMapping
    public List<Contrat> findAll() {
        return contratService.findAll();
    }

    @Operation(summary = "Consulter un contrat")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrat trouvé"),
            @ApiResponse(responseCode = "404", description = "Contrat introuvable")
    })
    @GetMapping("/{id}")
    public Contrat findById(@Parameter(description = "Identifiant du contrat") @PathVariable Long id) {
        return contratService.findById(id);
    }

    @Operation(summary = "Créer un contrat", description = "Le corps ne doit pas contenir d'identifiant.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contrat créé"),
            @ApiResponse(responseCode = "400", description = "Identifiant fourni ou règle non respectée (montant total négatif)")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Contrat create(@RequestBody Contrat contrat) {
        return contratService.create(contrat);
    }

    @Operation(summary = "Modifier un contrat")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrat modifié"),
            @ApiResponse(responseCode = "400", description = "Règle non respectée (montant total négatif)"),
            @ApiResponse(responseCode = "404", description = "Contrat introuvable")
    })
    @PutMapping("/{id}")
    public Contrat update(@Parameter(description = "Identifiant du contrat") @PathVariable Long id,
                       @RequestBody Contrat contrat) {
        return contratService.update(id, contrat);
    }

    @Operation(summary = "Supprimer un contrat")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contrat supprimé"),
            @ApiResponse(responseCode = "404", description = "Contrat introuvable")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@Parameter(description = "Identifiant du contrat") @PathVariable Long id) {
        contratService.deleteById(id);
    }

    @Operation(summary = "Ajouter un contrat avec ses paiements",
            description = "Les paiements (sans identifiant) sont créés en cascade avec le contrat.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contrat et paiements créés"),
            @ApiResponse(responseCode = "400", description = "Identifiant fourni, aucun paiement, montant de paiement non positif ou somme des paiements supérieure au montant total")
    })
    @PostMapping("/avec-paiements")
    @ResponseStatus(HttpStatus.CREATED)
    public Contrat addWithPayments(@RequestBody Contrat contrat) {
        return contratService.addWithPayments(contrat);
    }
}
