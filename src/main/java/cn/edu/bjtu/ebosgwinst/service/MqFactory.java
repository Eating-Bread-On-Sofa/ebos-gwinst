package cn.edu.bjtu.ebosgwinst.service;

public interface MqFactory {
    MqProducer createProducer();
    MqConsumer createConsumer(String topic);
}
