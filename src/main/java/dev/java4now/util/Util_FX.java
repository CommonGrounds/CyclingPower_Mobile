package dev.java4now.util;

import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Services;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

public class Util_FX {

    private static final Logger LOGGER = LoggerFactory.getLogger(Util_FX.class);

    public static File resizeImage(File originalFile, int targetWidth, int targetHeight) throws IOException {
        Image originalImage = new Image(originalFile.toURI().toString());
        LOGGER.debug("Original image: " + originalImage.getWidth() + "x" + originalImage.getHeight());

        double scale = Math.min((double) targetWidth / originalImage.getWidth(), (double) targetHeight / originalImage.getHeight());
        int newWidth = (int) (originalImage.getWidth() * scale);
        int newHeight = (int) (originalImage.getHeight() * scale);

        ImageView imageView = new ImageView(originalImage);
        imageView.setFitWidth(newWidth);
        imageView.setFitHeight(newHeight);
        imageView.setSmooth(true);
        imageView.setPreserveRatio(true);

        SnapshotParameters params = new SnapshotParameters();
        WritableImage resizedImage = new WritableImage(newWidth, newHeight);
        imageView.snapshot(params, resizedImage);

//        File resizedFile = new File("resized_" + originalFile.getName().replace(".jpg", ".png"));
        File resizedFile = new File(Services.get(StorageService.class)
                .flatMap(StorageService::getPrivateStorage)
                .orElseThrow(() -> new IOException("No storage"))
                .getPath() + "/resized_" + originalFile.getName().replace(".jpg", ".png"));

        saveAsPng(resizedImage, resizedFile);

        LOGGER.debug("Resized file: " + resizedFile.getPath() + ", " + resizedFile.length() + " bytes");
        return resizedFile;
    }



    //-----------------------------------------------------------------
    private static void saveAsPng(WritableImage image, File outputFile) throws IOException {
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
}