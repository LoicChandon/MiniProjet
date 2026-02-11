package pharmacie.entity;

import java.util.ArrayList;
import java.util.List;

import io.micrometer.common.lang.NonNull;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Size;
import lombok.ToString;

public class Fournisseur {
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
