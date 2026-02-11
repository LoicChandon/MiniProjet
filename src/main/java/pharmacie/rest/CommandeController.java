package pharmacie.rest;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import pharmacie.dto.CommandeDTO;
import pharmacie.dto.EnTeteCommandeDTO;
import pharmacie.dto.LigneDTO;
import pharmacie.entity.Commande;
import pharmacie.service.CommandeService;

@Slf4j
@RestController // Cette classe est un contrôleur REST
@RequestMapping(path = "/api/services/commandes") // chemin d'accès
public class CommandeController {
	private final CommandeService commandeService;
	private final ModelMapper mapper;

    // Injection de dépendance (@Autowired)
	public CommandeController(CommandeService commandeService, ModelMapper mapper) {
		this.commandeService = commandeService;
		this.mapper = mapper;
	}

	@PostMapping("ajouterPour/{dispensaireCode}")
	public  ResponseEntity<CommandeDTO> ajouter(@PathVariable @NonNull String dispensaireCode) {
        log.info("Contrôleur : ajouter commande pour {}", dispensaireCode);
		Commande commande = commandeService.creerCommande(dispensaireCode);
        var body = mapper.map(commande, CommandeDTO.class);
        return ResponseEntity.ok(body);
	}

	@PostMapping("expedier/{commandeNum}")
	public ResponseEntity<EnTeteCommandeDTO> expedier(@PathVariable Integer commandeNum) {
        log.info("Contrôleur : expédier la commande {}", commandeNum);
        var body = mapper.map(commandeService.enregistreExpedition(commandeNum), EnTeteCommandeDTO.class);
		return ResponseEntity.ok(body);
	}

	@PostMapping("ajouterLigne")
	public ResponseEntity<LigneDTO> ajouterLigne(@RequestParam int commandeNum, @RequestParam int medicamentRef, @RequestParam int quantite) {
        log.info("Contrôleur : ajouterLigne {} {} {}", commandeNum, medicamentRef, quantite);
		var ligne = commandeService.ajouterLigne(commandeNum, medicamentRef, quantite);
        var body = mapper.map(ligne, LigneDTO.class);
        return ResponseEntity.ok(body);
	}

    @DeleteMapping("supprimerLigne/{idLigne}")
    public ResponseEntity<Void>  supprimerLigne(@PathVariable Integer idLigne) {
        log.info("Contrôleur : supprimerLigne {}", idLigne);
        commandeService.supprimerLigne(idLigne);
        // Renvoie : 204 - No Content
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{commandeNum}")
    public ResponseEntity<CommandeDTO> getCommande(@PathVariable Integer commandeNum) {
        log.info("Contrôleur : getCommande {}", commandeNum);
        var body = mapper.map(commandeService.getCommande(commandeNum), CommandeDTO.class);
        return ResponseEntity.ok(body);
    }

    @GetMapping("enCoursPour/{dispensaireCode}")
    public ResponseEntity<List<EnTeteCommandeDTO>> getCommandeEnCoursPour(@PathVariable @NonNull String dispensaireCode) {
        log.info("Contrôleur : getCommandeEnCoursPour {}", dispensaireCode);
        List<Commande> commandes = commandeService.getCommandeEnCoursPour(dispensaireCode);

        List<EnTeteCommandeDTO> result = new ArrayList<>();
        for (Commande commande : commandes) {
            result.add(mapper.map(commande, EnTeteCommandeDTO.class));
        }
        // Ca peut s'écrire en une ligne avec une expression lambda
        // List<EnTeteCommandeDTO> result = commandes.stream().map(commande -> mapper.map(commande, EnTeteCommandeDTO.class)).collect(Collectors.toList());

        return  ResponseEntity.ok(result);
    }
}
