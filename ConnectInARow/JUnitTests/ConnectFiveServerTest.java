import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import org.junit.Test;

public class ConnectFiveServerTest {

    private static final int PORT = 5000;
    private OutputStream sOut;
    private InputStream sIn;
    private Semaphore lock = new Semaphore(0); //Shared lock between client & server - sync test case. (semaphore)


    @Test
    public void testClientServer() throws IOException, InterruptedException {
        ServerSocket server = new ServerSocket(PORT);
        listen(server);
        Socket client = new Socket("127.0.0.1", PORT);
        OutputStream clientOut = client.getOutputStream();
        InputStream clientIn = client.getInputStream();
        System.out.println("Waiting for lock");
        lock.acquire();
        System.out.println("Acquired lock");

        write(clientOut, "How's it going?");
        assertRead(sIn, "How's it going?");

        write(sOut, "Howya");
        assertRead(clientIn, "Howya");

        printWrite(clientOut, "printWrite Test");
        assertRead(sIn, "printWrite Test");

        printWrite(sOut, "printWrite Test 2");
        assertRead(clientIn, "printWrite Test 2");

        client.close();
        server.close();
    }

    /**
     * write() - Writes to OutputStream for both server and client.
     */
    private void write(OutputStream out, String str) throws IOException {
        out.write(str.getBytes());
        out.flush();
    }

    /**
     * printWrite() - Writes to OutputStream for both server and client.
     */
    private void printWrite(OutputStream out, String str) throws IOException {
        PrintWriter pw = new PrintWriter(out);
        pw.print(str);
        pw.flush();
    }

    /**
     * assertRead() -Reads from InputStream for both server and client.
     */
    private void assertRead(InputStream in, String expected) throws IOException {
        assertEquals("Too few bytes available for reading: ", expected.length(), in.available());
        byte[] buf = new byte[expected.length()];
        in.read(buf);
        assertEquals(expected, new String(buf));
    }

    /**
     * listen() - Listens & accepts a single incoming request (server side) on a separate
     * thread. Once the request is received, takes in its IO streams and sends to
     * the client side above through the shared lock object.
     */
    private void listen(ServerSocket server) {
        new Thread(() -> {
            try {
                Socket socket = server.accept();
                System.out.println("Incoming Connection: " + socket);

                sOut = socket.getOutputStream();
                sIn = socket.getInputStream();

                lock.release();
                System.out.println("Released lock");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
