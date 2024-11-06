package br.lcstuber;

import org.apache.zookeeper.ZooKeeper;

public class EleiçãoDeLíder {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static  final int TIMEOUT = 5000;
    private ZooKeeper zk;

    public static void main(String[] args) {

    }

    public void conectar() throws Exception {
        zk = new ZooKeeper(
            String.format("%s:%d", HOST, PORT),
            TIMEOUT,
            (evento) -> {

            }
        );
    }
}
