package com.multimodal.interview.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文件处理工具类。
 */
public class FileUtil {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final String[] ALLOWED_EXTENSIONS = {".pdf", ".doc", ".docx"};

    private FileUtil() {
    }

    /**
     * 校验上传文件是否合法。
     *
     * @param file 上传文件
     * @return 合法时返回 {@code true}
     */
    public static boolean isValidFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return false;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        for (String allowedExtension : ALLOWED_EXTENSIONS) {
            if (allowedExtension.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 提取简历文件中的文本内容。
     *
     * @param file 上传文件
     * @return 提取后的文本
     * @throws IOException 文件读取异常
     */
    public static String extractText(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("文件名不能为空");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        try (InputStream inputStream = file.getInputStream()) {
            return switch (extension) {
                case ".pdf" -> extractTextFromPdf(inputStream);
                case ".docx" -> extractTextFromDocx(inputStream);
                case ".doc" -> extractTextFromDoc(inputStream);
                default -> throw new IOException("不支持的文件格式");
            };
        }
    }

    private static String extractTextFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private static String extractTextFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            try (XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                return extractor.getText();
            }
        }
    }

    private static String extractTextFromDoc(InputStream inputStream) throws IOException {
        try (HWPFDocument document = new HWPFDocument(inputStream)) {
            try (WordExtractor extractor = new WordExtractor(document)) {
                return extractor.getText();
            }
        }
    }
}
