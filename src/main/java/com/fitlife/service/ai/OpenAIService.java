package com.fitlife.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {
    
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${ai.openai.api-key:}")
    private String apiKey;
    
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    
    public String generateNutritionAdvice(String userContext, String query) {
        try {
            String prompt = buildNutritionPrompt(userContext, query);
            return callOpenAI(prompt, "nutrition-coach");
        } catch (Exception e) {
            log.error("Error generating nutrition advice", e);
            return "Lo siento, no pude generar una respuesta en este momento. Por favor, intenta de nuevo más tarde.";
        }
    }
    
    public String analyzeFoodImage(String detectedFoods, String userGoals) {
        try {
            String prompt = buildFoodAnalysisPrompt(detectedFoods, userGoals);
            return callOpenAI(prompt, "food-analyzer");
        } catch (Exception e) {
            log.error("Error analyzing food image", e);
            return "No pude analizar la imagen correctamente. Por favor, intenta con otra foto.";
        }
    }
    
    public String generateMealRecommendations(String userProfile, String preferences, String restrictions) {
        try {
            String prompt = buildMealRecommendationPrompt(userProfile, preferences, restrictions);
            return callOpenAI(prompt, "meal-planner");
        } catch (Exception e) {
            log.error("Error generating meal recommendations", e);
            return "No pude generar recomendaciones de comidas en este momento.";
        }
    }
    
    public String generateMotivationalMessage(String userProgress, String challenges) {
        try {
            String prompt = buildMotivationPrompt(userProgress, challenges);
            return callOpenAI(prompt, "motivational-coach");
        } catch (Exception e) {
            log.error("Error generating motivational message", e);
            return "¡Sigue adelante! Cada paso cuenta en tu camino hacia una vida más saludable.";
        }
    }
    
    private String callOpenAI(String prompt, String context) throws IOException {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-openai-key")) {
            log.warn("OpenAI API key not configured, returning mock response");
            return generateMockResponse(context, prompt);
        }
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4");
        requestBody.put("messages", List.of(
            Map.of("role", "system", "content", getSystemPrompt(context)),
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);
        
        RequestBody body = RequestBody.create(
            objectMapper.writeValueAsString(requestBody),
            MediaType.get("application/json")
        );
        
        Request request = new Request.Builder()
            .url(OPENAI_API_URL)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .post(body)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("OpenAI API call failed: " + response.code());
            }
            
            String responseBody = response.body().string();
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
            
            return "No pude generar una respuesta válida.";
        }
    }
    
    private String getSystemPrompt(String context) {
        return switch (context) {
            case "nutrition-coach" -> """
                Eres un nutricionista experto y coach de FitLife, la mejor app para bajar de peso del mundo.
                Tu objetivo es ayudar a los usuarios a alcanzar sus metas de pérdida de peso de manera saludable.
                Responde en español, de manera amigable y motivadora.
                Proporciona consejos prácticos y personalizados basados en la información del usuario.
                Mantén las respuestas concisas pero informativas (máximo 300 palabras).
                """;
            case "food-analyzer" -> """
                Eres un experto en análisis nutricional de FitLife.
                Analiza los alimentos detectados en la imagen y proporciona información nutricional precisa.
                Calcula calorías, macronutrientes y da recomendaciones sobre porciones.
                Responde en español de manera clara y educativa.
                """;
            case "meal-planner" -> """
                Eres un planificador de comidas experto de FitLife.
                Crea recomendaciones de comidas personalizadas basadas en los objetivos del usuario.
                Considera restricciones dietéticas, preferencias y metas de pérdida de peso.
                Proporciona opciones variadas y deliciosas en español.
                """;
            case "motivational-coach" -> """
                Eres un coach motivacional de FitLife, especializado en apoyo emocional para pérdida de peso.
                Proporciona mensajes inspiradores y prácticos para superar desafíos.
                Mantén un tono positivo, empático y alentador en español.
                Ayuda a los usuarios a mantener la motivación y superar obstáculos.
                """;
            default -> "Eres un asistente de salud y fitness de FitLife. Responde de manera útil y motivadora en español.";
        };
    }
    
    private String buildNutritionPrompt(String userContext, String query) {
        return String.format("""
            Contexto del usuario: %s
            
            Pregunta del usuario: %s
            
            Por favor, proporciona una respuesta personalizada que incluya:
            1. Respuesta directa a la pregunta
            2. Consejos prácticos aplicables
            3. Motivación para continuar con sus objetivos
            """, userContext, query);
    }
    
    private String buildFoodAnalysisPrompt(String detectedFoods, String userGoals) {
        return String.format("""
            Alimentos detectados en la imagen: %s
            Objetivos del usuario: %s
            
            Analiza estos alimentos y proporciona:
            1. Estimación de calorías totales
            2. Desglose de macronutrientes
            3. Evaluación nutricional (saludable/no saludable)
            4. Sugerencias de mejora si es necesario
            """, detectedFoods, userGoals);
    }
    
    private String buildMealRecommendationPrompt(String userProfile, String preferences, String restrictions) {
        return String.format("""
            Perfil del usuario: %s
            Preferencias: %s
            Restricciones: %s
            
            Genera 3 recomendaciones de comidas que incluyan:
            1. Nombre del plato
            2. Ingredientes principales
            3. Estimación de calorías
            4. Por qué es buena para sus objetivos
            """, userProfile, preferences, restrictions);
    }
    
    private String buildMotivationPrompt(String userProgress, String challenges) {
        return String.format("""
            Progreso del usuario: %s
            Desafíos actuales: %s
            
            Genera un mensaje motivacional que:
            1. Reconozca su progreso
            2. Aborde sus desafíos específicos
            3. Proporcione estrategias prácticas
            4. Inspire a continuar
            """, userProgress, challenges);
    }
    
    private String generateMockResponse(String context, String prompt) {
        return switch (context) {
            case "nutrition-coach" -> """
                ¡Excelente pregunta! Basándome en tu perfil, te recomiendo enfocarte en alimentos ricos en proteína y fibra 
                que te ayudarán a sentirte satisfecho por más tiempo. Incluye vegetales de hoja verde, proteínas magras 
                como pollo o pescado, y carbohidratos complejos como quinoa o avena. 
                
                Recuerda que la hidratación también es clave - intenta beber al menos 8 vasos de agua al día. 
                ¡Vas por buen camino, sigue así! 💪
                """;
            case "food-analyzer" -> """
                Análisis de tu comida:
                
                🔍 Calorías estimadas: 450-500 kcal
                🥩 Proteínas: 25g (excelente)
                🍞 Carbohidratos: 35g (moderado)
                🥑 Grasas: 18g (saludables)
                
                ✅ Esta comida está bien balanceada para tus objetivos. La porción de proteína es perfecta y los vegetales 
                aportan fibra y micronutrientes importantes. ¡Buen trabajo!
                """;
            case "meal-planner" -> """
                Aquí tienes 3 opciones deliciosas para tu próxima comida:
                
                🥗 Ensalada de Quinoa con Pollo (380 kcal)
                - Quinoa, pechuga de pollo, espinacas, tomate cherry, aguacate
                
                🐟 Salmón con Vegetales al Vapor (420 kcal)  
                - Filete de salmón, brócoli, zanahorias, calabacín
                
                🍳 Tortilla de Vegetales (320 kcal)
                - Huevos, espinacas, champiñones, pimientos, queso bajo en grasa
                """;
            case "motivational-coach" -> """
                ¡Wow, mira todo lo que has logrado! 🌟 
                
                Cada día que eliges cuidar tu alimentación es una victoria. Los desafíos que mencionas son completamente 
                normales - todos pasamos por momentos difíciles en este camino.
                
                Mi consejo: celebra cada pequeño logro. ¿Bebiste suficiente agua hoy? ¡Victoria! ¿Elegiste una opción 
                saludable para el almuerzo? ¡Otra victoria!
                
                Recuerda: no se trata de ser perfecto, se trata de ser constante. ¡Tú puedes! 💪✨
                """;
            default -> "¡Hola! Estoy aquí para ayudarte con tus objetivos de salud y fitness. ¿En qué puedo asistirte hoy?";
        };
    }
}