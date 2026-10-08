package tn.esprit.wafik_lahbib_4cce10;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tn.esprit.wafik_lahbib_4cce10.domain.Contrat;
import tn.esprit.wafik_lahbib_4cce10.domain.ModePaiement;
import tn.esprit.wafik_lahbib_4cce10.domain.Paiement;
import tn.esprit.wafik_lahbib_4cce10.repository.IContratRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@SpringBootApplication
public class WafikLahbib4cce10Application implements CommandLineRunner {

    private final IContratRepository contratRepository;

    public WafikLahbib4cce10Application(IContratRepository contratRepository) {
        this.contratRepository = contratRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(WafikLahbib4cce10Application.class, args);
    }

    // petit programme de test pour voir les requetes SQL du CRUD sur Contrat
    @Override
    public void run(String... args) {
        LocalDate aujourdhui = LocalDate.now(ZoneId.of("Africa/Tunis"));

        // 1) creation : un contrat avec 2 paiements
        Contrat contrat = new Contrat();
        contrat.setDateSignature(aujourdhui);
        contrat.setMontantTotal(new BigDecimal("900"));
        contrat.setValide(true);

        Paiement p1 = new Paiement();
        p1.setMontant(new BigDecimal("400"));
        p1.setDatePaiement(aujourdhui);
        p1.setModePaiement(ModePaiement.CARTE);
        p1.setContrat(contrat);

        Paiement p2 = new Paiement();
        p2.setMontant(new BigDecimal("500"));
        p2.setDatePaiement(aujourdhui);
        p2.setModePaiement(ModePaiement.ESPECES);
        p2.setContrat(contrat);

        contrat.getPaiements().add(p1);
        contrat.getPaiements().add(p2);

        contrat = contratRepository.save(contrat);
        Long id = contrat.getIdContrat();
        log.info("contrat cree, id = {}", id);

        // 2) lecture
        Optional<Contrat> lu = contratRepository.findById(id);
        if (lu.isPresent()) {
            BigDecimal montantLu = lu.get().getMontantTotal();
            log.info("contrat lu, montant = {}", montantLu);
        }
        long nbContrats = contratRepository.count();
        log.info("nombre de contrats = {}", nbContrats);

        // 3) mise a jour + on retire un paiement de la liste (orphanRemoval)
        contrat.setMontantTotal(new BigDecimal("950"));
        contrat.getPaiements().remove(0);
        contrat = contratRepository.save(contrat);
        BigDecimal nouveauMontant = contrat.getMontantTotal();
        log.info("contrat modifie, montant = {}", nouveauMontant);

        // 4) suppression : les paiements restants partent avec le contrat (cascade)
        contratRepository.deleteById(id);
        boolean existe = contratRepository.existsById(id);
        log.info("contrat existe encore ? {}", existe);
    }
}
