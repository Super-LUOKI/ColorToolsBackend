package color.server.colortools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ColorToolsApplication {


    public static void main(String[] args) {
        SpringApplication.run(ColorToolsApplication.class, args);
        printIpv4Address();
    }

    /**
     * 打印本机在局域网中所有的ip地址
     */
    private static void printIpv4Address(){
        List<String> ipList = new ArrayList<String>();
        try {
            java.util.Enumeration<java.net.NetworkInterface> netInterfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                java.net.NetworkInterface ni = (java.net.NetworkInterface) netInterfaces.nextElement();
                java.util.Enumeration<java.net.InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    java.net.InetAddress ip = (java.net.InetAddress) ips.nextElement();
                    if (ip.getHostAddress().indexOf(":") == -1) {
                        ipList.add(ip.getHostAddress());
                    }
                }
            }
        } catch (java.net.SocketException e) {
            e.printStackTrace();
        }
        System.out.println("本机在局域网中所有的ip地址：");
        for (String ip : ipList) {
            System.out.println(ip);
        }

    }

}
