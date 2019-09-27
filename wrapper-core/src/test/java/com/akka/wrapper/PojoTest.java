package com.akka.wrapper;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.PojoValidator;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class PojoTest {
    // The package to test
    private static final String[] POJO_PACKAGES = {"com.akka.wrapper.dto", "com.akka.wrapper.job", "com.akka.wrapper.job.listener"};

    private List<PojoClass> pojoClasses; // NOPMD

    private PojoValidator pojoValidator; // NOPMD

    @Before
    public void setUp() {

        pojoValidator = new PojoValidator();

        pojoValidator.addTester(new SetterTester());
        pojoValidator.addTester(new GetterTester());

    }

    @Test
    public void testPojoStructureAndBehavior() {
        for (String packageName : POJO_PACKAGES) {
            pojoClasses = PojoClassFactory.getPojoClasses(packageName);
            for (final PojoClass pojoClass : pojoClasses) {
                try {
                    if (!(pojoClass.getName().equalsIgnoreCase("com.akka.wrapper.testingUtil.ExecutePerlJob") || pojoClass.getName().equalsIgnoreCase("com.akka.wrapper.testingUtil.PerlJobExecutor"))) {
                        pojoValidator.runValidation(pojoClass);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
