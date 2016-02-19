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

    String PACKAGE_NAME = "io.github.otakuchiyan.dnsman";
    String ACTION_SETDNS_DONE = PACKAGE_NAME + ".SETDNS_DONE";

    //PROP mode
    String SETPROP_COMMAND_PREFIX = "setprop net.dns";
    String GETPROP_COMMAND_PREFIX = "getprop net.dns";

    String[] CHECKPROP_COMMANDS = {
            GETPROP_COMMAND_PREFIX + "1",
            GETPROP_COMMAND_PREFIX + "2"
    };

    //IPTABLES mode
    String SETRULE_COMMAND = "iptables -t nat %s OUTPUT -p %s --dport 53 -j DNAT --to-destination %s\n";
    String CHECKRULE_COMMAND_PREFIX = "iptables -t nat -L OUTPUT | grep ";

    //NDC mode
    String NDC_COMMAND_PREFIX = "ndc resolver";
    String SETIFDNS_COMMAND_BELOW_42 = NDC_COMMAND_PREFIX + " setifdns %s %s %s\n";
    String SETIFDNS_COMMAND = NDC_COMMAND_PREFIX + " setifdns %s '' %s %s\n";
    String SETNETDNS_COMMAND = NDC_COMMAND_PREFIX + " setnetdns %s '' %s %s\n";
    String SETDEFAULTIF_COMMAND = NDC_COMMAND_PREFIX + " setdefaultif";

    String FLUSHNET_COMMAND = NDC_COMMAND_PREFIX + " flushnet %s\n";
    String FLUSHDEFAULTIF_COMMAND = NDC_COMMAND_PREFIX + " flushdefaultif\n";


    //0 is no error
    int ERROR_SETPROP_FAILED = 1;
    int ERROR_UNKNOWN = 9999;

    int VPN_REQUEST = 0x01;
    int REFRESH_CURRENT_DNS_REQUEST = 0x02;
}