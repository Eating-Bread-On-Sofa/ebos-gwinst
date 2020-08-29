package cn.edu.bjtu.ebosgwinst.service;

public interface MqProducer {
    void publish(String topic, String message);
}
