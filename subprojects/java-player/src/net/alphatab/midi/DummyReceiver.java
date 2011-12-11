package net.alphatab.midi;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

public class DummyReceiver implements Receiver{

    public DummyReceiver()
    {
    	    super();
    }

    @Override
    public void send(MidiMessage message, long timeStamp)
    {
    }
    
    public void open(){};
    public void close(){};
}
