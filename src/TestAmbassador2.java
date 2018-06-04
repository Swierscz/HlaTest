import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import org.portico.impl.hla13.types.DoubleTime;

public class TestAmbassador2 extends NullFederateAmbassador {

    protected double federateTime        = 0.0;
    protected double grantedTime         = 0.0;
    protected double federateLookahead   = 1.0;

    protected boolean isRegulating       = false;
    protected boolean isConstrained      = false;
    protected boolean isAdvancing        = false;

    protected boolean isAnnounced        = false;
    protected boolean isReadyToRun       = false;

    protected boolean running 			 = true;

    protected int testDataProcuctHandle = 1;

    private double convertTime( LogicalTime logicalTime )
    {
        // PORTICO SPECIFIC!!
        return ((DoubleTime)logicalTime).getTime();
    }

    private void log( String message )
    {
        System.out.println( "FederateAmbassador: " + message );
    }

    public void synchronizationPointRegistrationFailed( String label )
    {
        log( "Failed to register sync point: " + label );
    }

    public void synchronizationPointRegistrationSucceeded( String label )
    {
        log( "Successfully registered sync point: " + label );
    }

    public void announceSynchronizationPoint( String label, byte[] tag )
    {
        log( "Synchronization point announced: " + label );
        if( label.equals(TestFederate.READY_TO_RUN) )
            this.isAnnounced = true;
    }

    public void federationSynchronized( String label )
    {
        log( "Federation Synchronized: " + label );
        if( label.equals(TestFederate.READY_TO_RUN) )
            this.isReadyToRun = true;
    }


    /**
     * The RTI has informed us that time regulation is now enabled.
     */
    public void timeRegulationEnabled( LogicalTime theFederateTime )
    {
        this.federateTime = convertTime( theFederateTime );
        this.isRegulating = true;
    }

    public void timeConstrainedEnabled( LogicalTime theFederateTime )
    {
        this.federateTime = convertTime( theFederateTime );
        this.isConstrained = true;
    }

    public void timeAdvanceGrant( LogicalTime theTime )
    {
        this.federateTime = convertTime( theTime );
        this.isAdvancing = false;
    }


//    public void receiveInteraction( int interactionClass,
//                                    ReceivedInteraction theInteraction,
//                                    byte[] tag )
//    {
//        // just pass it on to the other method for printing purposes
//        // passing null as the time will let the other method know it
//        // it from us, not from the RTI
//        try {
//            receiveInteraction(interactionClass, theInteraction, tag, null, null);
//        } catch (InteractionClassNotKnown interactionClassNotKnown) {
//            interactionClassNotKnown.printStackTrace();
//        } catch (InteractionParameterNotKnown interactionParameterNotKnown) {
//            interactionParameterNotKnown.printStackTrace();
//        } catch (InvalidFederationTime invalidFederationTime) {
//            invalidFederationTime.printStackTrace();
//        } catch (FederateInternalError federateInternalError) {
//            federateInternalError.printStackTrace();
//        }
//    }

    @Override
    public void receiveInteraction(int interactionClass, ReceivedInteraction theInteraction, byte[] userSuppliedTag, LogicalTime theTime, EventRetractionHandle eventRetractionHandle) throws InteractionClassNotKnown, InteractionParameterNotKnown, InvalidFederationTime, FederateInternalError {

        StringBuilder builder = new StringBuilder("Interaction Received:");
        if(interactionClass == testDataProcuctHandle){
            try {
                int qty = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double time =  convertTime(theTime);
                builder.append("Testdata , time=" + time);
                builder.append(" qty=").append(qty);
                builder.append( "\n" );
            } catch (ArrayIndexOutOfBounds arrayIndexOutOfBounds) {
//                arrayIndexOutOfBounds.printStackTrace();
            }
        }

        log(builder.toString());

    }

}
