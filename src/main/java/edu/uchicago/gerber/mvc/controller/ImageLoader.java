package edu.uchicago.gerber._08final.mvc.controller;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;
import java.util.Map;

/*
Place all .png image assets in this directory src/main/resources/imgs or its subdirectories.
 */
public class ImageLoader {

    public static final Map<String, BufferedImage> IMAGES;
    //load all images prior to runtime in the static context
    static {
        Path rootDirectory = Paths.get("src/main/resources/imgs");
        Map<String, BufferedImage> localMap = null;
        try {
            localMap = loadPngImages(rootDirectory);
        } catch (IOException e) {
            e.fillInStackTrace();
        }
        IMAGES = localMap;
    }

    private static Map<String, BufferedImage> loadPngImages(Path rootDirectory) throws IOException {
        Map<String, BufferedImage> pngImages = new HashMap<>();
        Files.walkFileTree(rootDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().toLowerCase().endsWith(".png")) {
                    try {
                        BufferedImage bufferedImage = ImageIO.read(file.toFile());
                        if (bufferedImage != null) {
                            pngImages.put(file.getFileName().toString(), bufferedImage);
                        }
                    } catch (IOException e) {
                        e.fillInStackTrace();
                    }
                }
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // Handle the error here if necessary
                return FileVisitResult.CONTINUE;
            }
        });
        return pngImages;
    }
}
