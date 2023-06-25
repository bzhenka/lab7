package client.network;

import client.exceptions.NetworkClientException;
import requests.Request;
import responses.Response;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;

public class UDPClient implements NetworkClient{
    private final int PACKET_SIZE = 1024;
    private final int DATA_SIZE = PACKET_SIZE - 1;
    private final DatagramChannel client;
    private final InetSocketAddress addr;

    public UDPClient(InetAddress address, int port) throws IOException {
        this.addr = new InetSocketAddress(address, port);
        this.client = DatagramChannel.open().bind(null).connect(addr);
        this.client.configureBlocking(false);
    }

    @Override
    public void openConnection() throws NetworkClientException {

    }

    @Override
    public void closeConnection() throws NetworkClientException {

    }

    @Override
    public void silentCloseConnection() {

    }

    @Override
    public void sendData(byte[] bytesToSend) throws NetworkClientException {
        byte[][] ret = new byte[(int)Math.ceil(bytesToSend.length / (double)DATA_SIZE)][DATA_SIZE];

        int start = 0;
        for(int i = 0; i < ret.length; i++) {
            ret[i] = Arrays.copyOfRange(bytesToSend, start, start + DATA_SIZE);
            start += DATA_SIZE;
    }}

    @Override
    public Response sendRequest(Request request) throws NetworkClientException {
        return null;
    }
}
