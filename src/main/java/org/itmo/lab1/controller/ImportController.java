package org.itmo.lab1.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.itmo.lab1.dto.OrganizationImportDto;
import org.itmo.lab1.model.ImportHistory;
import org.itmo.lab1.service.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/import")
public class ImportController {

    private final ImportService importService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ImportController(ImportService importService) {
        this.importService = importService;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping
    public String showImportPage(Model model) {
        List<ImportHistory> history = importService.getImportHistory();
        model.addAttribute("importHistory", history);
        return "import";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                           @RequestParam("username") String username,
                           RedirectAttributes redirectAttributes) {
        
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Файл не выбран");
            return "redirect:/import";
        }

        if (username == null || username.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Имя пользователя не может быть пустым");
            return "redirect:/import";
        }

        try {
            // Парсинг JSON файла
            List<OrganizationImportDto> organizations = objectMapper.readValue(
                file.getInputStream(), 
                new TypeReference<List<OrganizationImportDto>>() {}
            );

            if (organizations.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Файл не содержит организаций");
                return "redirect:/import";
            }

            // Импорт организаций
            ImportHistory history = importService.importOrganizations(organizations, username);
            
            if ("SUCCESS".equals(history.getStatus())) {
                redirectAttributes.addFlashAttribute("success", 
                    "Успешно импортировано " + history.getImportedCount() + " организаций");
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Ошибка импорта: " + history.getErrorMessage());
            }

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", 
                "Ошибка чтения файла: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Ошибка импорта: " + e.getMessage());
        }

        return "redirect:/import";
    }
}
