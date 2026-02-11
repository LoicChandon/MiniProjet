package pharmacie.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import pharmacie.dao.CategorieRepository;

@Controller
@RequestMapping(path = "/stats")
public class StatsMVCController {

	private final CategorieRepository dao;

	public StatsMVCController(CategorieRepository dao) {
		this.dao = dao;
	}

	/**
     * Handles a GET request to display medicament statistics grouped by category.
     * Adds all available categories to the model for rendering in the view.
     *
     * @param model The Model object used to pass attributes to the view.
     * @return The name of the view template to render for medicament statistics by category.
     */
    @GetMapping(path = "medicamentsPourCategorie")

	public	String montreStatsMedicaments(Model model) {
		model.addAttribute("categories", dao.findAll());
		return "statsMedicamentsPourCategorie";
	}
}
