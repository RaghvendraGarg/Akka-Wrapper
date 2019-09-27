package com.akka.wrapper.util;

import java.text.ParseException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by gargr on 25/01/17.
 */
public class UtilTest {

    @Test
    public void convertStringToDate()throws ParseException {
        Date object = Util.convertStringToDate("15-01-2017");
        Assert.assertNotNull(object);
        Date object1 = Util.convertStringToDate("20170818");
        Assert.assertNotNull(object1);
        Date object3 = Util.convertStringToDate("2017-01-18T11:29:03.258+0000");
        Assert.assertNotNull(object3);
    }

}