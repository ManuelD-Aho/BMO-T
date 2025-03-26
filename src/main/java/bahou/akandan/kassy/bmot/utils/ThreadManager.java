package bahou.akandan.kassy.bmot.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadManager {

    // Taille du pool de threads : on peut la fixer ou la récupérer de config.properties
    private static final int POOL_SIZE = 10;
    private static final ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);

    /**
     * Soumet une tâche à exécuter dans le pool de threads.
     * @param runnable Tâche à exécuter.
     */
    public static void submitTask(Runnable runnable) {
        executor.submit(runnable);
    }

    /**
     * Arrête correctement le pool de threads (utile lors de l'arrêt du serveur).
     */
    public static void shutdown() {
        executor.shutdown();
        try {
            // On attend la fin des tâches en cours (ou un timeout)
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
