package dev.java4now.util;

import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Services;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javafx.scene.image.PixelReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import static dev.java4now.model.CyclingRecorder.localDir;

public class ImageUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class);

    // New method to handle camera image resizing
    public static File resizeCameraImage(Image image, String tempPrefix, int targetWidth, int targetHeight) {
        try {
            // Create temporary file for the original image
            File tempFile = new File(localDir, tempPrefix + System.currentTimeMillis() + ".png");

            // Convert Image to PNG file
            saveImageAsPng(image, tempFile);
            LOGGER.debug("Temp file: " + tempFile.getPath() + ", " + tempFile.length() + " bytes");

            // Resize using existing Util_FX method
            File resizedFile = Util_FX.resizeImage(tempFile, targetWidth, targetHeight);
            LOGGER.debug("Resized file: " + resizedFile.getPath() + ", " + resizedFile.length() + " bytes");

            // Delete the original file after resizing
            deleteFile(tempFile);

            return resizedFile;

        } catch (IOException e) {
            throw new RuntimeException("Failed to resize camera image", e);
        }
    }

    // Helper method to save Image as PNG (adapted from Util_FX)
    private static void saveImageAsPng(Image image, File outputFile) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader reader = image.getPixelReader();

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            // PNG signature
            byte[] signature = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
            fos.write(signature);

            // IHDR chunk
            ByteBuffer ihdr = ByteBuffer.allocate(13);
            ihdr.putInt(width);
            ihdr.putInt(height);
            ihdr.put((byte) 8); // Bit depth
            ihdr.put((byte) 2); // Color type (RGB)
            ihdr.put((byte) 0); // Compression method
            ihdr.put((byte) 0); // Filter method
            ihdr.put((byte) 0); // Interlace method
            writeChunk(fos, "IHDR", ihdr.array());

            // IDAT chunk
            byte[] pixelData = new byte[width * height * 3 + height]; // RGB + filter byte per row
            int offset = 0;
            for (int y = 0; y < height; y++) {
                pixelData[offset++] = 0; // Filter type: none
                for (int x = 0; x < width; x++) {
                    int argb = reader.getArgb(x, y);
                    pixelData[offset++] = (byte) ((argb >> 16) & 0xFF); // R
                    pixelData[offset++] = (byte) ((argb >> 8) & 0xFF);  // G
                    pixelData[offset++] = (byte) (argb & 0xFF);        // B
                }
            }
            Deflater deflater = new Deflater();
            deflater.setInput(pixelData);
            deflater.finish();
            byte[] compressed = new byte[pixelData.length];
            int compressedLength = deflater.deflate(compressed);
            writeChunk(fos, "IDAT", compressed, 0, compressedLength);

            // IEND chunk
            writeChunk(fos, "IEND", new byte[0]);
        }
    }

    private static void writeChunk(FileOutputStream fos, String type, byte[] data) throws IOException {
        writeChunk(fos, type, data, 0, data.length);
    }

    private static void writeChunk(FileOutputStream fos, String type, byte[] data, int off, int len) throws IOException {
        ByteBuffer chunk = ByteBuffer.allocate(12 + len); // 4 (len) + 4 (type) + len (data) + 4 (CRC)
        chunk.putInt(len);
        chunk.put(type.getBytes("ASCII"));
        chunk.put(data, off, len);
        CRC32 crc = new CRC32();
        crc.update(type.getBytes("ASCII"));
        crc.update(data, off, len);
        chunk.putInt((int) crc.getValue());
        fos.write(chunk.array());
    }


    // Helper method to delete a file
    public static void deleteFile(File file) {
        if (file.exists()) {
            if (file.delete()) {
                LOGGER.debug("Deleted file: " + file.getPath());
            } else {
                System.err.println("Failed to delete file: " + file.getPath());
            }
        } else {
            LOGGER.warn("File does not exist: " + file.getPath());
        }
    }
}
