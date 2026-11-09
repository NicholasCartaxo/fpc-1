import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

/**
 * This class provides functionality to apply a mean filter to an image.
 * The mean filter is used to smooth images by averaging the pixel values
 * in a neighborhood defined by a kernel size.
 * 
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ImageMeanFilter.applyMeanFilter("input.jpg", "output.jpg", 3);
 * }
 * </pre>
 * 
 * <p>Supported image formats: JPG, PNG</p>
 * 
 * <p>Author: temmanuel@comptuacao.ufcg.edu.br</p>
 */
public class ImageMeanFilter {
    
    public static class ClusterWork implements Runnable {
        
        private final int id;
        private final int numThreads;
        private final int kernelSize;
        private final BufferedImage originalImage;
        private final BufferedImage filteredImage;
        private final AtomicInteger changedPixels;

        public ClusterWork(int id, int numThreads, int kernelSize, BufferedImage originalImage, BufferedImage filteredImage, AtomicInteger changedPixels){
            this.id = id;
            this.numThreads = numThreads;
            this.kernelSize = kernelSize;
            this.originalImage = originalImage;
            this.filteredImage = filteredImage;
            this.changedPixels = changedPixels;
        }
        
        @Override
        public void run() {
            int localChangedPixels = 0;
            // Image processing
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            // Process each pixel from lines associated with the id
            for (int y = id; y < height; y+=numThreads) {
                for (int x = 0; x < width; x++) {
                    // Calculate neighborhood average
                    int[] avgColor = calculateNeighborhoodAverage(originalImage, x, y, kernelSize);
                    
                    // Set filtered pixel
                    filteredImage.setRGB(x, y, 
                        (avgColor[0] << 16) | 
                        (avgColor[1] << 8)  | 
                        avgColor[2]
                    );

                    if(originalImage.getRGB(x, y) != filteredImage.getRGB(x, y)){
                        localChangedPixels++;
                    }
                }
            }
            changedPixels.addAndGet(localChangedPixels);
        }
    }


    /**
     * Applies mean filter to an image
     * 
     * @param inputPath  Path to input image
     * @param outputPath Path to output image 
     * @param kernelSize Size of mean kernel
     * @throws IOException If there is an error reading/writing
     */
    public static void applyMeanFilter(String inputPath, int numThreads, String outputPath, int kernelSize) throws IOException {
        // Load image
        BufferedImage originalImage = ImageIO.read(new File(inputPath));
        
        // Create result image
        BufferedImage filteredImage = new BufferedImage(
            originalImage.getWidth(), 
            originalImage.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );
        
        AtomicInteger changedPixels = new AtomicInteger(0);

        Thread[] threads = new Thread[numThreads];
        for(int i = 0; i<numThreads; i++){
            threads[i] = new Thread(new ClusterWork(i, numThreads, kernelSize, originalImage, filteredImage, changedPixels), "ClusterThread-"+i);
            threads[i].start();
        }
        
        for(int i = 0; i<numThreads; i++){
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Save filtered image
        ImageIO.write(filteredImage, "jpg", new File(outputPath));

        System.out.println("Total pixels changed: "+changedPixels.get());
        int totalPixels = originalImage.getWidth()*originalImage.getHeight();
        System.out.println("Total pixels unchanged: "+(totalPixels-changedPixels.get()));
    }
    
    /**
     * Calculates average colors in a pixel's neighborhood
     * 
     * @param image      Source image
     * @param centerX    X coordinate of center pixel
     * @param centerY    Y coordinate of center pixel
     * @param kernelSize Kernel size
     * @return Array with R, G, B averages
     */
    private static int[] calculateNeighborhoodAverage(BufferedImage image, int centerX, int centerY, int kernelSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        int pad = kernelSize / 2;
        
        // Arrays for color sums
        long redSum = 0, greenSum = 0, blueSum = 0;
        int pixelCount = 0;
        
        // Process neighborhood
        for (int dy = -pad; dy <= pad; dy++) {
            for (int dx = -pad; dx <= pad; dx++) {
                int x = centerX + dx;
                int y = centerY + dy;
                
                // Check image bounds
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    // Get pixel color
                    int rgb = image.getRGB(x, y);
                    
                    // Extract color components
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    
                    // Sum colors
                    redSum += red;
                    greenSum += green;
                    blueSum += blue;
                    pixelCount++;
                }
            }
        }
        
        // Calculate average
        return new int[] {
            (int)(redSum / pixelCount),
            (int)(greenSum / pixelCount),
            (int)(blueSum / pixelCount)
        };
    }
    
    /**
     * Main method for demonstration
     * 
     * Usage: java ImageMeanFilter <input_file>
     * 
     * Arguments:
     *   input_file - Path to the input image file to be processed
     *                Supported formats: JPG, PNG
     * 
     * Example:
     *   java ImageMeanFilter input.jpg
     * 
     * The program will generate a filtered output image named "filtered_output.jpg"
     * using a 7x7 mean filter kernel
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java ImageMeanFilter <input_file> <num_threads>");
            System.exit(1);
        }

        String inputFile = args[0];
        int numThreads = Integer.parseInt(args[1]);

        if(numThreads < 2){
            throw new IllegalArgumentException("<num_threads> needs to be at least 2.");
        }

        try {
            applyMeanFilter(inputFile, numThreads, "filtered_output.jpg", 7);
        } catch (IOException e) {
            System.err.println("Error processing image: " + e.getMessage());
        }
    }
}
