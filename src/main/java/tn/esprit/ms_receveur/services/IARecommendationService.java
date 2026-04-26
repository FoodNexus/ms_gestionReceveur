package tn.esprit.ms_receveur.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class IARecommendationService {

    private static final String NUTRIFLOW_DATA_DIR = "nutriflow-data";
    private static final String VENV_PYTHON = "venv/bin/python3";
    private static final String RECOMMEND_SCRIPT = "scripts/recommend_basket.py";

    private String getBaseDir() {
        return System.getProperty("user.home") + "/" + NUTRIFLOW_DATA_DIR;
    }

    public boolean isIAAvailable() {
        String baseDir = getBaseDir();
        return new File(baseDir + "/" + VENV_PYTHON).exists() &&
                new File(baseDir + "/" + RECOMMEND_SCRIPT).exists();
    }

    public RecommendationResult getRecommendations(int nbPersonnes, String region, String age, String sex) {
        if (!isIAAvailable()) {
            return getDefaultRecommendations(nbPersonnes);
        }

        String baseDir = getBaseDir();
        String pythonPath = baseDir + "/" + VENV_PYTHON;
        String scriptPath = baseDir + "/" + RECOMMEND_SCRIPT;

        try {
            log.info("Exécution IA pour {} personnes ({}, {}, {})", nbPersonnes, region, age, sex);

            // Ajout des paramètres --age et --sex pour le script Python
            ProcessBuilder pb = new ProcessBuilder(
                    pythonPath, scriptPath,
                    "--nb", String.valueOf(nbPersonnes),
                    "--region", region,
                    "--age", age,
                    "--sex", sex
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            if (process.waitFor() == 0) {
                return parseRecommendations(output.toString(), nbPersonnes);
            }
        } catch (Exception e) {
            log.error("Erreur exécution IA: {}", e.getMessage());
        }
        return getDefaultRecommendations(nbPersonnes);
    }

    private static final Map<String, String> TRANSLATIONS = new HashMap<>();

    static {
        TRANSLATIONS.put("baguette bread", "Pain Baguette");
        TRANSLATIONS.put("Tabouna bread", "Pain Tabouna");
        TRANSLATIONS.put("Couscous, raw", "Couscous (Graines)");
        TRANSLATIONS.put("Spaghetti", "Pâtes (Spaghetti)");
        TRANSLATIONS.put("Whole cow's milk", "Lait de vache");
        TRANSLATIONS.put("Dry chickpeas, raw", "Pois chiches");
        TRANSLATIONS.put("Dry bean, raw", "Haricots secs");
        TRANSLATIONS.put("Lentil, raw", "Lentilles");
        TRANSLATIONS.put("Tomato, raw", "Tomates fraîches");
        TRANSLATIONS.put("Concentrated tomato", "Tomate concentrée");
        TRANSLATIONS.put("Onion, raw", "Oignons");
        TRANSLATIONS.put("Potatoes, raw", "Pommes de terre");
        TRANSLATIONS.put("Seed oil", "Huile végétale");
        TRANSLATIONS.put("Olive oil", "Huile d'olive");
        TRANSLATIONS.put("Sugar", "Sucre");
        TRANSLATIONS.put("Chicken meat, raw", "Viande de Poulet");
        TRANSLATIONS.put("Lamb, raw", "Viande d'Agneau");
        TRANSLATIONS.put("Beef meat, raw", "Viande de Bœuf");
        TRANSLATIONS.put("Egg, whole, raw", "Œufs");
        TRANSLATIONS.put("Rice, raw", "Riz");
        TRANSLATIONS.put("Flour, powder", "Farine");
        TRANSLATIONS.put("Semolina, raw", "Semoule");
    }

    private RecommendationResult parseRecommendations(String output, int nbPersonnes) {
        RecommendationResult result = new RecommendationResult();
        Map<String, Double> produits = new LinkedHashMap<>();
        double confiance = 94.0; // Valeur par défaut

        // Pattern pour matcher "Produit: Valeur" ou "CONFIDENCE: Valeur"
        Pattern prodPattern = Pattern.compile("([^:]+):\\s*(\\d+[.,]?\\d*)");

        for (String line : output.split("\n")) {
            if (line.startsWith("CONFIDENCE:")) {
                try {
                    confiance = Double.parseDouble(line.replace("CONFIDENCE:", "").trim());
                } catch (Exception e) {
                    log.warn("Erreur parsing confiance: {}", e.getMessage());
                }
                continue;
            }

            Matcher matcher = prodPattern.matcher(line);
            if (matcher.find()) {
                String produitEng = matcher.group(1).trim();
                double quantite = Double.parseDouble(matcher.group(2).replace(",", "."));
                
                String produitFr = TRANSLATIONS.getOrDefault(produitEng, produitEng);
                produits.put(produitFr, Math.round(quantite * 100.0) / 100.0);
            }
        }

        if (produits.isEmpty()) return getDefaultRecommendations(nbPersonnes);

        result.setProduits(produits);
        result.setConfiance(confiance);
        return result;
    }

    private RecommendationResult getDefaultRecommendations(int nbPersonnes) {
        RecommendationResult result = new RecommendationResult();
        Map<String, Double> produits = new LinkedHashMap<>();
        
        // Panier de base par défaut (hebdomadaire)
        produits.put("Pain Baguette", Math.round(nbPersonnes * 1.75 * 100.0) / 100.0);
        produits.put("Lait de vache", Math.round(nbPersonnes * 1.0 * 100.0) / 100.0);
        produits.put("Couscous (Graines)", Math.round(nbPersonnes * 0.8 * 100.0) / 100.0);
        produits.put("Pâtes (Spaghetti)", Math.round(nbPersonnes * 0.5 * 100.0) / 100.0);
        produits.put("Huile végétale", Math.round(nbPersonnes * 0.15 * 100.0) / 100.0);
        produits.put("Sucre", Math.round(nbPersonnes * 0.1 * 100.0) / 100.0);
        
        result.setProduits(produits);
        result.setConfiance(70.0);
        return result;
    }

    public static class RecommendationResult {
        private Map<String, Double> produits;
        private double confiance;
        public Map<String, Double> getProduits() { return produits; }
        public void setProduits(Map<String, Double> produits) { this.produits = produits; }
        public double getConfiance() { return confiance; }
        public void setConfiance(double confiance) { this.confiance = confiance; }
    }
}
