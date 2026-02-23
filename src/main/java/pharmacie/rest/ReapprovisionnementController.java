package pharmacie.rest;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import pharmacie.service.ReapprovisionnementService;
import pharmacie.service.ReapprovisionnementService.MailEnvoye;

/**
 * Contrôleur REST pour le service de réapprovisionnement.
 * Permet de déclencher la vérification des stocks et l'envoi des mails aux fournisseurs.
 */
@RestController
@RequestMapping(path = "/api/reapprovisionnement")
@Slf4j
public class ReapprovisionnementController {

    private final ReapprovisionnementService reapprovisionnementService;

    public ReapprovisionnementController(ReapprovisionnementService reapprovisionnementService) {
        this.reapprovisionnementService = reapprovisionnementService;
    }

    /**
     * Déclenche la vérification des stocks et l'envoi des demandes de devis.
     * Retourne la liste des mails envoyés aux fournisseurs avec le détail
     * des médicaments à réapprovisionner, catégorie par catégorie.
     *
     * @return la liste des mails envoyés (résumé par fournisseur)
     */
    @PostMapping(path = "notifier")
    public List<MailEnvoye> notifierFournisseurs() {
        log.info("Déclenchement du réapprovisionnement");
        return reapprovisionnementService.verifierEtNotifierReapprovisionnement();
    }
}
