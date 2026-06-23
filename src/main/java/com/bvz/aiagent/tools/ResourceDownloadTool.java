package com.bvz.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class ResourceDownloadTool {

    @Tool(description = "从指定 URL 下载资源到本地，适合保存图片或附件")
    public String downloadResource(
            @ToolParam(description = "资源下载地址") String url,
            @ToolParam(description = "保存到本地的文件名") String fileName
    ) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/download";
        String filePath = fileDir + "/" + fileName;
        try {
            FileUtil.mkdir(fileDir);

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
                return JSONUtil.createObj()
                        .set("success", false)
                        .set("localPath", "")
                        .set("mimeType", contentType == null ? "" : contentType)
                        .set("size", 0)
                        .set("error", "unsupported image format returned by server")
                        .toString();
            }

            FileUtil.writeBytes(bytes, filePath);
            return JSONUtil.createObj()
                    .set("success", true)
                    .set("localPath", filePath)
                    .set("mimeType", contentType == null ? "" : contentType)
                    .set("size", bytes.length)
                    .toString();
        } catch (Exception e) {
            return JSONUtil.createObj()
                    .set("success", false)
                    .set("localPath", "")
                    .set("mimeType", "")
                    .set("size", 0)
                    .set("error", e.getMessage())
                    .toString();
        }
    }

    private boolean isSupportedRasterImage(byte[] bytes, String contentType) {
        if (bytes == null || bytes.length < 12) {
            return false;
        }

        String normalizedContentType = contentType == null ? "" : contentType.toLowerCase();
        if (StrUtil.containsAny(normalizedContentType, "image/jpeg", "image/jpg", "image/png")) {
            return true;
        }
        if (StrUtil.isNotBlank(normalizedContentType) && !normalizedContentType.startsWith("image/")) {
            return false;
        }
        if (StrUtil.containsAny(normalizedContentType, "image/avif", "image/webp")) {
            return false;
        }

        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return true;
        }
        if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return true;
        }

        String boxType = new String(bytes, 4, 8);
        return !StrUtil.containsAnyIgnoreCase(boxType, "ftypavif", "ftypmif1", "ftypheic", "ftypheif", "WEBP");
    }
}
