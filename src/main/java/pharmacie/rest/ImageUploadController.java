package pharmacie.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pharmacie.dao.MedicamentRepository;
import pharmacie.entity.Medicament;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.lang.NonNull;

@RestController
@RequestMapping(path = "/api/upload")
@Slf4j
public class ImageUploadController {

    private final MedicamentRepository medicamentRepository;

    // Chemin vers le répertoire static/images
    private static final String UPLOAD_DIR = "/tmp/images/";

    @Autowired
    public ImageUploadController(MedicamentRepository medicamentRepository) {
        this.medicamentRepository = medicamentRepository;
    }

    /**
     * Upload une image pour un médicament
     * @param reference la référence du médicament
     * @param file le fichier image à uploader
     * @return ResponseEntity avec le résultat de l'opération
     */
    @PostMapping("/{reference}/image")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @PathVariable @NonNull Integer reference,
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Vérifier que le fichier n'est pas vide
            if (file.isEmpty()) {
                log.warn("Tentative d'upload d'un fichier vide pour le médicament {}", reference);
                response.put("success", false);
                response.put("message", "Le fichier est vide");
                return ResponseEntity.badRequest().body(response);
            }

            // Vérifier que le médicament existe
            Medicament medicament = medicamentRepository.findById(reference).orElse(null);
            if (medicament == null) {
                log.warn("Médicament avec référence {} introuvable", reference);
                response.put("success", false);
                response.put("message", "Médicament avec référence " + reference + " introuvable");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Vérifier le type de fichier
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("Type de fichier non supporté: {}", contentType);
                response.put("success", false);
                response.put("message", "Le fichier doit être une image (JPEG, PNG, GIF, etc.)");
                return ResponseEntity.badRequest().body(response);
            }

            // Créer le répertoire s'il n'existe pas
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Répertoire d'upload créé: {}", UPLOAD_DIR);
            }

            // Générer un nom de fichier unique
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = "med_" + reference + "_" + UUID.randomUUID().toString() + fileExtension;

            // Sauvegarder le fichier
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Image sauvegardée: {} pour le médicament {}", uniqueFilename, reference);

            // Mettre à jour l'URL de l'image dans la base de données
            String imageUrl = "/images/" + uniqueFilename;
            medicament.setImageURL(imageUrl);
            medicamentRepository.save(medicament);
            log.info("URL de l'image mise à jour pour le médicament {}: {}", reference, imageUrl);

            // Réponse de succès
            response.put("success", true);
            response.put("message", "Image uploadée avec succès");
            response.put("imageUrl", imageUrl);
            response.put("medicament", Map.of(
                "reference", medicament.getReference(),
                "nom", medicament.getNom(),
                "imageURL", medicament.getImageURL()
            ));

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Erreur lors de l'upload de l'image pour le médicament {}", reference, e);
            response.put("success", false);
            response.put("message", "Erreur lors de la sauvegarde du fichier: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'upload de l'image pour le médicament {}", reference, e);
            response.put("success", false);
            response.put("message", "Erreur inattendue: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Récupérer les informations d'un médicament y compris son image
     * @param reference la référence du médicament
     * @return ResponseEntity avec les informations du médicament
     */
    @GetMapping("/{reference}")
    public ResponseEntity<Map<String, Object>> getMedicament(@PathVariable @NonNull Integer reference) {
        Map<String, Object> response = new HashMap<>();

        try {
            Medicament medicament = medicamentRepository.findById(reference).orElse(null);
            if (medicament == null) {
                response.put("success", false);
                response.put("message", "Médicament avec référence " + reference + " introuvable");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("success", true);
            response.put("medicament", Map.of(
                "reference", medicament.getReference(),
                "nom", medicament.getNom(),
                "imageURL", medicament.getImageURL() != null ? medicament.getImageURL() : "",
                "prixUnitaire", medicament.getPrixUnitaire(),
                "unitesEnStock", medicament.getUnitesEnStock(),
                "categorie", medicament.getCategorie().getLibelle()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du médicament {}", reference, e);
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération du médicament: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
