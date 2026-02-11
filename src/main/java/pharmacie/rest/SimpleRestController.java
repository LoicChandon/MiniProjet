package pharmacie.rest;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import pharmacie.dao.CategorieRepository;
import pharmacie.entity.Categorie;
import pharmacie.entity.Medicament;
import pharmacie.exceptions.DuplicateException;

@RestController // Cette classe est un contrôleur REST
@RequestMapping(path = "/api/simple") // chemin d'accès
@Slf4j // Logger
public class SimpleRestController {
    private final CategorieRepository categorieDao;

    // Injection de dépendance (@Autowired)
    SimpleRestController(CategorieRepository categorieDao) {
        this.categorieDao = categorieDao;
    }

    /**
     * Un contrôleur qui renvoie une liste d'entités
     *
     * @return la liste des catégories
     */
    @GetMapping(path = "list")
    public List<Categorie> getAll() {
        // This returns a JSON or XML with the categories
        return categorieDao.findAll();
    }

    /**
     * Ajoute une nouvelle catégorie par POST ou GET
     *
     * @param libelle     le libellé de la nouvelle catégorie
     * @param description la description de la nouvelle catégorie
     * @return la catégorie nouvellement créée, avec sa clé auto-générée
     * @throws DuplicateException si le libellé existe déjà
     */
    @RequestMapping(path = "ajouter", method = { RequestMethod.GET, RequestMethod.POST })
    public Categorie addNew(
            @RequestParam(required = true) final String libelle,
            @RequestParam(defaultValue = "Description non fournie") final String description)
            throws DuplicateException {
        final Categorie result = new Categorie(libelle);
        result.setDescription(description);
        try {
            categorieDao.save(result);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("Le libellé '" + libelle + "' est déjà utilisé");
        }
        return result;
    }

    /**
     * Un contrôleur qui génère du HTML "à la main"
     *
     * @return un fragment de HTML qui montre le nombre de catégories dans la base
     */
    @GetMapping(path = "combien", produces = MediaType.TEXT_HTML_VALUE) // pas de vue , génère directement du HTML
    public String combienDeCategories() {
        return "<h1>Il y a " + categorieDao.count() + " catégories dans la base</h1>";
    }

    /**
     *
     * @param timeout
     * @return
     */
    @GetMapping(path = "wait", produces = MediaType.TEXT_PLAIN_VALUE) // pas de vue , génère directement du texte)
    public String waitFor(@RequestParam(defaultValue = "10") final int timeout) throws InterruptedException {
        Thread.sleep(1000 * timeout);
        return "Après un délai de " + timeout + " seconds";
    }

    @PostMapping(path = "testJson", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String testJSON(@RequestBody Medicament m) {
        log.info("Medicament (JSON): {}", m);
        return m.toString();
    }

    @PostMapping(path = "testForm", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String testForm(Medicament m) {
        log.info("Medicament (FORM): {}", m);
        return m.toString();
    }
}
