package com.diploma.verivicationdipllom.service;

import com.diploma.verivicationdipllom.domain.dto.CustomMultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MultipartFileConverterService {

    public List<MultipartFile> extractFrames(MultipartFile videoFile) throws IOException, InterruptedException {
        // Создаем временный файл для хранения загруженного видео
        File tempVideoFile = File.createTempFile("tempVideo", ".tmp");
        try (InputStream in = videoFile.getInputStream(); FileOutputStream out = new FileOutputStream(tempVideoFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        }

        // Создаем директорию для выходных кадров
        File outputDirectory = new File(tempVideoFile.getParent(), "frames");
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        // Выполняем команду FFmpeg для извлечения кадров
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-i", tempVideoFile.getAbsolutePath(),
                "-vf", "fps=1",  // Извлечение 1 кадра в секунду
                outputDirectory.getAbsolutePath() + "/frame%d.png"  // Выходные файлы
        );
        Process process = processBuilder.start();
        process.waitFor();

        // Удаляем временный файл
        tempVideoFile.delete();

        // Сборка списка файлов из выходной директории и преобразование в MultipartFile
        File[] files = outputDirectory.listFiles((d, name) -> name.endsWith(".png"));
        List<MultipartFile> multipartFiles = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                try (FileInputStream inputStream = new FileInputStream(file)) {
                    MultipartFile multipartFile = new CustomMultipartFile(
                            file.getName(),  // Name
                            file.getName(),  // Original Filename
                            "image/jpeg",  // Content Type
                            inputStream.readAllBytes()
                    );
                    multipartFiles.add(multipartFile);
                }
                // Удалите файлы после использования, если это необходимо
                file.delete();
            }
        }
        // Удаляем директорию после использования
        outputDirectory.delete();
        return multipartFiles;
    }




//    public static List<File> extractFrames(MultipartFile videoFile, String outputDir) throws IOException, InterruptedException {
//        // Создаем временные файлы для хранения загруженного видео
//        File tempVideoFile = File.createTempFile("tempVideo", ".tmp");
//        try (FileOutputStream out = new FileOutputStream(tempVideoFile)) {
//            InputStream in = videoFile.getInputStream();
//            byte[] buffer = new byte[1024];
//            int length;
//            while ((length = in.read(buffer)) != -1) {
//                out.write(buffer, 0, length);
//            }
//        }
//
//        // Создаем директорию для выходных кадров
//        File outputDirectory = new File(outputDir);
//        if (!outputDirectory.exists()) {
//            outputDirectory.mkdirs();
//        }
//
//        // Выполняем команду FFmpeg для извлечения кадров
//        ProcessBuilder processBuilder = new ProcessBuilder(
//                "ffmpeg",
//                "-i", tempVideoFile.getAbsolutePath(),
//                "-vf", "fps=1",  // Извлечение 1 кадра в секунду
//                outputDir + "/frame%d.png"  // Выходные файлы
//        );
//        Process process = processBuilder.start();
//        process.waitFor();
//
//        // Удаляем временный файл
//        tempVideoFile.delete();
//
//        // Сборка списка файлов из выходной директории
//        File[] files = outputDirectory.listFiles((d, name) -> name.endsWith(".png"));
//        List<File> imageFiles = new ArrayList<>();
//        if (files != null) {
//            for (File file : files) {
//                imageFiles.add(file);
//            }
//        }
//        return imageFiles;
//    }
//    public static List<MultipartFile> convertFilesToMultipartFiles(List<File> files) throws IOException {
//        List<MultipartFile> multipartFiles = new ArrayList<>();
//        for (File file : files) {
//            try (FileInputStream inputStream = new FileInputStream(file)) {
//                MultipartFile multipartFile = new CustomMultipartFile(
//                        file.getName(),  // Name
//                        file.getName(),  // Original Filename
//                        "image/jpeg",  // Content Type
//                        inputStream.readAllBytes()
//                );
//                multipartFiles.add(multipartFile);
//            }
//        }
//        return multipartFiles;
//    }

//    public static List<MultipartFile> convertImagesToMultipartFiles(String dirPath) throws IOException {
//        File dir = new File(dirPath);
//        File[] files = dir.listFiles((d, name) -> name.endsWith(".png"));
//        List<MultipartFile> multipartFiles = new ArrayList<>();
//
//        if (files != null) {
//            for (File file : files) {
//                try (FileInputStream inputStream = new FileInputStream(file)) {
//                    multipartFiles.add(new CustomMultipartFile(
//                            file.getName(),  // Name
//                            file.getName(),  // Original Filename
//                            "image/jpeg",  // Content Type
//                            inputStream.readAllBytes()
//                    ));
//                }
//            }
//        }
//        return multipartFiles;
//    }
}
