package go.kr.dsp.api.camel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


@Slf4j
@Component
public class AgentRouteBuilder {

  public String build() {
    String hostAddr = "";

    try {
      Enumeration<NetworkInterface> nienum = NetworkInterface.getNetworkInterfaces();
      while (nienum.hasMoreElements()) {
        NetworkInterface ni = nienum.nextElement();
        Enumeration<InetAddress> kk= ni.getInetAddresses();
        while (kk.hasMoreElements()) {
          InetAddress inetAddress = kk.nextElement();
          if (!inetAddress.isLoopbackAddress() &&
            !inetAddress.isLinkLocalAddress() &&
            inetAddress.isSiteLocalAddress()) {
            hostAddr = inetAddress.getHostAddress();
          }
        }
      }

    } catch (SocketException e) {
      throw new RuntimeException(e);
    }
    return hostAddr;
  }
}
