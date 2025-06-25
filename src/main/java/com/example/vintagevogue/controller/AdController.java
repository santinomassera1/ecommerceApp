package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Ad;
import com.example.vintagevogue.service.AdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/admin/ad")
public class AdController {

    @Autowired
    private AdService adService;
    
    @Value("${upload.path:./src/main/resources/static/uploads}")
    private String uploadPath;
    
    // Patrón para validar URLs de imágenes
    private static final Pattern IMAGE_URL_PATTERN = Pattern.compile(".*\\.(jpeg|jpg|gif|png|webp|svg|bmp)(\\?.*)?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern GOOGLE_URL_PATTERN = Pattern.compile(".*(google\\.com\\/url|googleusercontent\\.com).*", Pattern.CASE_INSENSITIVE);

    @GetMapping
    public String manageAds(Model model) {
        model.addAttribute("ads", adService.getAllAds());
        model.addAttribute("ad", new Ad());
        return "manage-ad";
    }

    @PostMapping("/save")
    public String saveAd(@ModelAttribute Ad ad, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile, 
                         RedirectAttributes redirectAttributes) {
        try {
            // Validar la URL de la imagen si se proporciona
            if (ad.getImageUrl() != null && !ad.getImageUrl().trim().isEmpty()) {
                String imageUrl = ad.getImageUrl().trim();
                
                // Verificar si es una URL de Google
                if (GOOGLE_URL_PATTERN.matcher(imageUrl).matches()) {
                    redirectAttributes.addFlashAttribute("error", 
                        "La URL proporcionada parece ser una URL de redirección de Google. " +
                        "Por favor, use la URL directa de la imagen (clic derecho en la imagen → Copiar dirección de imagen).");
                    return "redirect:/admin/ad";
                }
                
                // Verificar si la URL tiene un formato de imagen válido
                if (!IMAGE_URL_PATTERN.matcher(imageUrl).matches()) {
                    redirectAttributes.addFlashAttribute("error", 
                        "La URL proporcionada no parece ser una imagen válida. " +
                        "Asegúrese de que la URL termine en .jpg, .png, .gif, etc.");
                    return "redirect:/admin/ad";
                }
                
                // Intentar validar la URL
                try {
                    new URL(imageUrl).toURI();
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "La URL proporcionada no es válida: " + e.getMessage());
                    return "redirect:/admin/ad";
                }
            }
            
            // Procesar el archivo subido
            if (imageFile != null && !imageFile.isEmpty()) {
                // Crear directorio de uploads si no existe
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                
                // Generar nombre único para el archivo
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String fileName = UUID.randomUUID().toString() + fileExtension;
                
                // Ruta completa del archivo
                Path filePath = Paths.get(uploadPath, fileName);
                
                // Guardar archivo con opción de reemplazo si existe
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                
                // Actualizar URL de la imagen en el anuncio
                ad.setImageUrl("/uploads/" + fileName);
                
                System.out.println("Archivo guardado en: " + filePath.toAbsolutePath());
                System.out.println("URL de imagen establecida: " + ad.getImageUrl());
            }
            
            // Guardar el anuncio
            adService.saveAd(ad);
            redirectAttributes.addFlashAttribute("success", "Anuncio guardado correctamente");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al guardar el archivo: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al guardar el anuncio: " + e.getMessage());
        }
        
        return "redirect:/admin/ad";
    }

    @GetMapping("/edit/{id}")
    public String editAd(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Ad ad = adService.getAdById(id).orElseThrow(() -> new IllegalArgumentException("ID de anuncio inválido: " + id));
            model.addAttribute("ad", ad);
            model.addAttribute("ads", adService.getAllAds());
            return "manage-ad";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al editar el anuncio: " + e.getMessage());
            return "redirect:/admin/ad";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteAd(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Ad ad = adService.getAdById(id).orElse(null);
            if (ad != null && ad.getImageUrl() != null && ad.getImageUrl().startsWith("/uploads/")) {
                try {
                    // Eliminar archivo físico si existe
                    File file = new File(uploadPath + ad.getImageUrl().substring(8));
                    if (file.exists()) {
                        file.delete();
                        System.out.println("Archivo eliminado: " + file.getAbsolutePath());
                    } else {
                        System.out.println("El archivo no existe: " + file.getAbsolutePath());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Continuar con la eliminación del registro aunque falle la eliminación del archivo
                }
            }
            
            adService.deleteAd(id);
            redirectAttributes.addFlashAttribute("success", "Anuncio eliminado correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el anuncio: " + e.getMessage());
        }
        
        return "redirect:/admin/ad";
    }
}
