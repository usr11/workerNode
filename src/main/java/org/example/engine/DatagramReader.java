package org.example.engine;

import org.example.model.Datagram;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;

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
        System.out.println("[Reader] ─── Iniciando lectura de archivo ───");
        System.out.println("[Reader] Rango: [" + String.format("%,d", startOffset) +
                " - " + String.format("%,d", endOffset) + "]");

        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("[Reader] ✗ ERROR: Archivo no encontrado: " + filePath);
            System.err.println("[Reader]   Ruta absoluta: " + file.getAbsolutePath());
            sendPoisonPillAndExit();
            return;
        }

        if (!file.canRead()) {
            System.err.println("[Reader] ✗ ERROR: Sin permisos de lectura");
            sendPoisonPillAndExit();
            return;
        }

        System.out.println("[Reader] ✓ Archivo validado (" +
                String.format("%.2f MB total)", file.length() / 1024.0 / 1024.0));

        int linesRead = 0;
        int linesSkipped = 0;
        long readStartTime = System.currentTimeMillis();

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {

            raf.seek(startOffset);

            // Ajuste de bordes
            if (startOffset > 0) {
                raf.readLine();
            }

            System.out.println("[Reader] ─── Leyendo líneas del CSV ───");

            String line;
            long lastReport = System.currentTimeMillis();
            int lastCount = 0;

            while (raf.getFilePointer() < endOffset && (line = raf.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    linesSkipped++;
                    continue;
                }

                try {
                    Datagram d = new Datagram(line);
                    queue.put(d);
                    linesRead++;

                    // reporte
                    long now = System.currentTimeMillis();
                    if (now - lastReport >= 2000) {
                        int rate = (int) ((linesRead - lastCount) / 2.0);
                        double progress = ((raf.getFilePointer() - startOffset) * 100.0) /
                                (endOffset - startOffset);

                        System.out.println(String.format(
                                "[Reader] Progreso: %5.1f%% | Leídas: %,7d | Rate: %,5d/s | Cola: %d",
                                progress, linesRead, rate, queue.size()
                        ));

                        lastReport = now;
                        lastCount = linesRead;
                    }

                } catch (IllegalArgumentException e) {
                    linesSkipped++;
                }
            }

            long readTime = System.currentTimeMillis() - readStartTime;

            System.out.println("[Reader] ─────────────────────────────────");
            System.out.println("[Reader] ✓ Lectura completada");
            System.out.println("[Reader]   Líneas válidas:   " + String.format("%,d", linesRead));
            System.out.println("[Reader]   Líneas inválidas: " + String.format("%,d", linesSkipped));
            System.out.println("[Reader]   Tiempo lectura:   " + readTime + " ms");
            System.out.println("[Reader]   Rate promedio:    " + String.format("%,d", linesRead * 1000 / readTime) + " líneas/s");
            System.out.println("[Reader] ─────────────────────────────────");

        } catch (Exception e) {
            System.err.println("[Reader] ✗ ERROR DE I/O: " + e.getMessage());
            e.printStackTrace();
        } finally {
            sendPoisonPillAndExit();
        }
    }

    private void sendPoisonPillAndExit() {
        try {
            queue.put(POISON_PILL);
            System.out.println("[Reader] ✓ POISON_PILL enviada. Finalizando Reader.");
        } catch (InterruptedException e) {
            System.err.println("[Reader] ✗ Interrumpido al enviar POISON_PILL");
            Thread.currentThread().interrupt();
        }
    }
}