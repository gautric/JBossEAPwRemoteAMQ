package net.a.g.sample.jboss.remote.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(name = "RemoteMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:comp/env/RemoteQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "UseJNDI", propertyValue = "true"),
        @ActivationConfigProperty(propertyName = "connectionFactoryLookup", propertyValue = "java:comp/env/RemoteConnectionFactory"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "clientID", propertyValue = "jboss-mdb")}
        )
public class RemoteMDB implements MessageListener {

    public void onMessage(Message rcvMessage) {
        TextMessage msg = null;
        try {
            if (rcvMessage instanceof TextMessage) {
                msg = (TextMessage) rcvMessage;
                System.out.println("Received Message from Remote Queue : " + msg.getText());
            } else {
                System.err.println("Message of wrong type: " + rcvMessage.getClass().getName());
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
