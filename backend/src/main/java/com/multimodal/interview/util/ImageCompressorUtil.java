package com.multimodal.interview.util;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * 图片压缩工具类。
 */
public class ImageCompressorUtil {

    private ImageCompressorUtil() {
    }

    /**
     * 压缩上传图片。
     *
     * @param file 原始图片文件
     * @return 压缩后的文件对象
     * @throws IOException 图片处理异常
     */
    public static MultipartFile compressImage(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        String formatName = getFormatName(contentType);

        BufferedImage image = ImageIO.read(file.getInputStream());
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        double scaleFactor = 0.9;
        int targetWidth = (int) (originalWidth * scaleFactor);
        int targetHeight = (int) (originalHeight * scaleFactor);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean isJpeg = formatName.equalsIgnoreCase("JPEG") || formatName.equalsIgnoreCase("JPG");

        while (true) {
            BufferedImage resizedImage = resizeImage(image, targetWidth, targetHeight);

            baos.reset();
            if (isJpeg) {
                writeJpeg(resizedImage, baos, 0.8f);
            } else {
                writeImage(resizedImage, formatName, baos);
            }

            if (baos.size() <= 800 * 1024 || targetWidth < 100 || targetHeight < 100) {
                break;
            }

            scaleFactor *= 0.9;
            targetWidth = (int) (targetWidth * scaleFactor);
            targetHeight = (int) (targetHeight * scaleFactor);
        }

        InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
        return new CustomMultipartFile(
                inputStream,
                file.getName(),
                file.getOriginalFilename(),
                contentType,
                baos.size()
        );
    }

    /**
     * 按指定尺寸缩放图片。
     */
    private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, getImageType(originalImage));
        Graphics2D g2d = resizedImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(originalImage, 0, 0, width, height, null);
        g2d.dispose();
        return resizedImage;
    }

    /**
     * 以指定质量写入 JPEG 图片。
     */
    private static void writeJpeg(BufferedImage image, ByteArrayOutputStream baos, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("JPEG");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPEG writer found");
        }

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        }
        writer.dispose();
    }

    /**
     * 写入非 JPEG 图片。
     */
    private static void writeImage(BufferedImage image, String formatName, ByteArrayOutputStream baos) throws IOException {
        ImageIO.write(image, formatName, baos);
    }

    /**
     * 根据内容类型推断图片格式。
     */
    private static String getFormatName(String contentType) {
        if (contentType == null) {
            return "JPEG";
        }
        if (contentType.contains("png")) {
            return "PNG";
        }
        if (contentType.contains("jpeg") || contentType.contains("jpg")) {
            return "JPEG";
        }
        if (contentType.contains("gif")) {
            return "GIF";
        }
        return "JPEG";
    }

    /**
     * 根据原图是否存在透明通道确定目标图片类型。
     */
    private static int getImageType(BufferedImage originalImage) {
        if (originalImage.getColorModel().hasAlpha()) {
            return BufferedImage.TYPE_INT_ARGB;
        }
        return BufferedImage.TYPE_INT_RGB;
    }

    /**
     * 基于内存字节流的 MultipartFile 实现。
     */
    static class CustomMultipartFile implements MultipartFile {

        private final InputStream inputStream;
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final long size;

        CustomMultipartFile(InputStream inputStream, String name, String originalFilename,
                            String contentType, long size) {
            this.inputStream = inputStream;
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.size = size;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return size == 0;
        }

        @Override
        public long getSize() {
            return size;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return inputStream.readAllBytes();
        }

        @Override
        public InputStream getInputStream() {
            return inputStream;
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            try (java.io.OutputStream outputStream = new java.io.FileOutputStream(dest)) {
                outputStream.write(getBytes());
            }
        }
    }
}
