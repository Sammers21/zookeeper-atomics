package com.github.Sammers21;

public class ZkNameSpace {
    String zNodeName;
    String zkConnectString;

    public ZkNameSpace(String zNodeName, String zkConnectString) {
        this.zNodeName = zNodeName;
        this.zkConnectString = zkConnectString;
    }
}
