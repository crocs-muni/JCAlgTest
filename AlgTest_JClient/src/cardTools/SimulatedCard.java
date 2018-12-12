package cardTools;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

/**
 *
 * @author Petr Svenda
 */
public class SimulatedCard extends Card {

    @Override
    public ATR getATR() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getProtocol() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public CardChannel getBasicChannel() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public CardChannel openLogicalChannel() throws CardException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void beginExclusive() throws CardException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void endExclusive() throws CardException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public byte[] transmitControlCommand(int i, byte[] bytes) throws CardException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void disconnect(boolean bln) throws CardException {
        // do nothing
    }
}
