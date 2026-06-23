package com.bvz.aiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.List;

@Configuration
@Slf4j
public class LoveAppVectorStoreConfig {

    private static final String VECTOR_STORE_CACHE_PATH = "tmp/vector-store/love-app-simple-vector-store.json";

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        File cacheFile = new File(System.getProperty("user.dir"), VECTOR_STORE_CACHE_PATH);

        if (cacheFile.isFile()) {
            try {
                simpleVectorStore.load(cacheFile);
                log.info("Loaded cached love-app vector store from {}", cacheFile.getAbsolutePath());
                return simpleVectorStore;
            } catch (Exception e) {
                log.warn("Failed to load cached love-app vector store, rebuilding: {}", e.getMessage());
            }
        }

        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
//        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documents);
        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(documents);

        simpleVectorStore.add(enrichedDocuments);

        File parentDir = cacheFile.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            log.warn("Failed to create vector store cache directory: {}", parentDir.getAbsolutePath());
            return simpleVectorStore;
        }
        try {
            simpleVectorStore.save(cacheFile);
            log.info("Saved love-app vector store cache to {}", cacheFile.getAbsolutePath());
        } catch (Exception e) {
            log.warn("Failed to save love-app vector store cache: {}", e.getMessage());
        }
        return simpleVectorStore;
    }
}
