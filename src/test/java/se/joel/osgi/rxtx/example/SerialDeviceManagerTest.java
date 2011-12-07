package se.joel.osgi.rxtx.example;

import com.ericsson.research.common.testutil.ReflectionTestUtil;
import gnu.io.CommPortIdentifier;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.*;
import org.osgi.service.component.ComponentContext;

import java.util.Dictionary;
import java.util.Enumeration;


/**
 * SerialDeviceManager Tester.
 */
public class SerialDeviceManagerTest {
    private Mockery mockContext = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    private ComponentContext context;
    private Enumeration portIdentifiers;
    private SerialDeviceManager serialDeviceManager;
    private CommPortIdentifier portId;
    private BundleContext bundleContext;
    private Bundle bundle;

    @Before
    public void setUp() throws Exception {
        bundle = mockContext.mock(Bundle.class);
        portIdentifiers = mockContext.mock(Enumeration.class);
        portId = mockContext.mock(CommPortIdentifier.class);
        context = mockContext.mock(ComponentContext.class);
        bundleContext = mockContext.mock(BundleContext.class);
        serialDeviceManager = new SerialDeviceManager();
        ReflectionTestUtil.setField(serialDeviceManager, "portIdentifiers", portIdentifiers);
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Method: activate(ComponentContext mockContext)
     */
    @Test
    public void testActivate() throws InvalidSyntaxException {
        final Filter filter = mockContext.mock(Filter.class);
        mockContext.checking(new Expectations() {{
            allowing(bundleContext).getBundle();
            will(returnValue(bundle));
            allowing(bundle).getBundleId();
            will(returnValue(42L));
            allowing(bundle).getSymbolicName();
            will(returnValue("Banan"));
            oneOf(context).getBundleContext();
            will(returnValue(bundleContext));
            oneOf(bundleContext).createFilter("(objectClass=org.osgi.service.log.LogService)");
            will(returnValue(filter));
            oneOf(bundleContext).addServiceListener(with(aNonNull(ServiceListener.class)),
                with("(objectClass=org.osgi.service.log.LogService)"));
            oneOf(bundleContext).getServiceReferences("org.osgi.service.log.LogService", null);
            will(returnValue(null));

            // First iteration
            oneOf(portIdentifiers).hasMoreElements();
            will(returnValue(true));
            oneOf(portIdentifiers).nextElement();
            will(returnValue(portId));
            oneOf(portId).getName();
            will(returnValue("TEST DEVICE 1"));
            oneOf(portId).getPortType();
            will(returnValue(CommPortIdentifier.PORT_PARALLEL));

            // Second iteration
            oneOf(portIdentifiers).hasMoreElements();
            will(returnValue(true));
            oneOf(portIdentifiers).nextElement();
            will(returnValue(portId));
            oneOf(portId).getName();
            will(returnValue("TEST DEVICE 1"));
            oneOf(portId).getPortType();
            will(returnValue(CommPortIdentifier.PORT_SERIAL));
            oneOf(portId).getName();
            will(returnValue("TEST DEVICE 2"));
            oneOf(context).getBundleContext();
            will(returnValue(bundleContext));
            oneOf(bundleContext)
                .registerService(with(SerialDevice.class.getName()), with(aNonNull(RxtxCommSerialDevice.class)),
                    with(aNull(Dictionary.class)));

            oneOf(portIdentifiers).hasMoreElements();
            will(returnValue(false));
        }});

        serialDeviceManager.activate(context);
    }

    /**
     * Method: deactivate(ComponentContext mockContext)
     */
    @Test
    public void testDeactivate() {
        serialDeviceManager.deactivate(context);
    }


}
