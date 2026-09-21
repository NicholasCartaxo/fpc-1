import java.lang.Thread;
import java.lang.Runnable;
import java.util.Random; // Importação necessária para tempo aleatório

public class SimpleConcurrentSolutionV2 {

    // 1.2. Tarefa de Verificação de Recursos (Classe Interna Não Anônima com TEMPO ALEATÓRIO)
    private static class ResourceCheckTask implements Runnable {
        
        private static final Random random = new Random();

        private final int resourceId;

        public ResourceCheckTask(int resourceId){
            this.resourceId = resourceId;
        }

        @Override
        public void run() {
            Thread currentThread = Thread.currentThread();

            System.out.println("[" + currentThread.getName() + "] INÍCIO: Verificação do Recurso " + resourceId + ".");

            try {
                // Gera um valor aleatório entre 1000ms (1s) e 3000ms (3s)
                int sleepTime = 1000 + random.nextInt(2000); 
                System.out.println("[" + currentThread.getName() + "] Verificando Recurso " + resourceId + " (Duração: " + sleepTime + "ms)...");
                Thread.sleep(sleepTime); 
            } catch (InterruptedException e) {
                System.err.println("[" + currentThread.getName() + "] Verificação interrompida.");
                Thread.currentThread().interrupt();
                return;
            }
            System.out.println("[" + currentThread.getName() + "] FIM: Verificação do Recurso " + resourceId + " concluída.");
        }
    }
    // Fim da Classe Interna não anônima

    // 1.1. Tarefa de Inicialização de Logs (Tempo Fixo)
    private static Runnable logSetupTask = new Runnable() {
        @Override
        public void run() {
            Thread currentThread = Thread.currentThread();

            System.out.println("[" + currentThread.getName() + "] INÍCIO: Configuração de Logs...");

            try {
                Thread.sleep(4000); // TEMPO FIXO: 4.0 segundos
            } catch (InterruptedException e) {
                System.err.println("[" + currentThread.getName() + "] Configuração de Logs interrompida.");
                Thread.currentThread().interrupt();
                return;
            }

            System.out.println("[" + currentThread.getName() + "] FIM: Configuração de Logs concluída.");
        }
    };

    public static void main(String[] args) {
        Thread currentThread = Thread.currentThread();
        System.out.println("[" + currentThread.getName() + "] --- INÍCIO DO PROGRAMA JAVA ---");

        // Criação e Início das Threads
        Thread tLogs = new Thread(logSetupTask, "Setup-Logs");
        Thread[] tChecks = new Thread[5];
        for(int i = 0; i < 5; i++){
            tChecks[i] = new Thread(new ResourceCheckTask(i+1), "Check-Recurso-"+(i+1));
            tChecks[i].start();
        }

        // Início da execução concorrente da configuração dos logs
        tLogs.start();

        System.out.println("[" + currentThread.getName() + "] Inicialização de tarefas concorrentes solicitada.");

        try{
            tLogs.join();
            
            for(Thread t : tChecks){
                t.join();
            }

        } catch( InterruptedException e){
            System.err.println("["+currentThread.getName() + "] interrompida.");
            Thread.currentThread().interrupt();
            return;
        }

        System.out.println("[" + currentThread.getName() + "] --- FIM DO PROGRAMA JAVA (Main terminou) ---");
    }
}
