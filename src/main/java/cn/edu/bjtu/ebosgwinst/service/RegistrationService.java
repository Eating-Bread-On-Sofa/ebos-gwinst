package cn.edu.bjtu.ebosgwinst.service;

import cn.edu.bjtu.ebosgwinst.entity.Registration;
import org.springframework.stereotype.Service;

@Service
public interface RegistrationService {
    String registration(Registration registration);
}
