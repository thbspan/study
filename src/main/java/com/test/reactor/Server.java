package com.test.reactor;

/**
 * <b>Reactor demo</b> <br/>
 * https://www.cnblogs.com/f1194361820/p/5679796.html
 */
public class Server {

    private final Demultiplexer selector = new Demultiplexer();

    private final EventDispatcher eventLooper = new EventDispatcher(selector);

    private final Acceptor acceptor;

    public Server(int port) {
        acceptor = new Acceptor(selector, port);
    }

    public void start() {
        eventLooper.registEventHandler(EventType.ACCEPT, new AcceptEventHandler(selector));
        new Thread(acceptor, "Acceptor-" + acceptor.getPort()).start();
        eventLooper.handleEvents();
    }
}
