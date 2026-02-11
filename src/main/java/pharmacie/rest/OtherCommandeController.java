package pharmacie.rest;

import java.net.URI;

import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import pharmacie.dao.CommandeProjection;
import pharmacie.dao.CommandeRepository;
import pharmacie.entity.Commande;
import pharmacie.entity.Ligne;
import pharmacie.service.CommandeService;

@RestController // Cette classe est un contrôleur REST
@RequestMapping(path = "/api/examples") // chemin d'accès
public class OtherCommandeController {

	private final CommandeService commandeService;

	private final CommandeRepository commandeDao;

    private final RepositoryEntityLinks entityLinks;

    private final DefaultFormattingConversionService conversionService;

    // Injection de dépendance (@Autowired)
	public OtherCommandeController(CommandeService commandeService, CommandeRepository commandeDao, RepositoryEntityLinks entityLinks, DefaultFormattingConversionService conversionService) {
		this.commandeService = commandeService;
		this.commandeDao = commandeDao;
        this.entityLinks = entityLinks;
        this.conversionService = conversionService;
    }

	@GetMapping("projection/{commandeNum}")
	public CommandeProjection projection(@PathVariable Integer commandeNum) {
		return commandeDao.findProjectionByNumero(commandeNum);
	}

    @GetMapping("deserialize")
    public ResponseEntity<EntityModel<Commande>> deserialize(@RequestParam String commandeURI) {
        final URI uri = URI.create(commandeURI);
        Commande commande = conversionService.convert(uri, Commande.class);
        return commande != null ? ResponseEntity.ok(EntityModel.of(commande)) : ResponseEntity.notFound().build();
    }

	@GetMapping("ajouterPourClient/{clientCode}")
	public EntityModel<Commande> ajouterEntity(@PathVariable @NonNull String clientCode) {
        var commande = commandeService.creerCommande(clientCode);
        Link selfLink = entityLinks.linkToItemResource(Commande.class, commande.getNumero()).withSelfRel();
		return EntityModel.of(commande, selfLink);
    }

	@GetMapping("ajouterRedirect")
	public RedirectView ajouterLigneRedirect(@RequestParam int commandeNum, @RequestParam int produitRef, @RequestParam int quantite) {
		var ligne = commandeService.ajouterLigne(commandeNum, produitRef, quantite);
        Link selfLink = entityLinks.linkToItemResource(Ligne.class, ligne.getId());
		return new RedirectView(selfLink.getHref());
	}

}
