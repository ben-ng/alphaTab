/*
	* This file is part of alphaTab.
	*
	*  alphaTab is free software: you can redistribute it and/or modify
	*  it under the terms of the GNU General Public License as published by
	*  the Free Software Foundation, either version 3 of the License, or
	*  (at your option) any later version.
	*
	*  alphaTab is distributed in the hope that it will be useful,
	*  but WITHOUT ANY WARRANTY; without even the implied warranty of
	*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	*  GNU General Public License for more details.
	*
	*  You should have received a copy of the GNU General Public License
	*  along with alphaTab.  If not, see <http://www.gnu.org/licenses/>.
	*/

package net.alphatab.midi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.IllegalStateException;
import java.util.concurrent.locks.*;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.io.*;
import java.lang.InterruptedException;

import javax.swing.JOptionPane;

import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Transmitter;
import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JLabel;

import org.herac.tuxguitar.player.impl.midiport.coreaudio.*;

import netscape.javascript.JSObject;

public class MidiPlayer extends JApplet {
	private Sequence _sequence;
	private Sequencer _sequencer;
	private long _lastTick;
	private String _updateFunction;
	private String _jsInitFunction;
	private String _stopFunction;
	private int _metronomeTrack;
	private TickNotifierReceiver _tickReceiver;
	private ReentrantLock _lockObj;
	private JSObject _win;
	private boolean noMIDI=false;
	private boolean isMac=false;
	private boolean isSetup=false;

	@Override
	public void init() {
		super.init();
		_updateFunction = getParameter("onTickChanged");
		_jsInitFunction = getParameter("onAppletLoaded");
		_stopFunction = getParameter("onSequenceStop");

		try {
			_sequencer = MidiSystem.getSequencer();
			_sequencer.open();

			//We use this lock object throughout to prevent deadlocks
			_lockObj = new ReentrantLock();
		}
		catch (MidiUnavailableException e) {
			noMIDI=true;
			e.printStackTrace();
		}
	}

	public void setIsMac(boolean isAMac) {
		this.isMac = isAMac;
	}

	public boolean getIsMac() {
		return this.isMac;
	}

	private void performSetupIfNecessary() throws MidiUnavailableException {
		if(this.isSetup) {
			return;
		}
		this.isSetup=true;

		//If this is a mac, we need to disable the native synthesizer by replacing it with
		//our custom receiver
		if(this.getIsMac()) {
			List<Transmitter> transmitters=_sequencer.getTransmitters();

			//Loop through all transmitters (there should only be one, but lets be safe here)
			for(int i=0; i<transmitters.size(); i++) {
				Transmitter tickTransmitter = transmitters.get(i);
				if(i==0) {
					//This custom receiver sends audio events to the CoreAudio JNI library, bypassing
					//the native java midi system
					MidiReceiverImpl coreAudioReceiver=new MidiReceiverImpl(tickTransmitter.getReceiver());
					coreAudioReceiver.open();
					tickTransmitter.setReceiver(coreAudioReceiver);
				}
				else {
					//This receiver has a neutered send() override
					tickTransmitter.setReceiver(new DummyReceiver());
				}
			}
		}

		//Now we get a new transmitter so that LiveConnect won't deadlock/lag along with the audio thread
		Transmitter liveConnectTransmitter = _sequencer.getTransmitter();
		_tickReceiver = new TickNotifierReceiver(liveConnectTransmitter.getReceiver());
		liveConnectTransmitter.setReceiver(_tickReceiver);

        _tickReceiver.addSysexEventListener(new SysexEventListener()
        {
            @Override
            public void sysex(SysexMessage message)
            {
				if(_lockObj.tryLock()) {
					try {
		                byte[] data = message.getData();
		                // JOptionPane.showMessageDialog(null,"Sysex" + data);
		                
		                if(data[0] == 0x00 &&
		                   data[1] == MidiMessageUtils.REST_MESSAGE)
		                {
		                    notifyPosition(_sequencer.getTickPosition());
		                }
					}
					catch(Exception ep) {
						ep.printStackTrace();
					}
					finally {
						_lockObj.unlock();
					}
	            }
	    	}
        });

		_sequencer.addMetaEventListener(new MetaEventListener() {
            @Override
            public void meta(MetaMessage metaMsg) {
		        if (metaMsg.getType() == 0x2F) {
					try {
		                notifyStop(_sequencer.getTickPosition());
					}
					catch(Exception ep) {
						ep.printStackTrace();
					}
					finally {
						_lockObj.unlock();
					}
		        }
            }
        });
	}
	//Runs when the applet starts, calls the javascript init function
	public void start() {
		if(_win==null) {
			//This pulls up the javascript player overlay
			_win=JSObject.getWindow(this);
			if(noMIDI) {
				_win.eval(_jsInitFunction+"(false,true);");
			}
			else {
				_win.eval(_jsInitFunction+"();");
			}
		}
	}

