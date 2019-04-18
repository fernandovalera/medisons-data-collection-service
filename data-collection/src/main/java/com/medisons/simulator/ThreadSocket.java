package com.medisons.simulator;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Wrapper for an output stream writing to a socket with thread safety.
 */
public class ThreadSocket {

    private DataOutputStream out;

    /**
     * Constructs new ThreadSocket.
     * @param socket The socket to write to.
     * @throws IOException If there was an error getting the socket's output stream.
     */
    public ThreadSocket(Socket socket) throws IOException
    {
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    /**
     * Writes to socket with thread safety.
     * @param packet The data packet to send.
     * @throws IOException If there was an error writing to the output stream.
     */
    public synchronized void write(byte[] packet) throws IOException
    {
        out.write(packet, 0, packet.length);
        out.flush();
    }
}
