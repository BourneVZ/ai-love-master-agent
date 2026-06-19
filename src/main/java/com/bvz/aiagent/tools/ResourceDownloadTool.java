package com.bvz.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

public class ResourceDownloadTool {

    @Tool(description = "从指定 URL 下载资源到本地，适合保存图片、附件等文件")
    public String downloadResource(@ToolParam(description = "资源下载地址") String url,
                                   @ToolParam(description = "保存到本地的文件名") String fileName) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/download";
        String filePath = fileDir + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);

            // 这里明确偏向 jpeg/png，避免 CDN 按协商内容返回 AVIF/WebP，
            // 导致“文件后缀是 jpg，但内容实际上不是常规 jpg/png”的问题。
            HttpResponse response = HttpRequest.get(url)
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Accept", "image/jpeg,image/png,image/*;q=0.8,*/*;q=0.5")
                    .header("Referer", "https://www.pexels.com/")
                    .header("Origin", "https://www.pexels.com")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .timeout(20000)
                    .execute();

            byte[] bytes = response.bodyBytes();
            String contentType = response.header("Content-Type");
            if (!isSupportedRasterImage(bytes, contentType)) {
                return "Error downloading resource: unsupported image format returned by server, contentType="
                        + contentType;
            }

            FileUtil.writeBytes(bytes, filePath);
            return "Resource downloaded successfully to: " + filePath;
        } catch (Exception e) {
            return "Error downloading resource: " + e.getMessage();
        }
    }

    /**
     * 只接受当前项目链路里稳定可消费的常规位图格式。
     * AVIF 虽然也是图片，但当前 IDE / iText / 本地查看链路都不稳定，先显式拦掉。
     */
    private boolean isSupportedRasterImage(byte[] bytes, String contentType) {
        if (bytes == null || bytes.length < 12) {
            return false;
        }

        String normalizedContentType = contentType == null ? "" : contentType.toLowerCase();
        if (StrUtil.containsAny(normalizedContentType, "image/jpeg", "image/jpg", "image/png")) {
            return true;
        }
        if (StrUtil.containsAny(normalizedContentType, "image/avif", "image/webp")) {
            return false;
        }

        // JPEG magic number: FF D8 FF
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return true;
        }
        // PNG magic number: 89 50 4E 47 0D 0A 1A 0A
        if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return true;
        }

        // AVIF / HEIF family usually starts with ftypxxxx in bytes 4-11.
        String boxType = new String(bytes, 4, 8);
        return !StrUtil.containsAnyIgnoreCase(boxType, "ftypavif", "ftypmif1", "ftypheic", "ftypheif", "WEBP");
    }
}
