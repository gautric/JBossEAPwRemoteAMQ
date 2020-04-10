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

Dans le repertoire RemoteAMQ, packager l'application via la commande maven.

> mvn clean package

puis pour le deploiement il suffira d'utiliser le plugin wilfly avec la commande suivante 

> mvn wildfly:deploy


## Execution du Sender

Afin de verifier que tout ce passe correctement, 

> curl localhost:8080/remote-amq/send

vous devriez avoir le resultat suivant dans la sortie standard du serveur JBoss EAP

```
19:14:07,748 INFO  [stdout] (Thread-18 (ActiveMQ-client-global-threads)) Received Message from Remote Queue : This is message 1
19:14:07,759 INFO  [stdout] (Thread-16 (ActiveMQ-client-global-threads)) Received Message from Remote Queue : This is message 2
19:14:07,770 INFO  [stdout] (Thread-14 (ActiveMQ-client-global-threads)) Received Message from Remote Queue : This is message 3
19:14:07,819 INFO  [stdout] (Thread-21 (ActiveMQ-client-global-threads)) Received Message from Remote Queue : This is message 4
19:14:07,829 INFO  [stdout] (Thread-17 (ActiveMQ-client-global-threads)) Received Message from Remote Queue : This is message 5
```


## Link 

* [Red Hat Official Documentation](https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.3/html/configuring_messaging/resource_adapters)
* [Helloworld MBD](https://github.com/jboss-developer/jboss-eap-quickstarts/tree/7.3.x/helloworld-mdb)