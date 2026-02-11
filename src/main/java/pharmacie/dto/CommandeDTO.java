package pharmacie.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class CommandeDTO {
    private Integer numero;
    private LocalDate saisiele;
    private DispensaireDTO dispensaire;
    private List<LigneDTO> lignes;
}
