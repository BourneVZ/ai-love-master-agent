package com.bvz.aiagent.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PDFGenerationTool {

    private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile("!\\[([^\\]]*)\\]\\((https?://[^)\\s]+)\\)");
    private static final Pattern MARKDOWN_LINK_PATTERN = Pattern.compile("\\[([^\\]]+)]\\((https?://[^)\\s]+)\\)");
    private static final Pattern RAW_URL_PATTERN = Pattern.compile("https?://\\S+");
    private static final Pattern LOCAL_IMAGE_PATTERN = Pattern.compile("!\\[([^\\]]*)\\]\\(([^)]+\\.(png|jpg|jpeg|webp))\\)");

    @Tool(description = "根据给定内容生成 PDF 文件，支持基础 markdown 排版，并尝试把图片嵌入 PDF")
    public String generatePDF(
            @ToolParam(description = "生成后的 PDF 文件名") String fileName,
            @ToolParam(description = "需要写入 PDF 的正文内容") String content
    ) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        String tempFilePath = filePath + ".tmp";
        List<String> embeddedLocalAssets = collectEmbeddedLocalAssets(content);

        try {
            cn.hutool.core.io.FileUtil.mkdir(fileDir);

            try (PdfWriter writer = new PdfWriter(tempFilePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                document.setFont(font);
                renderMarkdownLikeContent(document, sanitizeForPdf(content, font));
            }

            Files.move(Paths.get(tempFilePath), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
            boolean readable = Files.exists(Paths.get(filePath)) && Files.size(Paths.get(filePath)) > 0;
            return JSONUtil.createObj()
                    .set("success", true)
                    .set("localPath", filePath)
                    .set("readable", readable)
                    .set("embeddedLocalAssets", embeddedLocalAssets)
                    .toString();
        } catch (Exception e) {
            cn.hutool.core.io.FileUtil.del(tempFilePath);
            cn.hutool.core.io.FileUtil.del(filePath);
            log.error("生成 PDF 失败: {}", e.getMessage(), e);
            return JSONUtil.createObj()
                    .set("success", false)
                    .set("localPath", "")
                    .set("readable", false)
                    .set("embeddedLocalAssets", embeddedLocalAssets)
                    .set("error", e.getMessage())
                    .toString();
        }
    }

    private List<String> collectEmbeddedLocalAssets(String content) {
        List<String> assets = new ArrayList<>();
        if (StrUtil.isBlank(content)) {
            return assets;
        }
        Matcher matcher = LOCAL_IMAGE_PATTERN.matcher(content);
        while (matcher.find()) {
            assets.add(matcher.group(2).trim());
        }
        return assets;
    }

    private void renderMarkdownLikeContent(Document document, String content) {
        if (StrUtil.isBlank(content)) {
            document.add(new Paragraph(""));
            return;
        }

        String[] lines = content.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i] == null ? "" : lines[i].trim();
            if (line.isEmpty()) {
                document.add(new Paragraph("").setMarginBottom(6));
                continue;
            }

            if (isTableLine(line)) {
                List<String> tableLines = new ArrayList<>();
                while (i < lines.length && isTableLine(lines[i].trim())) {
                    tableLines.add(lines[i].trim());
                    i++;
                }
                i--;
                renderTable(document, tableLines);
                continue;
            }

            if (isHorizontalRule(line)) {
                document.add(new LineSeparator(new SolidLine()).setMarginTop(6).setMarginBottom(10));
                continue;
            }

            if (line.startsWith("### ")) {
                document.add(new Paragraph(cleanInlineMarkdown(line.substring(4))).setFontSize(14).setFontColor(new DeviceGray(0.1f)).setMarginTop(8).setMarginBottom(6));
                continue;
            }
            if (line.startsWith("## ")) {
                document.add(new Paragraph(cleanInlineMarkdown(line.substring(3))).setFontSize(16).setFontColor(new DeviceGray(0.05f)).setMarginTop(10).setMarginBottom(8));
                continue;
            }
            if (line.startsWith("# ")) {
                document.add(new Paragraph(cleanInlineMarkdown(line.substring(2))).setFontSize(20).setFontColor(new DeviceGray(0.0f)).setMarginTop(4).setMarginBottom(10));
                continue;
            }

            if (line.startsWith("> ")) {
                document.add(new Paragraph(cleanInlineMarkdown(line.substring(2))).setFontColor(new DeviceGray(0.35f)).setMarginLeft(16).setMarginBottom(8));
                continue;
            }

            if (line.startsWith("- ") || line.startsWith("* ")) {
                renderBulletLine(document, line.substring(2).trim());
                continue;
            }

            renderNormalLine(document, line);
        }
    }

    private void renderBulletLine(Document document, String content) {
        Matcher imageMatcher = MARKDOWN_IMAGE_PATTERN.matcher(content);
        if (imageMatcher.matches()) {
            String altText = cleanInlineMarkdown(imageMatcher.group(1));
            String imageUrl = sanitizeUrl(imageMatcher.group(2));
            if (StrUtil.isNotBlank(altText)) {
                document.add(new Paragraph("- " + altText).setMarginLeft(12).setMarginBottom(4));
            }
            addImageFromUrl(document, imageUrl, altText);
            return;
        }

        Matcher localImageMatcher = LOCAL_IMAGE_PATTERN.matcher(content);
        if (localImageMatcher.matches()) {
            String altText = cleanInlineMarkdown(localImageMatcher.group(1));
            String imagePath = localImageMatcher.group(2).trim();
            if (StrUtil.isNotBlank(altText)) {
                document.add(new Paragraph("- " + altText).setMarginLeft(12).setMarginBottom(4));
            }
            addImageFromLocalPath(document, imagePath, altText);
            return;
        }

        document.add(new Paragraph("- " + cleanInlineMarkdown(content)).setMarginLeft(12).setMarginBottom(4));
    }

    private void renderNormalLine(Document document, String line) {
        Matcher imageMatcher = MARKDOWN_IMAGE_PATTERN.matcher(line);
        if (imageMatcher.matches()) {
            String altText = cleanInlineMarkdown(imageMatcher.group(1));
            String imageUrl = sanitizeUrl(imageMatcher.group(2));
            if (StrUtil.isNotBlank(altText)) {
                document.add(new Paragraph(altText).setFontColor(new DeviceGray(0.35f)).setMarginBottom(4));
            }
            addImageFromUrl(document, imageUrl, altText);
            return;
        }

        Matcher localImageMatcher = LOCAL_IMAGE_PATTERN.matcher(line);
        if (localImageMatcher.matches()) {
            String altText = cleanInlineMarkdown(localImageMatcher.group(1));
            String imagePath = localImageMatcher.group(2).trim();
            if (StrUtil.isNotBlank(altText)) {
                document.add(new Paragraph(altText).setFontColor(new DeviceGray(0.35f)).setMarginBottom(4));
            }
            addImageFromLocalPath(document, imagePath, altText);
            return;
        }

        String cleanedLine = cleanInlineMarkdown(line);
        Matcher rawUrlMatcher = RAW_URL_PATTERN.matcher(cleanedLine);
        if (rawUrlMatcher.matches()) {
            addImageFromUrl(document, sanitizeUrl(cleanedLine), "");
            return;
        }

        document.add(new Paragraph(cleanedLine).setMarginBottom(4));
    }

    private boolean isTableLine(String line) {
        return StrUtil.isNotBlank(line) && line.startsWith("|") && line.endsWith("|");
    }

    private boolean isHorizontalRule(String line) {
        return "---".equals(line) || "***".equals(line);
    }

    private void renderTable(Document document, List<String> tableLines) {
        if (tableLines.size() < 2) {
            tableLines.forEach(line -> document.add(new Paragraph(cleanInlineMarkdown(line))));
            return;
        }

        List<String> headers = splitTableRow(tableLines.get(0));
        if (headers.isEmpty()) {
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(headers.size())).useAllAvailableWidth();
        for (String header : headers) {
            table.addHeaderCell(new Cell().setBackgroundColor(new DeviceGray(0.9f)).add(new Paragraph(cleanInlineMarkdown(header)).setFontSize(11)));
        }

        for (int i = 2; i < tableLines.size(); i++) {
            List<String> columns = splitTableRow(tableLines.get(i));
            for (int col = 0; col < headers.size(); col++) {
                String cellText = col < columns.size() ? cleanInlineMarkdown(columns.get(col)) : "";
                table.addCell(new Cell().add(new Paragraph(cellText)));
            }
        }

        table.setMarginTop(6);
        table.setMarginBottom(10);
        document.add(table);
    }

    private List<String> splitTableRow(String row) {
        String normalized = row.trim();
        if (normalized.startsWith("|")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("|")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        String[] parts = normalized.split("\\|", -1);
        List<String> result = new ArrayList<>(parts.length);
        for (String part : parts) {
            result.add(part.trim());
        }
        return result;
    }

    private String cleanInlineMarkdown(String line) {
        if (line == null) {
            return "";
        }

        String cleaned = MARKDOWN_LINK_PATTERN.matcher(line).replaceAll("$1 ($2)");
        cleaned = cleaned.replace("**", "").replace("__", "").replace("`", "");
        return cleaned.trim();
    }

    private String sanitizeUrl(String rawUrl) {
        if (rawUrl == null) {
            return "";
        }
        String url = rawUrl.trim();
        while (url.endsWith(")") || url.endsWith("]") || url.endsWith(",") || url.endsWith(".")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    private void addImageFromUrl(Document document, String imageUrl, String altText) {
        if (StrUtil.isBlank(imageUrl)) {
            return;
        }

        try {
            byte[] imageBytes = downloadImageBytes(imageUrl);
            Image image = new Image(ImageDataFactory.create(imageBytes));
            image.setAutoScale(true);
            image.setMarginTop(4);
            image.setMarginBottom(10);
            document.add(image);
        } catch (Exception e) {
            log.warn("图片嵌入失败，保留原始链接: {}, reason={}", imageUrl, e.getMessage());
            if (StrUtil.isNotBlank(altText)) {
                document.add(new Paragraph(altText).setFontColor(new DeviceGray(0.35f)).setMarginBottom(3));
            }
            document.add(new Paragraph("图片加载失败，保留参考链接：").setFontColor(new DeviceGray(0.35f)));
            document.add(new Paragraph(imageUrl).setFontColor(new DeviceGray(0.35f)).setMarginBottom(8));
        }
    }

    private void addImageFromLocalPath(Document document, String imagePath, String altText) {
        try {
            File imageFile = resolveLocalImageFile(imagePath);
            Image image = new Image(ImageDataFactory.create(imageFile.getAbsolutePath()));
            image.setAutoScale(true);
            image.setMarginTop(4);
            image.setMarginBottom(10);
            document.add(image);
        } catch (Exception e) {
            log.warn("本地图片嵌入失败，保留原始路径: {}, reason={}", imagePath, e.getMessage());
            if (StrUtil.isNotBlank(altText)) {
                document.add(new Paragraph(altText).setFontColor(new DeviceGray(0.35f)).setMarginBottom(3));
            }
            document.add(new Paragraph("本地图片加载失败，保留参考路径：").setFontColor(new DeviceGray(0.35f)));
            document.add(new Paragraph(imagePath).setFontColor(new DeviceGray(0.35f)).setMarginBottom(8));
        }
    }

    private File resolveLocalImageFile(String imagePath) {
        Path resolvedPath = Paths.get(imagePath).normalize();
        if (resolvedPath.isAbsolute()) {
            File directFile = resolvedPath.toFile();
            if (directFile.exists()) {
                return directFile;
            }
        } else {
            Path cwdPath = Paths.get(System.getProperty("user.dir")).resolve(resolvedPath).normalize();
            File cwdFile = cwdPath.toFile();
            if (cwdFile.exists()) {
                return cwdFile;
            }

            Path downloadPath = Paths.get(System.getProperty("user.dir"))
                    .resolve("tmp")
                    .resolve("download")
                    .resolve(resolvedPath.getFileName().toString())
                    .normalize();
            File downloadFile = downloadPath.toFile();
            if (downloadFile.exists()) {
                return downloadFile;
            }
        }

        throw new IllegalArgumentException("Local image file not found: " + imagePath);
    }

    private byte[] downloadImageBytes(String imageUrl) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Accept", "image/jpeg,image/png,image/*;q=0.8,*/*;q=0.5");
        connection.setRequestProperty("Referer", "https://www.pexels.com/");
        connection.setRequestProperty("Origin", "https://www.pexels.com");
        connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");

        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return outputStream.toByteArray();
        }
    }

    private String sanitizeForPdf(String content, PdfFont font) {
        if (content == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        content.codePoints().forEach(codePoint -> {
            if (codePoint == '\n' || codePoint == '\r' || codePoint == '\t') {
                builder.appendCodePoint(codePoint);
                return;
            }

            if (!Character.isBmpCodePoint(codePoint) || Character.isISOControl(codePoint)) {
                return;
            }

            if (font.containsGlyph(codePoint)) {
                builder.appendCodePoint(codePoint);
            }
        });
        return new String(builder.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
