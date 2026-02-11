package pharmacie.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class EnTeteCommandeDTO {
    private Integer numero;
    private DispensaireDTO dispensaire;
    private LocalDate saisiele;
}
