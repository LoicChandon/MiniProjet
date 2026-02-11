package pharmacie.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import pharmacie.dao.CommandeRepository;
import pharmacie.dao.DispensaireRepository;
import pharmacie.dao.LigneRepository;
import pharmacie.dao.MedicamentRepository;
import pharmacie.entity.Commande;
import pharmacie.entity.Ligne;

@Slf4j
@Service
@Validated // Les annotations de validation sont actives sur les méthodes de ce service
// (ex: @Positive)
public class CommandeService {
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    private final CommandeRepository commandeDao;
    private final DispensaireRepository dispensaireDao;
    private final LigneRepository ligneDao;
    private final MedicamentRepository medicamentDao;

    // @Autowired
    // Spring initialisera automatiquement ces paramètres
    public CommandeService(CommandeRepository commandeDao, DispensaireRepository dispensaireDao, LigneRepository ligneDao, MedicamentRepository medicamentDao) {
        this.commandeDao = commandeDao;
        this.dispensaireDao = dispensaireDao;
        this.ligneDao = ligneDao;
        this.medicamentDao = medicamentDao;
    }

    /**
     * Service métier : Enregistre une nouvelle commande pour un dispensaire connu par sa clé
     * Règles métier :
     * - le dispensaire doit exister
     * - On initialise l'adresse de livraison avec l'adresse du dispensaire
     * - Si le dispensaire a déjà commandé plus de 100 articles, on lui offre une remise de 15%
     *
     * @param dispensaireCode la clé du dispensaire
     * @return la commande créée
     * @throws java.util.NoSuchElementException si le dispensaire n'existe pas
     */
    @Transactional
    public Commande creerCommande(@NonNull String dispensaireCode) {
        log.info("Service : Création d'une commande pour {}", dispensaireCode);
        // On vérifie que le dispensaire existe
        var dispensaire = dispensaireDao.findById(dispensaireCode).orElseThrow();
        // On crée une commande pour ce dispensaire
        var nouvelleCommande = new Commande(dispensaire);
        // On initialise l'adresse de livraison avec l'adresse du dispensaire
        nouvelleCommande.setAdresseLivraison(dispensaire.getAdresse());
        // Si le dispensaire a déjà commandé plus de 100 médicaments, on lui offre une remise de 15%
        // La requête SQL nécessaire est définie dans l'interface DispensaireRepository
        var nbArticles = dispensaireDao.nombreArticlesCommandesPar(dispensaireCode);
        if (nbArticles > 100) {
            nouvelleCommande.setRemise(new BigDecimal("0.15"));
        }
        // On enregistre la commande (génère la clé)
        commandeDao.save(nouvelleCommande);
        return nouvelleCommande;
    }

    /**
     * <pre>
     * Service métier :
     * Enregistre une nouvelle ligne de commande pour une commande connue par sa clé,
     * Incrémente la quantité totale commandée (Medicament.unitesCommandees) avec la quantite à commander
     * Règles métier :
     * - le médicament référencé doit exister et ne pas être indisponible
     * - la commande doit exister
     * - la commande ne doit pas être déjà envoyée (le champ 'envoyeele' doit être null)
     * - la quantité doit être positive
     * - La quantité en stock du médicament ne doit pas être inférieure au total des quantités commandées
     * - Si le médicament est déjà présent dans la commande, les quantités sont additionnées
     * <pre>
     *
     * @param commandeNum la clé de la commande
     * @param medicamentRef  la clé du médicament
     * @param quantite    la quantité commandée (positive)
     * @return la ligne de commande créée
     * @throws java.util.NoSuchElementException                si la commande ou le
     *                                                         médicament n'existe pas
     * @throws IllegalStateException                           si il n'y a pas assez
     *                                                         de stock, si la
     *                                                         commande a déjà été
     *                                                         envoyée, ou si le
     *                                                         médicament est
     *                                                         indisponible
     * @throws jakarta.validation.ConstraintViolationException si la quantité n'est
     *                                                         pas positive
     */
    @Transactional
    public Ligne ajouterLigne(int commandeNum, int medicamentRef, @Positive int quantite) {
        log.info("Service : Ajout d'une ligne ({}, {}) à la commande {}", medicamentRef, quantite, commandeNum);
        // On vérifie que le médicament existe
        var medicament = medicamentDao.findById(medicamentRef).orElseThrow();
        // On vérifie que le médicament n'est pas marqué indisponible
        if (medicament.isIndisponible()) {
            throw new IllegalStateException("Médicament indisponible");
        }
        // On vérifie qu'il y a assez de stock
        if (medicament.getUnitesEnStock() < quantite + medicament.getUnitesCommandees()) {
            throw new IllegalStateException("Pas assez de stock");
        }
        // On vérifie que la commande existe
        var commande = commandeDao.findById(commandeNum).orElseThrow();
        // On vérifie que la commande n'est pas déjà envoyée
        if (commande.getEnvoyeele() != null) {
            throw new IllegalStateException("Commande déjà envoyée");
        }
        // On cherche si une ligne existe déjà dans la commande pour ce médicament
        var ligne = ligneDao.findByCommandeAndMedicament(commande, medicament).
        // Si pas trouvé on crée une nouvelle ligne
                orElse(new Ligne(commande, medicament, 0));
        // On incrémente la quantité de la ligne
        ligne.setQuantite(ligne.getQuantite() + quantite);
        // On incrémente la quantité commandée pour le médicament
        medicament.setUnitesCommandees(medicament.getUnitesCommandees() + quantite);
        // Inutile de sauvegarder le médicament, les entités modifiées par une transaction
        // sont automatiquement sauvegardées à la fin de la transaction
        // On enregistre la ligne de commande (génère la clé)
        return ligneDao.save(ligne);
    }

