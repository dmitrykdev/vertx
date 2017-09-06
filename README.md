# VERTX

Sample of Server-Client modules interaction using vertx publish-subscribe model.

Server imitates volatility shared source to emitt for client-side interactors via connection
with message-loss safe QoS.

How to: 
  1. Package modules with maven. Proper scripts embeded in related pom.xml.
  2. By default server HOST address for clients is localhost.<br>
     For distributed case submit server HOST address in 'clien/.../App.class' and 'clientDB/.../App.class'
 properties.
  3. Run modules:  java -jar target/\<module-name\>-fat.jar
  4. Enjoy!
  
 Install robomongo to interact with clientDB out of box: https://robomongo.org/
