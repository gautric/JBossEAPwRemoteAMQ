# JBoss EAP 7.3 w/ AMQ Broker 7.6

Ce tutorial permet de configurer un JBoss EAP 7.3 avec un server AMQ Broker 7.X distant

Pour cela il vous faut telecharger via vos souscription Developpeur ou Entreprise les binaires suivants :

* [JBoss EAP 7.3](https://access.redhat.com/jbossnetwork/restricted/listSoftware.html?product=appplatform&downloadType=distributions)
* [AMQ Broker 7.6](https://access.redhat.com/jbossnetwork/restricted/listSoftware.html?downloadType=distributions&product=jboss.amq.broker&version=7.3&productChanged=yes)

## Configuration du serveur JBoss EAP 7.X

Après avoir demarrer votre server JBoss EAP avec le profil full (standalone-full.xml),

> $JBOSS_HOME/bin/standalone.sh -c standalone-full.xml

il est necessaire de passer les commandes suivantes via la CLI JBoss

> $JBOSS_HOME/bin/jboss-cli.sh --connect

```
 /socket-binding-group=standard-sockets/remote-destination-outbound-socket-binding=messaging-remote-throughput:add(host=localhost, port=61616)
 /subsystem=messaging-activemq/remote-connector=netty-remote-throughput:add(socket-binding=messaging-remote-throughput)
 /subsystem=messaging-activemq/pooled-connection-factory=activemq-ra-remote:add(transaction=xa,entries=[java:/RemoteJmsXA, java:jboss/RemoteJmsXA],connectors=[netty-remote-throughput])
 /subsystem=naming/binding=java\:global\/remoteContext:add(binding-type=external-context, class=javax.naming.InitialContext, module=org.apache.activemq.artemis, environment=[java.naming.factory.initial=org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory, java.naming.provider.url=tcp://127.0.0.1:61616, queue.RemoteQueue=RemoteQueue])
 /subsystem=naming/binding=java\:\/jms\/queue\/RemoteQueue:add(lookup=java:global/remoteContext/RemoteQueue,binding-type=lookup)
 /system-property=ejb.resource-adapter-name:add(value="activemq-ra-remote.rar")
```

## Configuration du broker AMQ 7.X

Pour la partie du broker AMQ il faut rajouter la queue suivante dans le fichier _broker.xml_

```
  <address name="RemoteQueue">
    <anycast>
      <queue name="RemoteQueue" />
    </anycast>
  </address>
```

l'acceptor suivant  

```
  <acceptors>
    ...
    <acceptor name="netty-acceptor">tcp://localhost:61616?anycastPrefix=jms.queue.;multicastPrefix=jms.topic.</acceptor>
    ...
  </acceptors>
```

ainsi que le connector

```
  <connectors>
    <connector name="netty">tcp://localhost:61616</connector>
  </connectors>
```

## Compilation de l'application

Dans le repertoire RemoteAMQ
