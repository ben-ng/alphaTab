package org.herac.tuxguitar.player.impl.midiport.coreaudio;
import net.alphatab.midi.JNILibraryLoader;
import javax.sound.midi.Receiver;

public abstract class MidiReceiverJNI {

	private static final String JNI_LIBRARY_NAME = new String("tuxguitar-coreaudio-jni");

	static{
		try {
			JNILibraryLoader.loadLibrary(JNI_LIBRARY_NAME);
			//System.loadLibrary(JNI_LIBRARY_NAME);
		}
		catch(Throwable e) {
			e.printStackTrace();
		}
	}

	public MidiReceiverJNI() {
		super();
	}

	protected native void open();

	protected native void close();

	//protected native void findDevices();

	protected native void openDevice();

	protected native void closeDevice();

	protected native void noteOn(int channel,int note,int velocity);

	protected native void noteOff(int channel,int note,int velocity);

	protected native void controlChange(int channel,int control,int value);

	protected native void programChange(int channel,int program);

	protected native void pitchBend(int channel,int value);


	//protected abstract void addDevice(String name);
}
