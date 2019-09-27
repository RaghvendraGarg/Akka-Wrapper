package com.akka.wrapper;

import com.akka.wrapper.service.Service;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Created by gargr on 22/02/17.
 */
@Component
public class TestBunchService implements Service<MultiValueMap<String, TestImportMessage>> {

    private MultiValueMap<String, TestImportMessage> stringTestImportMessageMultiValueMap = new LinkedMultiValueMap<>();

    @Override
    public void apply(MultiValueMap<String, TestImportMessage> message) {
        System.out.println(message.size());
        message.forEach((k, v) -> {
            System.out.println(k + " :: ");
            v.forEach(t -> System.out.println(t.getaNumber()));
            stringTestImportMessageMultiValueMap.put(k ,v);
        });

    }

    public MultiValueMap<String, TestImportMessage> getStringTestImportMessageMultiValueMap() {
        return stringTestImportMessageMultiValueMap;
    }
}
