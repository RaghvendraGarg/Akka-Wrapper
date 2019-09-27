package com.akka.wrapper;

import org.springframework.stereotype.Component;

import com.akka.wrapper.service.Service;

/**
 * Created by gargr on 13/02/17.
 */
@Component
public class TestAdditionService implements Service<TestImportMessage> {

    @Override
    public void apply(TestImportMessage t) {
        System.out.println("addition");
        int i = t.getaNumber();
        i = i + 3;
        t.setaNumber(i);
    }

}
