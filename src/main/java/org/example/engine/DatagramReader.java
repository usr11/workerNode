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
        System.out.println("[Reader] === INICIANDO LECTURA ===");
        System.out.println("[Reader] Archivo: " + filePath);
        System.out.println("[Reader] Rango: " + startOffset + " - " + endOffset);

        // ✅ VALIDACIÓN: Verificar que el archivo existe
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("[Reader] ERROR: Archivo no encontrado: " + filePath);
            System.err.println("[Reader] Ruta absoluta intentada: " + file.getAbsolutePath());
            sendPoisonPillAndExit();
            return;
        }

        if (!file.canRead()) {
            System.err.println("[Reader] ERROR: Sin permisos de lectura: " + filePath);
            sendPoisonPillAndExit();
            return;
        }

        System.out.println("[Reader] Archivo validado. Tamaño total: " + file.length() + " bytes");

        int linesRead = 0;
        int linesSkipped = 0;

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {

            // 1. Posicionarse en el offset inicial
            raf.seek(startOffset);
            System.out.println("[Reader] Seek a posición: " + startOffset);

            // 2. Ajuste de bordes
            if (startOffset > 0) {
                String discarded = raf.readLine();
                System.out.println("[Reader] Línea parcial descartada (border adjustment)");
            }

            System.out.println("[Reader] Comenzando lectura de líneas...");

            // 3. Leer líneas
            String line;
            long lastReportedPosition = raf.getFilePointer();

            while (raf.getFilePointer() < endOffset && (line = raf.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    linesSkipped++;
                    continue;
                }

                try {
                    Datagram d = new Datagram(line);
                    queue.put(d); // Bloqueante
                    linesRead++;

                    // Reporte de progreso
                    if (linesRead % 1000 == 0) {
                        long currentPos = raf.getFilePointer();
                        long bytesRead = currentPos - lastReportedPosition;
                        System.out.println(String.format(
                                "[Reader] Leídas %,d líneas | Pos: %,d | +%,d bytes | Cola: %d",
                                linesRead, currentPos, bytesRead, queue.size()
                        ));
                        lastReportedPosition = currentPos;
                    }

                } catch (IllegalArgumentException e) {
                    linesSkipped++;
                }
            }

            System.out.println("[Reader] === LECTURA COMPLETADA ===");
            System.out.println("[Reader] Líneas válidas: " + linesRead);
            System.out.println("[Reader] Líneas inválidas: " + linesSkipped);
            System.out.println("[Reader] Posición final: " + raf.getFilePointer());

        } catch (Exception e) {
            System.err.println("[Reader] *** ERROR DE I/O ***");
            System.err.println("[Reader] Mensaje: " + e.getMessage());
            e.printStackTrace();
        } finally {
            sendPoisonPillAndExit();
        }
    }

    private void sendPoisonPillAndExit() {
        try {
            System.out.println("[Reader] Enviando POISON_PILL...");
            queue.put(POISON_PILL);
            System.out.println("[Reader] POISON_PILL enviada. Hilo finalizado.");
        } catch (InterruptedException e) {
            System.err.println("[Reader] Interrumpido al enviar POISON_PILL");
            Thread.currentThread().interrupt();
        }
    }
}