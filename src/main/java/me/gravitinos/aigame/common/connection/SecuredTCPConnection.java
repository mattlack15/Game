package me.gravitinos.aigame.common.connection;

import net.ultragrav.serializer.GravSerializer;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

public class SecuredTCPConnection implements AutoCloseable {

    private Socket socket;
    private EncryptionProvider encryptionProvider = null;

    private final Object lock = this;
    private final Object nextLock = new Object(), fillLock = new Object();

    private byte[] buffer;
    private int bufferPos;
    private final AtomicInteger bufferLength = new AtomicInteger();

    public SecuredTCPConnection(Socket socket, int bufferSize) {
        this.socket = socket;
        try {
            socket.setTcpNoDelay(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        this.buffer = new byte[bufferSize];
    }

    public SecuredTCPConnection(Socket socket) {
        this(socket, 65536);
    }


    public void setEncryptionProvider(EncryptionProvider provider) {
        this.encryptionProvider = provider;
    }

    public EncryptionProvider getEncryptionProvider() {
        return this.encryptionProvider;
    }

    private int left() {
        return this.bufferLength.get() - this.bufferPos;
    }

    private GravSerializer next0() {

        synchronized (lock) {

            int mark = bufferPos;

            if (left() < 4) {
                return null;
            }

            //Read size
            int size = 0;
            for (int i = 0; i < 4; ++i) {
                size |= buffer[bufferPos++] << i * 8 & 255 << i * 8;
            }

            //Read data
            if (left() < size) {
                bufferPos = mark;
                if(left() + (buffer.length - bufferLength.get()) < size) {
                    throw new IllegalStateException("Received data (" + (size+4) + ") is more than buffer length (" + buffer.length + ")!");
                }
                return null;
            }

            //Copy data from buffer
            byte[] data = new byte[size];
            System.arraycopy(buffer, bufferPos, data, 0, size);

            bufferPos += size;

            //Decrypt if necessary
            if (encryptionProvider != null) {
                data = encryptionProvider.decrypt(data);
            }

            if(data == null)
                throw new IllegalStateException();

            return new GravSerializer(data);
        }
    }

    private void fill(boolean block) {//TODO
        synchronized (fillLock) {
            try {

                synchronized (lock) {
                    if (bufferPos != 0) {
                        int size = left();
                        System.arraycopy(buffer, bufferPos, buffer, 0, size);
                        bufferPos = 0;
                        bufferLength.set(size);
                    }
                }

                int available = socket.getInputStream().available();
                int n;
                try {
                    int bufferDataEnd = bufferLength.get();
                    int maxReadLength = buffer.length - bufferDataEnd;
                    n = socket.getInputStream().read(buffer, bufferDataEnd, block ? maxReadLength : Math.min(socket.getInputStream().available(), maxReadLength));
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("buffer size: " + buffer.length + " bufferLength: " + bufferLength.get() + " left: " + left() + " avail: " + available);
                    throw e;
                }

                if (n >= 0) {
                    bufferLength.addAndGet(n);
                } else {
                    throw new IllegalStateException("Connection closed.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasNext() {
        fill(false);
        synchronized (lock) {
            int mark = bufferPos;
            if (next0() != null) {
                bufferPos = mark;
                return true;
            }
            return false;
        }
    }

    public GravSerializer next() {

        //Prevent concurrent uses of this method and enforces "first come first serve"
        synchronized (nextLock) {
            GravSerializer out;

            while ((out = next0()) == null) {
                fill(true); //Fill the buffer
            }

            return out;
        }
    }

    public <T extends Packet> T nextPacket() {
        GravSerializer serializer = next();
        Packet packet = Packet.deserialize(serializer, serializer.readInt());
        return (T) packet;
    }

    //Sending
    public void sendPacket(Packet packet) {
        GravSerializer serializer = new GravSerializer();
        serializer.writeInt(packet.getId());
        packet.serialize(serializer);

        this.send(serializer);
    }

    public void send(GravSerializer serializer) {
        byte[] toSend = serializer.toByteArray();

        if (encryptionProvider != null) {
            toSend = encryptionProvider.encrypt(toSend);
        }

        GravSerializer s = new GravSerializer();
        s.writeByteArray(toSend);
        toSend = s.toByteArray();

        try {
            synchronized (lock) {
                socket.getOutputStream().write(toSend);
                socket.getOutputStream().flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

}
