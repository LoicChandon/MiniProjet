package pharmacie.entity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter @Setter @NoArgsConstructor @RequiredArgsConstructor @ToString
public class Categorie {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(AccessLevel.NONE) // la clé est auto-générée par la BD, On ne veut pas de "setter"
	private Integer code;

	@NonNull
	@Size(min = 1, max = 255)
	@Column(unique=true, length = 255)
	@NotBlank // pour éviter les libellés vides
	private String libelle;

	@Size(max = 255)
	@Column(length = 255)
	private String description;

	@ToString.Exclude
	// CascadeType.ALL signifie que toutes les opérations CRUD sur la catégorie sont également appliquées à ses médicaments
	@OneToMany(cascade = {CascadeType.ALL}, mappedBy = "categorie")
	// pour éviter la boucle infinie si on convertit la catégorie en JSON
	@JsonIgnoreProperties({"categorie", "lignes"})
	private List<Medicament> medicaments = new LinkedList<>();


	@OneToMany(cascade = CascadeType.ALL, mappedBy = "categories")
    @ToString.Exclude
    private List<Fournisseur> fournisseurs = new ArrayList<>();
}
