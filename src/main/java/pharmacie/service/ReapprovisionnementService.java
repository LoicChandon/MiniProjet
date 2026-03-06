package pharmacie.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import lombok.extern.slf4j.Slf4j;
import pharmacie.dao.FournisseurRepository;
import pharmacie.dao.MedicamentRepository;
import pharmacie.entity.Categorie;
import pharmacie.entity.Fournisseur;
import pharmacie.entity.Medicament;

/**
 * Service métier de réapprovisionnement.
 * <p>
 * Détermine les médicaments à réapprovisionner (unitesEnStock < niveauDeReappro)
 * et envoie un mail personnalisé à chaque fournisseur susceptible de les fournir,
 * via l'API SendGrid (pas de serveur SMTP nécessaire).
 * Chaque fournisseur reçoit UN SEUL mail récapitulant, catégorie par catégorie,
 * les médicaments à réapprovisionner qu'il peut fournir.
 */
@Slf4j
@Service
public class ReapprovisionnementService {

    private final MedicamentRepository medicamentDao;
    private final FournisseurRepository fournisseurDao;

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    @Value("${sendgrid.from-name:Pharmacie}")
    private String fromName;

    public ReapprovisionnementService(MedicamentRepository medicamentDao,
                                       FournisseurRepository fournisseurDao) {
        this.medicamentDao = medicamentDao;
        this.fournisseurDao = fournisseurDao;
    }

    /**
     * DTO interne pour le résultat renvoyé par le contrôleur REST.
     * Récapitule le mail envoyé à un fournisseur.
     */
    public record MailEnvoye(
        String fournisseur,
        String email,
        Map<String, List<String>> medicamentsParCategorie
    ) {}

    /**
     * Effectue le réapprovisionnement :
     * 1. Recherche les médicaments dont unitesEnStock < niveauDeReappro
     * 2. Regroupe ces médicaments par catégorie
     * 3. Pour chaque fournisseur, détermine les catégories qu'il peut fournir
     * 4. Envoie un mail personnalisé à chaque fournisseur concerné via SendGrid
     *
     * @return la liste des mails envoyés (résumé)
     */
    @Transactional(readOnly = true)
    public List<MailEnvoye> verifierEtNotifierReapprovisionnement() {
        // 1. Trouver les médicaments à réapprovisionner
        List<Medicament> medicamentsAReappro = medicamentDao.medicamentsAReapprovisionner();

        if (medicamentsAReappro.isEmpty()) {
            log.info("Aucun médicament à réapprovisionner.");
            return List.of();
        }

        log.info("{} médicament(s) à réapprovisionner.", medicamentsAReappro.size());

        // 2. Regrouper par catégorie (code -> liste de médicaments)
        Map<Categorie, List<Medicament>> parCategorie = medicamentsAReappro.stream()
                .collect(Collectors.groupingBy(Medicament::getCategorie, LinkedHashMap::new, Collectors.toList()));

        // 3. Pour chaque fournisseur, vérifier quelles catégories il fournit
        List<Fournisseur> tousFournisseurs = fournisseurDao.findAll();
        List<MailEnvoye> resultat = new ArrayList<>();

        for (Fournisseur fournisseur : tousFournisseurs) {
            // Catégories que ce fournisseur peut fournir ET qui ont des médicaments à réapprovisionner
            Map<String, List<String>> medicamentsPourCeFournisseur = new LinkedHashMap<>();

            for (Categorie categorieFournie : fournisseur.getCategories()) {
                List<Medicament> medsAReappro = parCategorie.get(categorieFournie);
                if (medsAReappro != null && !medsAReappro.isEmpty()) {
                    List<String> nomsMedicaments = medsAReappro.stream()
                            .map(m -> String.format("%s (stock: %d, seuil: %d)",
                                    m.getNom(), m.getUnitesEnStock(), m.getNiveauDeReappro()))
                            .toList();
                    medicamentsPourCeFournisseur.put(categorieFournie.getLibelle(), nomsMedicaments);
                }
            }

            if (!medicamentsPourCeFournisseur.isEmpty()) {
                // Construire et envoyer le mail via SendGrid
                envoyerMail(fournisseur, medicamentsPourCeFournisseur);
                resultat.add(new MailEnvoye(
                        fournisseur.getNom(),
                        fournisseur.getAdresseElectronique(),
                        medicamentsPourCeFournisseur
                ));
            }
        }

        log.info("{} mail(s) de réapprovisionnement envoyé(s).", resultat.size());
        return resultat;
    }

