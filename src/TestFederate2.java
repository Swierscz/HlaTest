import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Random;
import java.util.Scanner;

public class TestFederate2 {

    public static final String READY_TO_RUN = "ReadyToRun";
    private RTIambassador rtiAmbassador;
    private TestAmbassador2 testAmbassador2;


    public void runFederate() throws RTIexception {
        rtiAmbassador = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();

        try
        {
            File fom = new File( "test.fed" );
            rtiAmbassador.createFederationExecution( "TestFederation",
                    fom.toURI().toURL() );
            log( "Created Federation" );
        }
        catch( FederationExecutionAlreadyExists exists )
        {
            log( "Didn't create federation, it already existed" );
        }
        catch( MalformedURLException urle )
        {
            log( "Exception processing fom: " + urle.getMessage() );
            urle.printStackTrace();
            return;
        }

        testAmbassador2 = new TestAmbassador2();
        rtiAmbassador.joinFederationExecution( "TestFederate2", "TestFederation", testAmbassador2);
        log( "Joined Federation as TestFederate2");

        rtiAmbassador.registerFederationSynchronizationPoint( READY_TO_RUN, null );

        while( testAmbassador2.isAnnounced == false )
        {
            rtiAmbassador.tick();
        }

        waitForUser();

        rtiAmbassador.synchronizationPointAchieved( READY_TO_RUN );
        log( "Achieved sync point: " +READY_TO_RUN+ ", waiting for federation..." );
        while( testAmbassador2.isReadyToRun == false )
        {
            rtiAmbassador.tick();
        }

        enableTimePolicy();

        publishAndSubscribe();

//        registerStorageObject();
        log("Loop started");
        while (testAmbassador2.running) {
            advanceTime(100);
            rtiAmbassador.tick();
        }

    }

    private void log( String message )
    {
        System.out.println( "TestFederate2   : " + message );
    }

//    private void sendInteraction(double timeStep) throws RTIexception {
//        SuppliedParameters parameters =
//                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();
//        Random random = new Random();
//        int quantityInt = random.nextInt(10) + 1;
//        byte[] quantity = EncodingHelpers.encodeInt(quantityInt);
//
//        int interactionHandle = rtiAmbassador.getInteractionClassHandle("InteractionRoot.TestProduct2");
//        int quantityHandle = rtiAmbassador.getParameterHandle( "quantity", interactionHandle );
//
//        parameters.add(quantityHandle, quantity);
//
//        LogicalTime time = convertTime( timeStep );
////        log("Sending TestProduct2: " + quantityInt);
//        rtiAmbassador.sendInteraction( interactionHandle, parameters, "tag".getBytes(), time );
//    }

    private void waitForUser()
    {
        log( " >>>>>>>>>> Press Enter to Continue <<<<<<<<<<" );
        Scanner checkout = new Scanner(System.in);
        try
        {
            checkout.nextLine();
        }
        catch( Exception e )
        {
            log( "Error while waiting for user input: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    private void enableTimePolicy() throws RTIexception
    {
        LogicalTime currentTime = convertTime( testAmbassador2.federateTime );
        LogicalTimeInterval lookahead = convertInterval( testAmbassador2.federateLookahead );

        this.rtiAmbassador.enableTimeRegulation( currentTime, lookahead );

        while( testAmbassador2.isRegulating == false )
        {
            rtiAmbassador.tick();
        }

        this.rtiAmbassador.enableTimeConstrained();

        while( testAmbassador2.isConstrained == false )
        {
            rtiAmbassador.tick();
        }
    }

    private void publishAndSubscribe() throws RTIexception {
//        int addProductHandle = rtiAmbassador.getInteractionClassHandle( "InteractionRoot.TestProduct2" );
//        rtiAmbassador.publishInteractionClass(addProductHandle);

        int testDataProductHandle = rtiAmbassador.getInteractionClassHandle("InteractionRoot.TestProduct");
        testAmbassador2.testDataProcuctHandle = testDataProductHandle;
        rtiAmbassador.subscribeInteractionClass(testDataProductHandle);
    }

    private void advanceTime( double timestep ) throws RTIexception
    {
        // request the advance
        testAmbassador2.isAdvancing = true;
        LogicalTime newTime = convertTime( testAmbassador2.federateTime + timestep );
        rtiAmbassador.timeAdvanceRequest( newTime );
        while( testAmbassador2.isAdvancing )
        {
            rtiAmbassador.tick();
        }
    }


//    private void updateHLAObject(double time) throws RTIexception{
//        SuppliedAttributes attributes =
//                RtiFactoryFactory.getRtiFactory().createSuppliedAttributes();
//
//        int classHandle = rtiAmbassador.getObjectClass(storageHlaHandle);
//        int stockHandle = rtiAmbassador.getAttributeHandle( "stock", classHandle );
//        byte[] stockValue = EncodingHelpers.encodeInt(stock);
//
//        attributes.add(stockHandle, stockValue);
//        LogicalTime logicalTime = convertTime( time );
//        rtiAmbassador.updateAttributeValues( storageHlaHandle, attributes, "actualize stock".getBytes(), logicalTime );
//    }

//    private void registerStorageObject() throws RTIexception {
//        int classHandle = rtiAmbassador.getObjectClassHandle("ObjectRoot.Storage");
//        this.storageHlaHandle = rtiAmbassador.registerObjectInstance(classHandle);
//    }

    private LogicalTime convertTime( double time )
    {
        // PORTICO SPECIFIC!!
        return new DoubleTime( time );
    }

    /**
     * Same as for {@link #convertTime(double)}
     */
    private LogicalTimeInterval convertInterval(double time )
    {
        // PORTICO SPECIFIC!!
        return new DoubleTimeInterval( time );
    }

    public static void main(String[] args) {
        try {
            new TestFederate2().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }

}
