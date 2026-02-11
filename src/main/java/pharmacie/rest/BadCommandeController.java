package pharmacie.rest;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import pharmacie.entity.Commande;
import pharmacie.service.CommandeService;

@RestController // Cette classe est un contrôleur REST
@RequestMapping("/api/bad/commandes") // chemin d'accès
public class BadCommandeController {
    private final CommandeService commandeService;

    // Injection de dépendance (@Autowired)
    public BadCommandeController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    // Le chemin d'accès sera
    // http://.../api/bad/commandes/ajouterPour/CODE_DU_CLIENT
    @GetMapping("ajouterPour/{codeDispensaire}")
    // PAS BON ! on renvoie une entité JPA !
    public Commande ajouter(@PathVariable @NonNull String codeDispensaire) {
        return commandeService.creerCommande(codeDispensaire);
    }
}
