package com.smart_extract_service.modules.catalog.application.usecases;

import com.smart_extract_service.modules.catalog.application.dto.AiEnrichmentResponse;
import com.smart_extract_service.modules.catalog.domain.ports.output.CatalogItemRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ============================================================
 * Serviço que orquestra as chamadas de IA para o Catálogo.
 * ============================================================
 * <p>
 * CONCEITO CHAVE: @Qualifier
 * Como temos DOIS beans do tipo ChatClient na configuração,
 * precisamos dizer ao Spring QUAL deles injetar em cada método.
 * <p>
 * ARQUITETURA MULTI-MODELO:
 * → Qwen 2.5 7B: Tarefas complexas (geração de texto, enriquecimento)
 * → Phi 3.5 Mini: Tarefas simples (classificação, extração)
 * <p>
 * SEGURANÇA:
 * → @Transactional garante que o DatabaseTenantAspect funcione
 * corretamente (SET LOCAL app.current_tenant dentro de transação)
 * ============================================================
 */
@Slf4j
@Service
@Transactional
public class CatalogAiService {

    // ═══════════════════════════════════════════════════════════
    // DEPENDÊNCIAS
    // ═══════════════════════════════════════════════════════════

    /**
     * ChatClient do Qwen 2.5 (tarefas complexas)
     */
    private final ChatClient qwenClient;

    /**
     * ChatClient do Phi 3.5 (tarefas simples)
     */
    private final ChatClient phiClient;

    /**
     * Repository para validar unicidade de SKU no banco
     */
    private final CatalogItemRepositoryPort repository;

    /**
     * Construtor com injeção qualificada.
     *
     * @param qwenClient Bean "qwenChatClient" da configuração
     * @param phiClient  Bean "phiChatClient" da configuração
     * @param repository Repository de catálogo (para validação de SKU)
     */
    public CatalogAiService(@Qualifier("qwenChatClient") ChatClient qwenClient, @Qualifier("phiChatClient") ChatClient phiClient, CatalogItemRepositoryPort repository) {
        this.qwenClient = qwenClient;
        this.phiClient = phiClient;
        this.repository = repository;
    }

    // ═══════════════════════════════════════════════════════════
    // 1. TAREFA COMPLEXA: Enriquecimento de Produto (Usa Qwen)
    // ══════════════════════════════════════════════════════════

