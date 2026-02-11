package pharmacie.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import pharmacie.entity.Fournisseur;


public interface  FournisseurRepository extends JpaRepository<Fournisseur, Integer>{
    
    /**
     * Recherche un fournisseur par son nom (unique)
     *
     * @param nom le nom recherché
     * @return Un fournisseur avec ce nom
     */
    Fournisseur findByNom(String nom);
}
