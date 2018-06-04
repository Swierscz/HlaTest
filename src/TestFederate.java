import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Random;
import java.util.Scanner;

public class TestFederate {

    public static final String READY_TO_RUN = "ReadyToRun";
    private RTIambassador rtiAmbassador;
    private TestAmbassador testAmbassador;


    public void runFederate() throws RTIexception {
        rtiAmbassador = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();

        try {
            File fom = new File("test.fed");
            rtiAmbassador.createFederationExecution("TestFederation",
                    fom.toURI().toURL());
            log("Created Federation");
        } catch (FederationExecutionAlreadyExists exists) {
            log("Didn't create federation, it already existed");
        } catch (MalformedURLException urle) {
            log("Exception processing fom: " + urle.getMessage());
            urle.printStackTrace();
            return;
        }

        testAmbassador = new TestAmbassador();


        rtiAmbassador.joinFederationExecution("TestFederate", "TestFederation", testAmbassador);
        log("Joined Federation as TestFederate");


        rtiAmbassador.registerFederationSynchronizationPoint(READY_TO_RUN, null);

        while (testAmbassador.isAnnounced == false) {
            rtiAmbassador.tick();
        }

        waitForUser();

        rtiAmbassador.synchronizationPointAchieved(READY_TO_RUN);
        log("Achieved sync point: " + READY_TO_RUN + ", waiting for federation...");
        while (testAmbassador.isReadyToRun == false) {
            rtiAmbassador.tick();
        }

        enableTimePolicy();

        publishAndSubscribe();
        log("Loop started");
        while (testAmbassador.running) {
            advanceTime(100);
            sendInteraction(testAmbassador.federateTime + testAmbassador.federateLookahead);
            rtiAmbassador.tick();
        }

    }

    private void log(String message) {
        System.out.println("TestFederate   : " + message);
    }

    private void enableTimePolicy() throws RTIexception
    {
        LogicalTime currentTime = convertTime( testAmbassador.federateTime );
        LogicalTimeInterval lookahead = convertInterval( testAmbassador.federateLookahead );

        this.rtiAmbassador.enableTimeRegulation( currentTime, lookahead );

        while( testAmbassador.isRegulating == false )
        {
            rtiAmbassador.tick();
        }

        this.rtiAmbassador.enableTimeConstrained();

        while( testAmbassador.isConstrained == false )
        {
            rtiAmbassador.tick();
        }
    }

    private void sendInteraction(double timeStep) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();
        Random random = new Random();
        int quantityInt = random.nextInt(10) + 1;
        byte[] quantity = EncodingHelpers.encodeInt(quantityInt);

        int interactionHandle = rtiAmbassador.getInteractionClassHandle("InteractionRoot.TestProduct");
        int quantityHandle = rtiAmbassador.getParameterHandle("quantity", interactionHandle);

        parameters.add(quantityHandle, quantity);

        LogicalTime time = convertTime(timeStep);
        log("Sending TestProduct: " + quantityInt + ", time=" + testAmbassador.federateTime);
        rtiAmbassador.sendInteraction(interactionHandle, parameters, "tag".getBytes(), time);
    }

    private void waitForUser() {
        log(" >>>>>>>>>> Press Enter to Continue <<<<<<<<<<");
        Scanner checkout = new Scanner(System.in);
        try {
            checkout.nextLine();
        } catch (Exception e) {
            log("Error while waiting for user input: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void publishAndSubscribe() throws RTIexception {
        int addProductHandle = rtiAmbassador.getInteractionClassHandle("InteractionRoot.TestProduct");
        rtiAmbassador.publishInteractionClass(addProductHandle);

        int testData2ProductHandle = rtiAmbassador.getInteractionClassHandle("InteractionRoot.TestProduct2");
        testAmbassador.testData2ProcuctHandle = testData2ProductHandle;
        rtiAmbassador.subscribeInteractionClass(testData2ProductHandle);
    }

    private void advanceTime(double timestep) throws RTIexception {
//        log("requesting time advance for: " + timestep);
        // request the advance
        testAmbassador.isAdvancing = true;
        LogicalTime newTime = convertTime(testAmbassador.federateTime + timestep);
        rtiAmbassador.timeAdvanceRequest(newTime);
        while (testAmbassador.isAdvancing) {
            rtiAmbassador.tick();
        }
    }

    private LogicalTime convertTime(double time) {
        // PORTICO SPECIFIC!!
        return new DoubleTime(time);
    }

    /**
     * Same as for {@link #convertTime(double)}
     */
    private LogicalTimeInterval convertInterval(double time) {
        // PORTICO SPECIFIC!!
        return new DoubleTimeInterval(time);
    }

    public static void main(String[] args) {
        try {
            new TestFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }


}