    /**
     * Enriquece dados de um produto usando IA.
     * <p>
     * FLUXO:
     * 1. Recebe nome e descrição crua do produto
     * 2. Envia para Qwen 2.5 com prompt estruturado
     * 3. Qwen retorna JSON com categoria, tags e descrição aprimorada
     * 4. Spring AI converte JSON para objeto Java (AiEnrichmentResponse)
     * <p>
     * ★ CORREÇÃO IMPORTANTE:
     * Usamos {{ e }} no prompt para escapar as chaves do JSON.
     * O Spring AI usa StringTemplate por trás, que interpreta { }
     * como variáveis. Para ter chaves literais no texto, duplicamos.
     *
     * @param productName    Nome do produto
     * @param rawDescription Descrição crua
     * @return Dados enriquecidos (categoria, tags, descrição)
     */
    public AiEnrichmentResponse enrichProduct(String productName, String rawDescription) {
        String safeName  = (productName == null || productName.isBlank())
                ? "Sem nome" : productName.trim();
        String safeNotes = (rawDescription == null || rawDescription.isBlank())
                ? "Nenhuma anotação" : rawDescription.trim();

        log.info("🧠 [QWEN] Enriquecendo produto: '{}'", safeName);

        boolean semAnotacoes = "Nenhuma anotação".equals(safeNotes);
        String contextInstruction = semAnotacoes
                ? "O usuário forneceu APENAS o nome. Gere uma descrição técnica mínima baseada somente "
                + "no nome — NÃO invente especificações (voltagem, potência, medidas, cor, material)."
                : "O usuário forneceu anotações. Reescreva de forma profissional, preservando TODAS as "
                + "especificações e explicitando TERMOS DE BUSCA e CASOS DE USO.";

        String prompt = """
            ## PAPEL
            Você é um especialista sênior em SEO, e-commerce e cadastro de produtos para ERP.
            Sua tarefa é transformar informações brutas em uma ficha de produto otimizada para buscas.

            ## ENTRADA
            Nome do Produto: %s
            Anotações do Usuário: %s
            Contexto Adicional: %s

            ## OBJETIVO
            Gerar metadados estruturados (categoria, tags e descrição otimizada) que maximizem a
            descoberta do produto em buscas internas e externas, mantendo fidelidade total aos
            dados fornecidos.

            ## REGRAS DE OURO (obrigatórias)
            1. NUNCA invente especificações, medidas, materiais, potência, cor, voltagem ou
               benefícios que não estejam explícitos na entrada. Se faltar dado, omita — não
               preencha com suposições.
            2. Use o nome do produto como âncora para inferir a categoria correta. Em caso de
               ambiguidade, escolha a categoria mais ampla e segura.
            3. Se as anotações estiverem vazias ou insuficientes, produza uma descrição curta
               baseada apenas no nome — sem inventar atributos.
            4. Preserve 100%% das especificações técnicas fornecidas (números, medidas,
               materiais, cor).

            ## REGRAS DA DESCRIÇÃO (enhancedDescription)
            - Idioma: Português do Brasil.
            - Extensão: 3 a 4 frases curtas (máx. ~350 caracteres).
            - Tom: técnico-comercial, objetivo, sem superlativos vazios ("o melhor", "incrível").
            - Primeira frase = resumo do produto (útil como snippet de busca).
            - Inclua naturalmente: tipo do item, categoria implícita, palavras-chave de intenção
              de busca (ex: "fritar sem óleo", "cozinha saudável", "preparo rápido") e as
              especificações técnicas fornecidas pelo usuário.
            - NÃO use markdown, listas, emojis, aspas extras ou quebras de linha.

            ## REGRAS DAS TAGS
            - 3 a 6 tags em minúsculas, sem acentos quando possível, cada uma com 1 a 3 palavras.
            - Mix de: tipo do produto, atributo técnico relevante e intenção de uso.
            - Não repita a categoria literalmente.

            ## SAÍDA
            Retorne APENAS um objeto JSON válido, sem markdown, sem comentários, exatamente neste
            schema:
            {{
              "category": "string",
              "tags": ["string", "string", "string"],
              "enhancedDescription": "string"
            }}

            ## EXEMPLO POSITIVO
            Entrada: Nome="Air Fryer 4L", Anotações="cesto antiaderente, timer 30min, 1400w, preta"
            Saída:
            {{
              "category": "Eletroportáteis",
              "tags": ["air fryer", "fritadeira eletrica", "cozinha saudavel", "1400w"],
              "enhancedDescription": "Fritadeira elétrica Air Fryer de 4 litros para fritar alimentos sem óleo na cozinha. Possui cesto antiaderente, timer de 30 minutos e potência de 1400w. Cor preta, ideal para preparo rápido e refeições saudáveis no dia a dia."
            }}

            ## EXEMPLO NEGATIVO (NÃO FAÇA)
            - Inventar "voltagem 220V" quando o usuário não informou.
            - Escrever "O melhor air fryer do mercado, perfeito para sua família!" (superlativo
              vazio + suposição).
            - Devolver texto fora do JSON, ou usar ```json no output.

            ## TAREFA
            Gere o JSON agora.
            """.formatted(safeName, safeNotes, contextInstruction);

        AiEnrichmentResponse response = qwenClient
                .prompt()
                .user(prompt)
                .call()
                .entity(AiEnrichmentResponse.class);

        if (response == null) {
            log.error("❌ [QWEN] Resposta nula do modelo para produto '{}'", safeName);
            throw new IllegalStateException("Resposta vazia do modelo ao enriquecer produto: " + safeName);
        }

        log.info("✅ [QWEN] Produto enriquecido: categoria={}, tags={}",
                response.category(), response.tags());
        return response;
    }

    // ═══════════════════════════════════════════════════════════
    // 2. TAREFA SIMPLES: Classificação de Produto (Usa Phi)
    // ═══════════════════════════════════════════════════════════

