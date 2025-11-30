package org.example.engine;

import org.example.model.Datagram;

import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;

/**
 * Reader Optimizado con RandomAccess (Seek).
 * Solo lee el fragmento asignado por el Master.
 */
public class DatagramReader implements Runnable {

    private final String filePath;
    private final long startOffset;
    private final long endOffset;
    private final BlockingQueue<Datagram> queue;

    public static final Datagram POISON_PILL = new Datagram(true);

    public DatagramReader(String filePath, long startOffset, long endOffset, BlockingQueue<Datagram> queue) {
        this.filePath = filePath;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.queue = queue;
    }

    @Override
    public void run() {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {

            // 1. Saltar al punto de inicio (Seek)
            raf.seek(startOffset);

            // 2. Ajuste de bordes (Critical Section):
            // Si no estamos al principio del archivo, probablemente caímos a mitad de una línea.
            // Hay que descartar esa línea parcial e ir a la siguiente.
            if (startOffset > 0) {
                raf.readLine();
            }

            String line;
            // 3. Leer mientras la posición del puntero sea menor al límite asignado
            while (raf.getFilePointer() < endOffset && (line = raf.readLine()) != null) {
                try {
                    // RandomAccessFile lee en ISO-8859-1, pero los números son ASCII seguro.
                    // Si tuvieras tildes, habría que convertir a UTF-8, pero para GPS está ok.
                    Datagram d = new Datagram(line);
                    queue.put(d);
                } catch (Exception e) {
                    // Ignorar líneas vacías o corruptas
                }
            }

        } catch (Exception e) {
            System.err.println("[Reader] Error IO: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                queue.put(POISON_PILL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}