    /**
     * <pre>
     * Service métier :
     * Supprime une ligne de commande pour une commande connue par sa clé,
     * Décrémente la quantité totale commandée (Medicament.unitesCommandees) de la quantité commandée
     * Règles métier :
     * - la commande ne doit pas être déjà envoyée (le champ 'envoyeele' doit être null)
     * <pre>
     *
     * @param id la clé de la ligne
     * @throws IllegalStateException si la commande a déjà été envoyée
     */
    @Transactional
    public void supprimerLigne(int id) {
        log.info("Service : Supression d'une ligne {}", id);
        // Si la ligne existe
        ligneDao.findById(id).ifPresent(ligne -> {
            var commande = ligne.getCommande();
            // On vérifie que la commande n'est pas déjà envoyée
            if (commande.getEnvoyeele() != null) {
                throw new IllegalStateException("Commande déjà envoyée");
            }
            // On récupère le médicament
            var medicament = ligne.getMedicament();
            // On décrémente la quantité commandée pour le médicament
            medicament.setUnitesCommandees(medicament.getUnitesCommandees() - ligne.getQuantite());
            // On supprime la ligne
            ligneDao.delete(ligne);
            // Inutile de sauvegarder le médicament, les entités modifiées par une transaction
            // sont automatiquement sauvegardées à la fin de la transaction
        });
    }

    /**
     * Service métier : Enregistre l'expédition d'une commande connue par sa clé
     * Règles métier :
     * - la commande doit exister
     * - la commande ne doit pas être déjà envoyée (le champ 'envoyeele' doit être null)
     * - On renseigne la date d'expédition (envoyeele) avec la date du jour
     * - Pour chaque médicament dans les lignes de la commande :
     * décrémente la quantité en stock (Medicament.unitesEnStock) de la quantité dans la commande
     * décrémente la quantité commandée (Medicament.unitesCommandees) de la quantité dans la commande
     *
     * @param commandeNum la clé de la commande
     * @return la commande mise à jour
     * @throws java.util.NoSuchElementException si la commande n'existe pas
     * @throws IllegalStateException            si la commande a déjà été envoyée
     */
    @Transactional
    public Commande enregistreExpedition(int commandeNum) {
        log.info("Service : Expédition de la commande pour {}", commandeNum);

        var commande = commandeDao.findById(commandeNum).orElseThrow();
        if (commande.getEnvoyeele() != null) {
            throw new IllegalStateException("Commande déjà expédiée");
        }
        commande.setEnvoyeele(LocalDate.now());
        commande.getLignes().forEach(ligne -> {
            var medicament = ligne.getMedicament();
            // Les médicaments de la commande ne sont plus en stock
            medicament.setUnitesEnStock(medicament.getUnitesEnStock() - ligne.getQuantite());
            // Les médicaments de la commande ne sont plus "en commande"
            medicament.setUnitesCommandees(medicament.getUnitesCommandees() - ligne.getQuantite());
        });
        return commande;
    }

    /**
     * Service métier : Récupère une commande connue par sa clé
     *
     * @param commandeNum la clé de la commande
     * @return la commande
     * @throws java.util.NoSuchElementException si la commande n'existe pas
     */
    @Transactional
    public Commande getCommande(int commandeNum) {
        return commandeDao.findById(commandeNum).orElseThrow();
    }

    @Transactional
    public List<Commande> getCommandeEnCoursPour(String dispensaireCode) {
        return commandeDao.commandesEnCoursPour(dispensaireCode);
    }
}
