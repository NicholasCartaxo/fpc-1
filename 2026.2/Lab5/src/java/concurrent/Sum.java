import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Sum {

    static class SumThread extends Thread{
        private final String path;

        public SumThread(String path) {
            this.path = path;
        }
        
        @Override
        public void run(){
            try{
                long sum = sum(path);
                System.out.println(path + " : " + sum);
            }catch(Exception e){
                Thread.currentThread().interrupt();
            }
        }
    }

    public static int sum(FileInputStream fis) throws IOException {
        
	int byteRead;
        int sum = 0;
        
        while ((byteRead = fis.read()) != -1) {
        	sum += byteRead;
        }

        return sum;
    }

    public static long sum(String path) throws Exception {
        Thread.sleep(1000); // emulating an additional delay of 1 second
        Path filePath = Paths.get(path);
        if (Files.isRegularFile(filePath)) {
       	    FileInputStream fis = new FileInputStream(filePath.toString());
            return sum(fis);
        } else {
            throw new RuntimeException("Non-regular file: " + path);
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.err.println("Usage: java Sum filepath1 filepath2 filepathN");
            System.exit(1);
        }

	//many exceptions could be thrown here. we don't care

        Thread[] threads = new Thread[args.length];

        for(int i=0;i<args.length;i++){
            threads[i] = new SumThread(args[i]);
            threads[i].start();
        }
        for(int i=0;i<args.length;i++){
            threads[i].join();
        }
    }
}
