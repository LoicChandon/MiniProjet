package pharmacie.rest;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pharmacie.dao.MedicamentRepository;
import pharmacie.dao.UnitesParMedicament;

@RestController
@RequestMapping(path = "/api/stats")
public class StatisticsRestController {
    private final MedicamentRepository dao;

    // Injection de dépendance (@Autowired)
    StatisticsRestController(MedicamentRepository dao) {
        this.dao = dao;
    }

    /**
     * Unites vendues pour chaque médicament d'une catégorie donnée.
     *
     * @param code le code de la catégorie à traiter
     * @return le nombre d'unités commandées pour chaque médicament en format JSON
     */
    @GetMapping(path = "unitesCommandeesPourCategorie/{code}", produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    public List<UnitesParMedicament> unitesCommandeesPourCategorie(@PathVariable final Integer code) {
        return dao.medicamentsCommandesPour(code);
    }

    /**
     * Unites vendues pour chaque médicament d'une catégorie donnée. Pas
     * d'utilisation
     * de DTO, renvoie simplement une liste de tableaux de valeurs
     * Plus pratique à utiliser pour Google Charts
     *
     * @param code le code de la catégorie à traiter
     * @return le nombre d'unités commandées pour chaque médicament en format JSON
     */
    @GetMapping(path = "unitesCommandeesPourCategorieV2/{code}", produces = { MediaType.APPLICATION_JSON_VALUE })
    public List<Object> unitesCommandeesPourCategorieV2(@PathVariable final Integer code) {
        return dao.medicamentsCommandesPourV2(code);
    }

}
