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

class GpxDrumkit 
{
    public static var _kits:Array<GpxDrumkit>;
    public static var DRUMKITS(getDrumkits, null):Array<GpxDrumkit>;
    
    public static function getDrumkits() : Array<GpxDrumkit>
    {
        if (_kits == null) 
        {
            _kits = new Array<GpxDrumkit>();
            _kits.push(new GpxDrumkit(36, 0 , 0));
            _kits.push(new GpxDrumkit(36, 0 , 0));
            _kits.push(new GpxDrumkit(37, 1 , 2));
            _kits.push(new GpxDrumkit(38, 1 , 0));
            _kits.push(new GpxDrumkit(41, 5 ,0));
            _kits.push(new GpxDrumkit(42, 10 ,0));
            _kits.push(new GpxDrumkit(43, 6 ,0));
            _kits.push(new GpxDrumkit(44, 11 ,0));
            _kits.push(new GpxDrumkit(45, 7 ,0));
            _kits.push(new GpxDrumkit(46, 10 ,2));
            _kits.push(new GpxDrumkit(47, 8 ,0));
            _kits.push(new GpxDrumkit(48, 9 ,0));
            _kits.push(new GpxDrumkit(49, 12 ,0));
            _kits.push(new GpxDrumkit(50, 9 ,0));
            _kits.push(new GpxDrumkit(51, 15 ,0));
            _kits.push(new GpxDrumkit(52, 16 ,0));
            _kits.push(new GpxDrumkit(53, 15 ,2));
            _kits.push(new GpxDrumkit(55, 14 ,0));
            _kits.push(new GpxDrumkit(56, 3 ,0));
            _kits.push(new GpxDrumkit(57, 13 ,0));
            _kits.push(new GpxDrumkit(59, 15 , 1));         
        }
        return _kits;
    }
    
    public var element(default,default):Int;
    public var variation(default,default):Int;
    public var midiValue(default,default):Int;
    
    public function new(midiValue:Int, element:Int, variation:Int)
    {
        this.midiValue = midiValue;
        this.element = element;
        this.variation = variation;
    }
}
