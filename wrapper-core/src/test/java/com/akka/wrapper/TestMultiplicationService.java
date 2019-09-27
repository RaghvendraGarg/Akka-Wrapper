package com.akka.wrapper;

import com.akka.wrapper.service.Service;
import org.springframework.stereotype.Component;

/**
 * Created by gargr on 13/02/17.
 */
@Component
public class TestMultiplicationService implements Service<TestImportMessage> {

    @Override
    public void apply(TestImportMessage t) {
        System.out.println("multiplication");
        int i = t.getaNumber();
        i = i * 3;
        t.setaNumber(i);
    }

}