    /**
     * Construit et envoie un mail personnalisé à un fournisseur via l'API SendGrid.
     */
    private void envoyerMail(Fournisseur fournisseur, Map<String, List<String>> medicamentsParCategorie) {
        StringBuilder corps = new StringBuilder();
        corps.append("Bonjour ").append(fournisseur.getNom()).append(",\n\n");
        corps.append("Nous vous contactons pour vous demander un devis de réapprovisionnement ")
             .append("pour les médicaments suivants :\n\n");

        for (Map.Entry<String, List<String>> entry : medicamentsParCategorie.entrySet()) {
            corps.append("=== ").append(entry.getKey()).append(" ===\n");
            for (String medicament : entry.getValue()) {
                corps.append("  - ").append(medicament).append("\n");
            }
            corps.append("\n");
        }

        corps.append("Merci de nous transmettre votre devis dans les meilleurs délais.\n\n");
        corps.append("Cordialement,\nLa Pharmacie");

        Email expediteur = new Email(fromEmail, fromName);
        Email destinataire = new Email(fournisseur.getAdresseElectronique());
        String sujet = "Demande de devis de réapprovisionnement";
        Content contenu = new Content("text/plain", corps.toString());
        Mail mail = new Mail(expediteur, sujet, destinataire, contenu);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Mail envoyé à {} ({}) - Status: {}",
                        fournisseur.getNom(), fournisseur.getAdresseElectronique(), response.getStatusCode());
            } else {
                log.error("Échec de l'envoi du mail à {} - Status: {}, Body: {}",
                        fournisseur.getAdresseElectronique(), response.getStatusCode(), response.getBody());
                throw new RuntimeException("Échec de l'envoi du mail à " + fournisseur.getAdresseElectronique()
                        + " (HTTP " + response.getStatusCode() + ")");
            }
        } catch (IOException e) {
            log.error("Erreur lors de l'envoi du mail à {} : {}", fournisseur.getAdresseElectronique(), e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi du mail à " + fournisseur.getAdresseElectronique(), e);
        }
    }

    /**
     * Diagnostic : vérifie la clé SendGrid et liste les médicaments à réapprovisionner.
     */
    public Map<String, Object> diagnostic() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. Vérifier la clé API
        boolean apiKeyPresente = sendGridApiKey != null && !sendGridApiKey.isBlank() && !"MISSING".equals(sendGridApiKey);
        result.put("sendgrid_api_key_configuree", apiKeyPresente);
        result.put("sendgrid_api_key_debut", apiKeyPresente ? sendGridApiKey.substring(0, Math.min(8, sendGridApiKey.length())) + "..." : "NON CONFIGURÉE");
        result.put("from_email", fromEmail);

        // 2. Tester la connexion SendGrid (appel léger)
        if (apiKeyPresente) {
            try {
                SendGrid sg = new SendGrid(sendGridApiKey);
                Request request = new Request();
                request.setMethod(Method.GET);
                request.setEndpoint("scopes");
                Response response = sg.api(request);
                result.put("sendgrid_connexion_status", response.getStatusCode());
                result.put("sendgrid_connexion_ok", response.getStatusCode() == 200);
                if (response.getStatusCode() != 200) {
                    result.put("sendgrid_erreur", response.getBody());
                }
            } catch (IOException e) {
                result.put("sendgrid_connexion_ok", false);
                result.put("sendgrid_erreur", e.getMessage());
            }
        }

        // 3. Médicaments à réapprovisionner
        List<Medicament> medsAReappro = medicamentDao.medicamentsAReapprovisionner();
        result.put("nb_medicaments_a_reapprovisionner", medsAReappro.size());
        result.put("medicaments_a_reapprovisionner", medsAReappro.stream()
                .map(m -> Map.of(
                        "nom", m.getNom(),
                        "stock", m.getUnitesEnStock(),
                        "seuil", m.getNiveauDeReappro(),
                        "categorie", m.getCategorie().getLibelle()))
                .toList());

        // 4. Fournisseurs concernés
        List<Fournisseur> fournisseurs = fournisseurDao.findAll();
        result.put("nb_fournisseurs", fournisseurs.size());
        result.put("fournisseurs", fournisseurs.stream()
                .map(f -> Map.of(
                        "nom", f.getNom(),
                        "email", f.getAdresseElectronique(),
                        "nb_categories", f.getCategories().size()))
                .toList());

        return result;
    }
}
