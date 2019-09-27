package com.akka.wrapper;

import com.akka.wrapper.dto.ImportMessage;

/**
 * Created by gargr on 13/02/17.
 */
public class TestImportMessage extends ImportMessage {

    public TestImportMessage(String source) {
        super(source);
    }

    private int aNumber;

    public int getaNumber() {
        return aNumber;
    }

    public void setaNumber(int aNumber) {
        this.aNumber = aNumber;
    }
}
