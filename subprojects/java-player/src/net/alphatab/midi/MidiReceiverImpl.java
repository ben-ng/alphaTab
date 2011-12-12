package net.alphatab.midi;

import org.herac.tuxguitar.player.impl.midiport.coreaudio.*;
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public class MidiReceiverImpl extends MidiReceiverJNI implements Receiver{
	private boolean open; // unnecessary
	    private boolean connected;
	    private Receiver _old;

	public MidiReceiverImpl(Receiver oldRecv){
		//System.out.println("MidiReceiverImpl");
		this.connected = false;
		_old=oldRecv;
	}

	@Override
	public void send(MidiMessage message, long timeStamp)
	{
		if(message instanceof ShortMessage) {
			ShortMessage sm=(ShortMessage) message;

			int command = sm.getCommand();
			int channel = sm.getChannel();
			int data1 = sm.getData1();
			int data2 = sm.getData2();
			switch(command) {
				case ShortMessage.NOTE_ON:
					//System.out.println("NOTE_ON");
					this.sendNoteOn(channel,data1,data2);
				break;
				case ShortMessage.NOTE_OFF:
					//System.out.println("NOTE_OFF");
					this.sendNoteOff(channel,data1,data2);
				break;
				case ShortMessage.CONTROL_CHANGE:
					//System.out.println("CONTROL_CHANGE");
					this.sendControlChange(channel,data1,data2);
				break;
				case ShortMessage.PROGRAM_CHANGE:
					//System.out.println("PROGRAM_CHANGE");
					this.sendProgramChange(channel,data1);
				break;
				case ShortMessage.PITCH_BEND:
					//System.out.println("PITCH_BEND");
					this.sendPitchBend(channel,data2);
				break;
				default:
					System.out.println("NOTICE: Unknown midi command, ignoring");
				break;
			}
			//System.out.println("Channel: "+channel+" Data: "+data1+","+data2);
		}
		else {
			System.out.println("NOTICE: Non-shortmessage received");
		}

		//Don't send anything to the old receiver (might be a synthesizer you see..)
		//if (_old != null) _old.send(message, timeStamp);
	}

	public void open(){
		//System.out.println("open");
		//super.open();
		this.open = true;
		this.connect();
	}

	public void close(){
		if(this.isOpen()){
			this.disconnect();
			super.close();
			this.open = false;
		}
	}

	public boolean isOpen(){
		return (this.open);
	}

	public boolean isConnected(){
		return (this.isOpen() && this.connected);
	}

    public void connect(){
		//System.out.println("connect()");
        if(isOpen()){
            if(!isConnected()){
                this.connected = true;
                this.openDevice();
            }
        }
    }

	public void disconnect() {
		if(isConnected()){
			this.closeDevice();
			this.connected = false;
		}
	}

	public void sendSystemReset() {
		if(isOpen()){
			//not implemented
		}
	}

	public void sendAllNotesOff() {
		for(int i = 0; i < 16; i ++){
			sendControlChange(i,MidiControllers.ALL_NOTES_OFF,0);
		}
	}

	public void sendControlChange(int channel, int controller, int value) {
		if(isOpen()){
			super.controlChange(channel, controller, value);
		}
	}

	public void sendNoteOff(int channel, int key, int velocity) {
		if(isOpen()){
			super.noteOff(channel, key, velocity);
		}
	}

	public void sendNoteOn(int channel, int key, int velocity) {
		if(isOpen()){
			super.noteOn(channel, key, velocity);
		}
	}

	public void sendPitchBend(int channel, int value) {
		if(isOpen()){
			super.pitchBend(channel, value);
		}
	}

	public void sendProgramChange(int channel, int value) {
		if(isOpen()){
			super.programChange(channel, value);
		}
	}
}
