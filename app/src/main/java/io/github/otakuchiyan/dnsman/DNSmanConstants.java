package io.github.otakuchiyan.dnsman;

public interface DNSmanConstants{
    String[] DEFAULT_LIST = {
            "127.0.0.1",
            "192.168.0.1",
            "192.168.100.1",
            "8.8.8.8",
            "8.8.4.4",
            "208.67.222.222",
            "208.67.220.220"
    };
    String ACTION_SETDNS_DONE = "io.github.otakuchiyan.dnsman.SETDNS_DONE";
}