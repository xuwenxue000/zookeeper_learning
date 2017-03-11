package com.william.xu.utils;

import org.apache.commons.lang.StringUtils;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.*;
import java.util.*;

public class ServerUtils {


    public static String getLocalIP() throws IOException {
        String myip = null;
        try {
            Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()){
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                Enumeration addresses = netInterface.getInetAddresses();
                if(netInterface.getName().startsWith("net")||netInterface.getName().startsWith("eth")||netInterface.getName().startsWith("en")||netInterface.getName().startsWith("bond")){
                    while (addresses.hasMoreElements())
                    {
                        ip = (InetAddress) addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address)
                        {
                            if(ip.getHostAddress().startsWith("192.") || ip.getHostAddress().startsWith("10.")){
                                myip = ip.getHostAddress();
                                break;
                            }
                        }
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally{
        }

        return myip;

    }


    public static String getLocalPort() throws IOException {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> objs = mbs.queryNames(new ObjectName("*:type=Connector,*"), Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
            for (Iterator<ObjectName> i = objs.iterator(); i.hasNext();) {
                ObjectName obj = i.next();
                String port = obj.getKeyProperty("port");
                return port;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
        }
        return "8080";

    }


    /**
     * 根据ip获取对应mac地址
     */
    public  static String getMacAddr(String ip){
        String MacAddr = "";
        StringBuffer str = new StringBuffer();
        try {
            InetAddress ia = InetAddress.getByName(ip);
            if(ia != null ){
                NetworkInterface NIC;
                try {
                    NIC = NetworkInterface.getByInetAddress(ia);
                    byte[] buf = NIC.getHardwareAddress();
                    for (int i = 0; i < buf.length; i++) {
                        str = str.append(byteHEX(buf[i]));
                    }
                    MacAddr = str.toString().toUpperCase();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return MacAddr;
    }
    /**
     * 获取本机名
     * @return
     */
    public static String getName(){
        String name="";
        try {
            name = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        return name;
    }
    /* 一个将字节转化为十六进制ASSIC码的函数 */
    public static String byteHEX(byte ib) {
        char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a','b', 'c', 'd', 'e', 'f' };
        char[] ob = new char[2];
        ob[0] = Digit[(ib >>> 4) & 0X0F];
        ob[1] = Digit[ib & 0X0F];
        String s = new String(ob);
        return s;
    }

    /**
     * 获取公网IP地址
     */
    private  static String getMyIP() throws IOException {
        InputStream ins = null;
        BufferedReader bReader = null;
        try {
            URL url = new URL("http://iframe.ip138.com/ic.asp");
            URLConnection con = url.openConnection();
            ins = con.getInputStream();
            InputStreamReader isReader = new InputStreamReader(ins, "GB2312");
            bReader = new BufferedReader(isReader);
            StringBuffer webContent = new StringBuffer();
            String str = null;
            while ((str = bReader.readLine()) != null) {
                webContent.append(str);
            }
            int start = webContent.indexOf("[") + 1;
            int end = webContent.indexOf("]");
            return webContent.substring(start, end);
        } finally {
            if (ins != null) {
                ins.close();
            }
            if(bReader != null){
                bReader.close();
            }
        }
    }

    /**
     * 检测端口是否开放
     */
    public static boolean checkMyselfPort(String ip, int port){
        Socket client = null;
        try{
            client = new Socket(ip,port);
            client.close();
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public static String getLocalMac() {
        String mac=null;
        try {
            String ip = getLocalIP();
            mac = getMacAddr(ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mac;
    }
}
