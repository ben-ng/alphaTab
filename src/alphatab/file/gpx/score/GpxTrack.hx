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
 *  
 *  This code is based on the code of TuxGuitar. 
 *      Copyright: J.Jørgen von Bargen, Julian Casadesus <julian@casadesus.com.ar>
 *      http://tuxguitar.herac.com.ar/
 */
package alphatab.file.gpx.score;

class GpxTrack 
{
    public var id(default,default):Int;
    public var tunningPitches(default,default):Array<Int>;
    public var color(default,default):Array<Int>;
    public var name(default,default):String;
    
    public var gmProgram(default,default):Int;
    public var gmChannel1(default,default):Int;
    public var gmChannel2(default,default):Int;
    
    public function new()
    {
        
    }
}