	private void notifyPosition(long tickPosition) {
		if(_lastTick == tickPosition || _updateFunction == null) return;
		try {
			_win.eval("setTimeout(function(){"+_updateFunction+"("+tickPosition+");},1);");
			_lastTick = tickPosition;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void notifyStop(long tickPosition) {
		if(_updateFunction == null)return;
		while(true) {
			if(_lockObj.getHoldCount()==0) {
				_lockObj.lock();
				try {
					_sequencer.stop();
					long tickpos=tickPosition-960*4;
					_sequencer.setTickPosition(tickpos);
					_win.eval("setTimeout(function(){"+_stopFunction+"("+tickpos+");},1);");
				}
				catch (Exception ep) {
					ep.printStackTrace();
				}
				finally {
					_lockObj.unlock();
					break;
				}
			}
			else {
				System.err.println("Could not acquire lock to send EOT callback");
			}
		}
	}

	public void updateSongData(String commands) {
		_lockObj.lock();
		try {
			_sequence = MidiSequenceParser.parse(commands);
			_sequencer.setSequence(_sequence);
			_metronomeTrack = MidiSequenceParser.getMetronomeTrack();
		}
		catch (Throwable e) {
			//JOptionPane.showMessageDialog(null,"MidiPlayer Error: \n" + e.toString());
			//e.printStackTrace();
		}
		finally {
			_lockObj.unlock();
		}
	}

	public void setMetronomeEnabled(boolean enabled) {
		_lockObj.lock();
		try {
			_sequencer.setTrackMute(_metronomeTrack, !enabled);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			_lockObj.unlock();
		}
	}

	public boolean isMetronomeEnabled() {
		_lockObj.lock();
		try {
			return _sequencer.getTrackMute(_metronomeTrack);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			_lockObj.unlock();
		}
		return false;
	}

	public void play() {
		try {
			performSetupIfNecessary();
			_lockObj.lock();
			_sequencer.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			_lockObj.unlock();
		}
	}

	public void pause() throws InterruptedException {
		//Don't lock yourself, silly. Without this the thread deadlocks itself in chrome.
		if(_lockObj.getHoldCount()==0) {
			_lockObj.lock();
			try {
				_sequencer.stop();
			}
			catch (IllegalStateException e) {
				e.printStackTrace();
			}
			/*
			catch (InvalidMidiDataException ex) {
				ex.printStackTrace();
			}*/
			catch (Exception ep) {
				ep.printStackTrace();
			}
			finally {
				_lockObj.unlock();
			}
		}
		else {
			_win.eval("setTimeout(function(){document.getElementById('playButton').click();},10);"); //Postpone it!
		}
	}

	public void stop() {
		try {
			this.pause();
			_sequencer.setTickPosition(0);
		}
		catch (IllegalStateException e) {
			e.printStackTrace();
		}
		catch (Exception ep) {
			ep.printStackTrace();
		}
	}

	public float getTempoInBPM() {
		_lockObj.lock();
		try {
			return _sequencer.getTempoInBPM();
		}
		catch (Exception ep) {
			ep.printStackTrace();
		}
		finally {
			_lockObj.unlock();
		}
		return (float) 0;
	}

	public void setTempoFactor(float newValue) {
		_lockObj.lock();
		try {
			_sequencer.setTempoFactor(newValue);
		}
		catch (Exception ep) {
			ep.printStackTrace();
		}
		finally {
			_lockObj.unlock();
		}
	}

	public long getMicrosecondLength() {
		_lockObj.lock();
		try {
			return _sequencer.getMicrosecondLength();
		}
		catch (Exception ep) {
			ep.printStackTrace();
		}
		finally {
			_lockObj.unlock();
		}
		return (long) 0;
	}

	public long getMicrosecondPosition() {
		_lockObj.lock();
		try {
			return _sequencer.getMicrosecondLength();
		}
		catch (Exception ep) {
			ep.printStackTrace();
		}
		finally {
			_lockObj.unlock();
		}
		return (long) 0;
	}

	public boolean getTrackMute(int track) {
		_lockObj.lock();
		try {
			return _sequencer.getTrackMute(track);
		}
		catch (Exception ep) {
			ep.printStackTrace();
		}
		finally {
			_lockObj.unlock();
		}
		return false;
	}

	public void setTrackMute(int track, boolean mute) {
		_lockObj.lock();
		try {
			_sequencer.setTrackMute(track, mute);
		}
		catch (Exception ep) {
			ep.printStackTrace();
		}
		finally {
			_lockObj.unlock();
		}
	}

	public boolean getTrackSolo(int track) {
		_lockObj.lock();
		try {
			return _sequencer.getTrackMute(track);
		}
		catch (Exception ep) {
			ep.printStackTrace();
		}
		finally {
			_lockObj.unlock();
		}
		return false;
	}

	public void setTrackSolo(int track, boolean solo) {
		_lockObj.lock();
		try {
			_sequencer.setTrackMute(track, solo);
		}
		catch (Exception ep) {
			ep.printStackTrace();
		}
		finally {
			_lockObj.unlock();
		}
	}

	public boolean isRunning() {
		_lockObj.lock();
		try {
			return _sequencer.isRunning();
		}
		catch (Exception ep) {
			ep.printStackTrace();
		}
		finally {
			_lockObj.unlock();
		}
		return false;
	}

	public void goTo(int tickPosition) {
		_lockObj.lock();
		try {
			_sequencer.setTickPosition(tickPosition);
		}
		catch (Exception ep) {
			ep.printStackTrace();
		}
		finally {
			_lockObj.unlock();
		}
	}
}
