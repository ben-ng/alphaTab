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
import java.io.*;
import java.lang.InterruptedException;

import javax.swing.JOptionPane;

import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JLabel;

import netscape.javascript.JSObject;

public class MidiPlayer extends JApplet {
	private Sequence _sequence;
	private Sequencer _sequencer;
	private long _lastTick;
	private String _updateFunction;
	private String _jsInitFunction;
	private int _metronomeTrack;
	private TickNotifierReceiver _tickReceiver;
	private ReentrantLock _lockObj;
	private JSObject _win;

	@Override
	public void init() {
		super.init();
		_updateFunction = getParameter("onTickChanged");
		_jsInitFunction = getParameter("onAppletLoaded");

		try {
			_sequencer = MidiSystem.getSequencer();
			_sequencer.open();

			Transmitter tickTransmitter = _sequencer.getTransmitter();
			_tickReceiver = new TickNotifierReceiver(tickTransmitter.getReceiver());
			tickTransmitter.setReceiver(_tickReceiver);
			_lockObj = new ReentrantLock();

			_tickReceiver.addControllerEventListener(new ControllerEventListener() {
				@Override
				public void controlChange(ShortMessage event) {
					if(_lockObj.tryLock()) {
						try {
							if(_sequencer.isRunning()) {
								switch(event.getCommand()) {
									case 0x80: // Noteon
									case 0x90: // Noteoff
											notifyPosition(_sequencer.getTickPosition());
										break;
								}
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
		}
		catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		if(_win==null) {
			//This pulls up the javascript player overlay
			_win=JSObject.getWindow(this);
			_win.eval(_jsInitFunction+"();");
		}
	}

	private void notifyPosition(long tickPosition) {
		if(_lastTick == tickPosition || _updateFunction == null)return;
		try {
			_win.eval("setTimeout(function(){"+_updateFunction+"("+tickPosition+");},1);");
			_lastTick = tickPosition;
		}
		catch (Exception e) {
			e.printStackTrace();
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
		_lockObj.lock();
		try {
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
				/*Not sure if this helps or is necesscary, sends the allSoundsOff command
				for(int i = 0; i < 15; i++) {
					ShortMessage allNotesOff = new ShortMessage();
					allNotesOff.setMessage(176 + i, 120, 0);
					_tickReceiver.send(allNotesOff, -1);
				}
				*/
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