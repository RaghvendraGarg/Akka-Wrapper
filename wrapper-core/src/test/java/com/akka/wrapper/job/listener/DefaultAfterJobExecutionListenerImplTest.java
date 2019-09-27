package com.akka.wrapper.job.listener;


import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.akka.wrapper.TestImportMessage;
import com.akka.wrapper.dto.ImportMessage;

/**
 * Created by gargr on 06/03/17.
 */
public class DefaultAfterJobExecutionListenerImplTest {

    private DefaultAfterJobExecutionListenerImpl j = new DefaultAfterJobExecutionListenerImpl();

    @Test
    public void getMessageIdsForImportMessage() {
        TestImportMessage testImportMessage = new TestImportMessage("Test");
        testImportMessage.setaNumber(12);
        String messageIds = j.getMessageIds(testImportMessage);
        Assert.assertNotNull(messageIds);
    }

    @Test
    public void getMessageIdsWhenObjectTypeIsMultiValueMap() {
        String messgeIds = "";
        MultiValueMap<String, ImportMessage> multiValueMap = new LinkedMultiValueMap();
        TestImportMessage testImportMessage = new TestImportMessage("Test");
        testImportMessage.setaNumber(12);
        messgeIds = messgeIds.concat(testImportMessage.getMessageId());
        multiValueMap.add(testImportMessage.getMessageId(), testImportMessage);
        testImportMessage = new TestImportMessage("Test");
        testImportMessage.setaNumber(12);
        messgeIds = messgeIds.concat("," + testImportMessage.getMessageId());
        multiValueMap.add(testImportMessage.getMessageId(), testImportMessage);
        String messageIds = j.getMessageIds(multiValueMap);
        Assert.assertEquals(messgeIds, messageIds);
    }


}