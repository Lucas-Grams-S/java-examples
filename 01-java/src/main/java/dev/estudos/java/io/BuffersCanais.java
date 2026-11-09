package dev.estudos.java.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * J13-03 · NIO: ByteBuffer (position/limit/capacity, flip, clear), FileChannel, buffer direto e um Selector
 * atendendo dois clientes com uma única thread.
 * Documento: java-docs/01-java/13-io-nio-e-http.md §2.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * antes de ler -&gt; pos=0 lim=5 cap=5
 * depois de read -&gt; pos=5 lim=5
 * depois de flip -&gt; pos=0 lim=5
 * decodificado -&gt; o1;10
 * depois de clear -&gt; pos=0 lim=5
 * sem flip, bytes restantes para ler -&gt; 0
 * com flip, bytes restantes para ler -&gt; 4
 * buffer direto -&gt; isDirect=true, heap -&gt; false
 * escrito via canal -&gt; novo
 * clientes atendidos pela mesma thread -&gt; 2 (main)
 * respostas -&gt; [CLIENTE0, CLIENTE1]
 * </pre>
 */
public class BuffersCanais {
    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        Path f = Files.createTempFile("nio", ".txt");
        try {
            Files.writeString(f, "o1;10\no2;20\n");
            try (var ch = FileChannel.open(f, StandardOpenOption.READ)) {
                ByteBuffer b = ByteBuffer.allocate(5);
                out.add("antes de ler -> pos=" + b.position() + " lim=" + b.limit() + " cap=" + b.capacity());
                ch.read(b);
                out.add("depois de read -> pos=" + b.position() + " lim=" + b.limit());
                b.flip();
                out.add("depois de flip -> pos=" + b.position() + " lim=" + b.limit());
                out.add("decodificado -> " + StandardCharsets.UTF_8.decode(b));
                b.clear();
                out.add("depois de clear -> pos=" + b.position() + " lim=" + b.limit());
            }
            // Sem flip(): leitura devolve "nada" (position já está no fim)
            ByteBuffer esquecido = ByteBuffer.allocate(4);
            esquecido.put("abcd".getBytes());
            out.add("sem flip, bytes restantes para ler -> " + esquecido.remaining());
            esquecido.flip();
            out.add("com flip, bytes restantes para ler -> " + esquecido.remaining());

            out.add("buffer direto -> isDirect=" + ByteBuffer.allocateDirect(16).isDirect() + ", heap -> " + ByteBuffer.allocate(16).isDirect());

            // escrita com canal
            try (var ch = FileChannel.open(f, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                ch.write(ByteBuffer.wrap("novo".getBytes(StandardCharsets.UTF_8)));
            }
            out.add("escrito via canal -> " + Files.readString(f));
        } finally { Files.deleteIfExists(f); }

        out.addAll(seletor());
        return out;
    }

    /** Uma thread e um Selector atendem 2 clientes (eco em maiúsculas). */
    static List<String> seletor() throws Exception {
        List<String> out = new ArrayList<>();
        try (Selector sel = Selector.open(); ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress("127.0.0.1", 0));
            server.configureBlocking(false);
            server.register(sel, SelectionKey.OP_ACCEPT);
            int port = ((InetSocketAddress) server.getLocalAddress()).getPort();

            List<String> respostas = java.util.Collections.synchronizedList(new ArrayList<>());
            Thread[] clientes = new Thread[2];
            for (int i = 0; i < 2; i++) {
                String msg = "cliente" + i;
                clientes[i] = Thread.ofPlatform().start(() -> {
                    try (var s = SocketChannel.open(new InetSocketAddress("127.0.0.1", port))) {
                        s.write(ByteBuffer.wrap(msg.getBytes()));
                        ByteBuffer b = ByteBuffer.allocate(64);
                        s.read(b);
                        b.flip();
                        respostas.add(StandardCharsets.UTF_8.decode(b).toString());
                    } catch (IOException e) { respostas.add("erro " + e); }
                });
            }
            int atendidos = 0;
            String threadDoSeletor = Thread.currentThread().getName();
            long limite = System.currentTimeMillis() + 5_000;
            while (atendidos < 2 && System.currentTimeMillis() < limite) {
                sel.select(200);
                Iterator<SelectionKey> it = sel.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey k = it.next(); it.remove();
                    if (k.isAcceptable()) {
                        SocketChannel c = server.accept();
                        if (c != null) { c.configureBlocking(false); c.register(sel, SelectionKey.OP_READ); }
                    } else if (k.isReadable()) {
                        SocketChannel c = (SocketChannel) k.channel();
                        ByteBuffer b = ByteBuffer.allocate(64);
                        int n = c.read(b);
                        if (n < 0) { c.close(); continue; }
                        b.flip();
                        String txt = StandardCharsets.UTF_8.decode(b).toString();
                        c.write(ByteBuffer.wrap(txt.toUpperCase().getBytes()));
                        atendidos++;
                    }
                }
            }
            for (Thread t : clientes) t.join();
            out.add("clientes atendidos pela mesma thread -> " + atendidos + " (" + threadDoSeletor + ")");
            out.add("respostas -> " + respostas.stream().sorted().toList());
        }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