    /**
     * Classifica um produto como "PRODUCT" ou "SERVICE".
     * <p>
     * POR QUE PHI?
     * → Tarefa binária simples (só 2 opções)
     * → Não precisa de criatividade
     * → Phi é 3x mais rápido que Qwen para isso
     *
     * @param productName Nome do produto
     * @return "PRODUCT" ou "SERVICE"
     */
    public String classifyProductType(String productName) {
        log.info("⚡ [PHI] Classificando tipo do produto: '{}'", productName);

        String prompt = """
                Classifique o item abaixo como PRODUCT (produto físico) ou SERVICE (serviço).
                Responda APENAS com uma das duas palavras: PRODUCT ou SERVICE.
                
                Item: %s
                """.formatted(productName != null ? productName : "");

        // .content() retorna apenas o texto puro da resposta
        String response = phiClient.prompt().user(prompt).call().content().trim().toUpperCase();

        // Segurança: Se a IA alucinar, temos um fallback
        if (!response.equals("PRODUCT") && !response.equals("SERVICE")) {
            log.warn("⚠️ [PHI] Resposta inesperada: '{}'. Usando PRODUCT como fallback.", response);
            return "PRODUCT";
        }

        log.info("✅ [PHI] Classificado como: {}", response);
        return response;
    }

    // ═══════════════════════════════════════════════════════════
    // 3. TAREFA SIMPLES: Extração de SKU (Usa Phi)
    // ═══════════════════════════════════════════════════════════

    /**
     * Extrai um possível SKU de um texto livre.
     * <p>
     * ★ CORREÇÃO IMPORTANTE:
     * Usamos Matcher.find() em vez de String.matches().
     * <p>
     * DIFERENÇA:
     * - matches(): exige que a string INTEIRA corresponda ao padrão
     * "PRD-00123" → true
     * "O SKU é PRD-00123" → false ❌
     * <p>
     * - find(): busca o padrão em QUALQUER PARTE da string
     * "PRD-00123" → true ✅
     * "O SKU é PRD-00123" → true ✅
     * "PRD-00123\n" → true ✅
     * <p>
     * EXEMPLO:
     * Entrada: "Adicionar o produto PRD-00123 ao carrinho"
     * Saída: "PRD-00123"
     *
     * @param text Texto livre
     * @return SKU extraído ou null se não encontrado
     */
    public String extractSku(String text) {
        log.info(" [PHI] Extraindo SKU de: '{}'", text);

        String prompt = """
                Extraia o código SKU do texto abaixo.
                
                Regras:
                1. SKU tem formato: 3 letras MAIÚSCULAS, hífen, 5 números
                2. Exemplo válido: PRD-00123, SRV-00042, CAT-99999
                3. Responda APENAS com o SKU, sem explicações
                4. Se não houver SKU válido, responda: NENHUM
                
                Texto: %s
                
                SKU:
                """.formatted(text != null ? text : "");

        String response = phiClient.prompt().user(prompt).call().content().trim().toUpperCase();

        log.info("🔍 [PHI] Resposta raw da IA: '{}'", response);

        if ("NENHUM".equals(response) || response.isEmpty()) {
            log.info("⚠️ [PHI] IA respondeu NENHUM ou vazio");
            return null;
        }

        // ★ CORREÇÃO: Usar Matcher.find() para busca parcial
        // Isso permite extrair o SKU mesmo se a IA retornar texto extra
        Matcher matcher = Pattern.compile("[A-Z]{3}-\\d{5}").matcher(response);

        if (matcher.find()) {
            String sku = matcher.group();
            log.info("✅ [PHI] SKU extraído com sucesso: {}", sku);
            return sku;
        }

        log.warn("⚠️ [PHI] Nenhum SKU válido encontrado na resposta: '{}'", response);
        return null;
    }

    // ═══════════════════════════════════════════════════════════
    // 4. TAREFA COMPOSTA: Sugestão de SKU Único (IA + Banco)
    // ═══════════════════════════════════════════════════════════

