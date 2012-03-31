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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.ShortMessage;

public class MidiMessageUtils
{

    public static MidiMessage noteOn(int channel, int note, int velocity)
    {
        try
        {
            ShortMessage message = new ShortMessage();
            message.setMessage(ShortMessage.NOTE_ON, channel, note, velocity);
            return message;
        }
        catch (InvalidMidiDataException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static MidiMessage noteOff(int channel, int note, int velocity)
    {
        try
        {
            ShortMessage message = new ShortMessage();
            message.setMessage(ShortMessage.NOTE_OFF, channel, note, velocity);
            return message;
        }
        catch (InvalidMidiDataException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    public static final byte REST_MESSAGE = (byte)0x00;
    public static MidiMessage rest()
    {
        try
        {
            SysexMessage message = new SysexMessage();
            message.setMessage(new byte[]{(byte)0xF0, (byte)0x00, REST_MESSAGE}, 3);
            return message;
        }
        catch (InvalidMidiDataException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static MidiMessage controlChange(int channel, int controller,
            int value)
    {
        try
        {
            ShortMessage message = new ShortMessage();
            message.setMessage(ShortMessage.CONTROL_CHANGE, channel, controller, value);
            return message;
        }
        catch (InvalidMidiDataException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static MidiMessage programChange(int channel, int instrument)
    {
        try
        {
            ShortMessage message = new ShortMessage();
            message.setMessage(ShortMessage.PROGRAM_CHANGE, channel, instrument, 0);
            return message;
        }
        catch (InvalidMidiDataException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static MidiMessage pitchBend(int channel, int value)
    {
        try
        {
            ShortMessage message = new ShortMessage();
            
            message.setMessage(ShortMessage.PITCH_BEND, channel, 0, value);
            return message;
        }
        catch (InvalidMidiDataException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static MidiMessage systemReset()
    {
        try
        {
            ShortMessage message = new ShortMessage();
            message.setMessage(ShortMessage.SYSTEM_RESET);
            return message;
        }
        catch (InvalidMidiDataException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static MidiMessage tempoInUSQ(int usq)
    {
        try
        {
            MetaMessage message = new MetaMessage();
            message.setMessage(0x51, new byte[] {
                    (byte) ((usq >> 16) & 0x00FF),
                    (byte) ((usq >> 8) & 0x00FF), (byte) ((usq) & 0x00FF) }, 3);
            return message;
        }
        catch (InvalidMidiDataException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static MidiMessage timeSignature(int numerator, int denominatorIndex, int denominatorValue)
    {
        try
        {
            MetaMessage message = new MetaMessage();
            message.setMessage(0x58, new byte[] { (byte) numerator,
                    (byte) denominatorIndex,
                    (byte) (96 / denominatorValue), 8 }, 4);
            return message;
        }
        catch (InvalidMidiDataException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}