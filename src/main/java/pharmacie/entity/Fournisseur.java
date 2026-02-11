package pharmacie.entity;

import java.util.ArrayList;
import java.util.List;

import io.micrometer.common.lang.NonNull;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



@Entity
@Getter @Setter @ToString
@Table(uniqueConstraints = {
	@UniqueConstraint(columnNames = {"NOM_REFERENCE", "ADRESSEELECTRONIQUE_REFERENCE"})
})
public class Fournisseur {
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(nullable = false)
	@Setter(AccessLevel.NONE) // la clé est auto-générée par la BD, On ne veut pas de "setter"
	private Integer id;

    @Basic(optional = false)
    @NonNull
    @Size(min = 1, max = 40)
    @Column(nullable = false, length = 40)
    private String nom;

    @Basic(optional = false)
    @Column(length = 30)
    private String adresseElectronique;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fournisseur")
    @ToString.Exclude
    private List<Categorie> categories = new ArrayList<>();

}
