/*******************************************************************************
 * Copyright (c) 2016 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.gameontext.player;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.gameontext.player.entity.PlayerDbRecord;
import org.gameontext.player.utils.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ApplicationScoped
public class Kafka {

   @Resource(lookup="kafkaUrl")
   protected String kafkaUrl;

   private Producer<String,String> producer=null;

   public Kafka(){
   }

   private boolean multipleHosts(){
       //this is a cheat, we need to enable ssl when talking to message hub, and not to kafka locally
       //the easiest way to know which we are running on, is to check how many hosts are in kafkaUrl
       //locally for kafka there'll only ever be one, and messagehub gives us a whole bunch..
       return kafkaUrl.indexOf(",") != -1;
   }

   @PostConstruct
   public void init(){

     try{
         try{
             //Kafka client expects this property to be set and pointing at the
             //jaas config file.. except when running in liberty, we don't need
             //one of those.. thankfully, neither does kafka client, it just doesn't
             //know that.. so we'll set this to an empty string to bypass the check.
             if(System.getProperty("java.security.auth.login.config")==null){
               System.setProperty("java.security.auth.login.config", "");
             }

             Log.log(Level.INFO, this, "Initializing kafka producer for url {0}", kafkaUrl);
             Properties producerProps = new Properties();
             producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
             producerProps.put(ProducerConfig.ACKS_CONFIG,"-1");
             producerProps.put(ProducerConfig.CLIENT_ID_CONFIG,"gameon-map");
             producerProps.put(ProducerConfig.RETRIES_CONFIG,0);
             producerProps.put(ProducerConfig.BATCH_SIZE_CONFIG,16384);
             producerProps.put(ProducerConfig.LINGER_MS_CONFIG,1);
             producerProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG,33554432);
             producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
             producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");

             //this is a cheat, we need to enable ssl when talking to message hub, and not to kafka locally
             //the easiest way to know which we are running on, is to check how many hosts are in kafkaUrl
             //locally for kafka there'll only ever be one, and messagehub gives us a whole bunch..
             boolean multipleHosts = multipleHosts();
             if(multipleHosts){
               Log.log(Level.INFO, this, "Initializing SSL Config for MessageHub");
               producerProps.put("security.protocol","SASL_SSL");
               producerProps.put("ssl.protocol","TLSv1.2");
               producerProps.put("ssl.enabled.protocols","TLSv1.2");
               Path p = Paths.get(System.getProperty("java.home"), "lib", "security", "cacerts");
               producerProps.put("ssl.truststore.location", p.toString());
               producerProps.put("ssl.truststore.password","changeit");
               producerProps.put("ssl.truststore.type","JKS");
               producerProps.put("ssl.endpoint.identification.algorithm","HTTPS");
             }

             producer = new KafkaProducer<String, String>(producerProps);

         }catch(KafkaException k){
             Throwable cause = k.getCause();
             if(cause.getMessage().contains("DNS resolution failed for url") && multipleHosts()){
                 Log.log(Level.SEVERE, this, "Error during Kafka Init. Kafka will be unavailable. You may need to restart all linked containers.", cause);
             }else{
                 throw k;
             }
         }
     }catch(Exception e){
         Log.log(Level.SEVERE, this, "Unknown error during kafka init, please report ", e);
     }
   }

   public void publishMessage(String topic, String key, String message){
     if(producer!=null){
         Log.log(Level.FINER, this, "Publishing Event {0} {1} {2}",topic,key,message);
         ProducerRecord<String,String> pr = new ProducerRecord<String,String>(topic, key, message);
         producer.send(pr);
         Log.log(Level.FINER, this, "Published Event");
     }else{
         Log.log(Level.FINER, this, "Kafka Unavailable, ignoring event {0} {1} {2}",topic,key,message);
     }
   }

   protected final ObjectMapper mapper = new ObjectMapper();
   public enum PlayerEvent {UPDATE,UPDATE_LOCATION,UPDATE_APIKEY,UPDATE_EMAIL,CREATE,DELETE}; 
   public void publishPlayerEvent(PlayerEvent eventType, PlayerDbRecord player){
       try{
           //note that messagehub topics are charged, so we must only
           //create them via the bluemix ui, to avoid accidentally
           //creating a thousand topics =)
           String topic = "playerEvents";
           //playerEvents are keyed by player id.
           String key = player.getId();

           ObjectNode rootNode = mapper.createObjectNode();
           rootNode.put("type", eventType.name());
           rootNode.set("player", mapper.valueToTree(player));

           String message = mapper.writeValueAsString(rootNode);

           publishMessage(topic, key, message);
       }catch(JsonProcessingException e){
           Log.log(Level.SEVERE, this, "Error during event publish, could not build json for player with id "+player.getId(),e);
       }
   }
}
