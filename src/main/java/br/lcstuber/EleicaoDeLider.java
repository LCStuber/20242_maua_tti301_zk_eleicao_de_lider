package br.lcstuber;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class EleicaoDeLider {
    private static final String HOST = "172.20.10.2";
    private static final int PORT = 2181;
    private static  final int TIMEOUT = 50000;
    private static final String NAMESPACE_ELEICAO = "/eleicao";
    private ZooKeeper zooKeeper;
    private String nomeDoZNodeDesseProcesso;

    public static void main(String[] args) throws Exception {
        var eleicaoDeLider = new EleicaoDeLider();
        eleicaoDeLider.conectar();
        eleicaoDeLider.realizarCandidatura();
        eleicaoDeLider.elegeroLíder();
        eleicaoDeLider.executar();
        eleicaoDeLider.fechar();
    }

    public void fechar() throws IOException, InterruptedException {
        zooKeeper.close();
    }

    public void realizarCandidatura() throws KeeperException, InterruptedException {
        String prefixo = String.format("%s/cand_", NAMESPACE_ELEICAO);
        String pathInteiro = zooKeeper.create(
                prefixo, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL
        );
        this.nomeDoZNodeDesseProcesso = pathInteiro.replace(String.format("%s/", NAMESPACE_ELEICAO), "");
    }

    public void elegeroLíder() throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren(NAMESPACE_ELEICAO, false);
        Collections.sort(children);
        String oMenor = children.getFirst();
        if (oMenor.equals(nomeDoZNodeDesseProcesso)) {
            System.out.printf("Me chamo %s e sou o líder\n", nomeDoZNodeDesseProcesso);
        } else {
            System.out.printf("Me chamo %s e não sou o líder. O líder é o %s.\n", nomeDoZNodeDesseProcesso, oMenor);
        }
    }

    public void conectar() throws IOException{
        zooKeeper = new ZooKeeper(
                String.format("%s:%s", HOST, PORT),
                TIMEOUT,
                (evento) -> {
                    if(evento.getType() == Watcher.Event.EventType.None){
                        if(evento.getState() == Watcher.Event.KeeperState.SyncConnected){
                            System.out.println("Conectou");
                        }
                        else if(evento.getState() == Watcher.Event.KeeperState.Disconnected){
                            synchronized (zooKeeper){
                                System.out.println("Desconectou...\n");
                                System.out.printf("Estamos na thread: %s\n", Thread.currentThread().getName());
                                zooKeeper.notify();
                            }
                        }
                    }
                }
        );
    }

    public void executar() throws InterruptedException{
        synchronized (zooKeeper){
            zooKeeper.wait();
        }
    }
}
