package com.medisons.simulator;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ThreadSocket {

    private DataOutputStream out;

    public ThreadSocket(Socket socket) throws IOException
    {
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    public synchronized void write(byte[] packet) throws IOException
    {
        out.write(packet, 0, packet.length);
        out.flush();
    }
}