    /**
     * Sugere um SKU baseado no nome do produto e VALIDA unicidade no banco.
     * <p>
     * POR QUE ESSE MÉTODO EXISTE?
     * → A IA NÃO TEM ACESSO AO BANCO DE DADOS
     * → Ela pode sugerir um SKU que já existe (alucinação)
     * → O backend precisa validar e, se necessário, ajustar
     * <p>
     * FLUXO:
     * 1. IA sugere um SKU no formato correto (ex: "CAF-00001")
     * 2. Backend verifica se já existe no banco (via repository.existsSku)
     * 3. Se existir: tenta "CAF-00002", "CAF-00003"... até achar um único
     * 4. Retorna o SKU único garantido
     *
     * @param productName Nome do produto (para a IA gerar prefixo relevante)
     * @param tenantId    ID do tenant (para isolamento multi-tenant)
     * @return SKU único e válido, pronto para ser salvo
     * @throws RuntimeException se não conseguir SKU único após 10 tentativas
     */
    public String suggestUniqueSku(String productName, java.util.UUID tenantId) {
        log.info("🎯 Sugerindo SKU único para: '{}'", productName);

        // 1. Pede para a IA sugerir um SKU baseado no nome
        String prompt = """
                Gere um código SKU para o produto abaixo.
                
                Regras:
                1. Formato: 3 letras MAIÚSCULAS + hífen + 5 números
                2. As 3 letras devem ser iniciais do nome (ex: Café → CAF, Notebook → NOT)
                3. Use números entre 00001 e 99999
                4. Responda APENAS com o SKU, sem explicações
                
                Produto: %s
                
                SKU:
                """.formatted(productName != null ? productName : "produto");

        String suggestedSku = phiClient.prompt().user(prompt).call().content().trim().toUpperCase();

        log.info(" [PHI] SKU sugerido pela IA: '{}'", suggestedSku);

        // 2. Valida formato com Regex
        if (!suggestedSku.matches("[A-Z]{3}-\\d{5}")) {
            log.warn("⚠️ SKU sugerido tem formato inválido '{}', gerando fallback", suggestedSku);
            suggestedSku = generateFallbackSku(productName);
        }

        // 3. Verifica unicidade no banco (loop de retry)
        int attempts = 0;
        String finalSku = suggestedSku;

        while (repository.existsSku(tenantId, finalSku, null) && attempts < 10) {
            log.info("🔄 SKU {} já existe no banco, tentando próximo...", finalSku);

            // Incrementa o número do SKU (ex: CAF-00001 → CAF-00002)
            String[] parts = finalSku.split("-");
            int number = Integer.parseInt(parts[1]) + 1;
            finalSku = parts[0] + "-" + String.format("%05d", number);
            attempts++;
        }

        if (attempts >= 10) {
            throw new RuntimeException("Não foi possível gerar SKU único após 10 tentativas para: " + productName);
        }

        log.info("✅ SKU único garantido: {}", finalSku);
        return finalSku;
    }

    /**
     * Gera SKU fallback quando a IA falha ou retorna formato inválido.
     * <p>
     * ESTRATÉGIA:
     * → Pega as 3 primeiras letras do nome (ou "PRD" se não houver)
     * → Adiciona número aleatório entre 10000 e 99999
     *
     * @param productName Nome do produto
     * @return SKU no formato "XXX-NNNNN"
     */
    private String generateFallbackSku(String productName) {
        // Pega as 3 primeiras letras, remove caracteres não-alfabéticos
        String prefix = "PRD"; // default

        if (productName != null && productName.length() >= 3) {
            String clean = productName.toUpperCase().replaceAll("[^A-Z]", "")  // remove espaços, acentos, etc.
                    .replaceAll("[ÁÀÂÃ]", "A").replaceAll("[ÉÈÊ]", "E").replaceAll("[ÍÌÎ]", "I").replaceAll("[ÓÒÔÕ]", "O").replaceAll("[ÚÙÛ]", "U");

            if (clean.length() >= 3) {
                prefix = clean.substring(0, 3);
            }
        }

        int random = new Random().nextInt(90000) + 10000; // 10000-99999
        return prefix + "-" + random;
    }
}