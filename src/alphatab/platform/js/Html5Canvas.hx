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
package alphatab.platform.js;
#if js
import alphatab.platform.Canvas;

/**
 * A canvas implementation for HTML5 canvas
 */
class Html5Canvas implements Canvas 
{
    private var _canvas:Dynamic;
    private var _jCanvas:JQuery;
    private var _context:Dynamic;
    
    public function new(dom:Dynamic) 
    {  
        this._canvas = dom;
        this._jCanvas = JQuery.elements(dom);
        this._context = dom.getContext("2d");
    }
    
    public var width(getWidth, setWidth):Int;
    public var height(getHeight, setHeight):Int;
    
    private function getWidth():Int 
    {
        return this._jCanvas.Width(); 
    }
    
    private function getHeight():Int 
    {
        return this._jCanvas.Height();
    }
    
    private function setWidth(width:Int):Int 
    {
        this._jCanvas.setWidth(width);
        this._canvas.width = width;
        this._context = this._canvas.getContext("2d");
        return width;
    }
    
    private function setHeight(height:Int):Int 
    {
        this._jCanvas.setHeight(height);
        this._canvas.height = height;
        this._context = this._canvas.getContext("2d");
        return height;
    } 
    
    // colors and styles
    public var strokeStyle(getStrokeStyle, setStrokeStyle):String;
    
    private function getStrokeStyle() : String
    {
        return this._context.strokeStyle;
    } 
    private function setStrokeStyle(value:String) : String
    {
        this._context.strokeStyle = value; 
        return this._context.strokeStyle;
    }
    
    public var fillStyle(getFillStyle, setFillStyle):String;
    private function getFillStyle() : String
    {
        return this._context.fillStyle;
    }
    private function setFillStyle(value:String) : String
    {
        this._context.fillStyle = value;
        return this._context.fillStyle;
    }
    
    // line caps/joins
    public var lineWidth(getLineWidth, setLineWidth):Float;
    private function getLineWidth() : Float
    {
        return this._context.lineWidth;
    }
    private function setLineWidth(value:Float) : Float
    {
        this._context.lineWidth = value;
        return this._context.lineWidth;
    }
    
    // rects
    public function clear():Void
    {
        this._context.clearRect(0,0, width, height);
    }
    public function fillRect(x:Float, y:Float, w:Float, h:Float):Void
    {
        this._context.fillRect(x, y, w, h);
    }
    public function strokeRect(x:Float, y:Float, w:Float, h:Float):Void
    {
        this._context.strokeRect(x, y, w, h);
    }

    // path API
    public function beginPath():Void
    {
        this._context.beginPath();
    }
    public function closePath():Void
    {
        this._context.closePath();
    }
    public function moveTo(x:Float, y:Float):Void
    {
        this._context.moveTo(x, y);
    }
    public function lineTo(x:Float, y:Float):Void
    {
        this._context.lineTo(x, y);
    }
    public function quadraticCurveTo(cpx:Float, cpy:Float, x:Float, y:Float):Void
    {
        this._context.quadraticCurveTo(cpx, cpy, x, y);
    }
    public function bezierCurveTo(cp1x:Float, cp1y:Float, cp2x:Float, cp2y:Float, x:Float, y:Float):Void
    {
        this._context.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y);
    }
    public function circle(x:Float, y:Float, radius:Float):Void
    {
        this._context.arc(x,y, radius, 0, Math.PI*2,true);
    }
    public function rect(x:Float, y:Float, w:Float, h:Float):Void
    {
        this._context.rect(x, y, w, h);
    }
    public function fill():Void
    {
        this._context.fill();
    }
    public function stroke():Void
    {
        this._context.stroke();
    }

    // text
    public var font(getFont, setFont):String; 
    private function getFont() : String
    {
        return this._context.font;
    }
    private function setFont(value:String) : String
    {
        this._context.font = value;
        return this._context.font;
    }
    
    public var textBaseline(getTextBaseline, setTextBaseline):String; 
    private function getTextBaseline() : String
    {
        return this._context.textBaseline;
    }
    private function setTextBaseline(value:String) : String
    {
        this._context.textBaseline = value;
        return this._context.textBaseLine;
    }

    public var textAlign(getTextAlign, setTextAlign):String; 
    private function getTextAlign() : String
    {
        return this._context.textAlign;
    }
    private function setTextAlign(value:String) : String
    {
        this._context.textAlign = value;
        return this._context.textAlign;
    }
    
    public function fillText(text:String, x:Float, y:Float, maxWidth:Float = 0):Void
    {
        if (maxWidth == 0)
        {
            this._context.fillText(text, x, y);
        }
        else
        {
            this._context.fillText(text, x, y, maxWidth);
        }
    }
    public function strokeText(text:String, x:Float, y:Float, maxWidth:Float = 0):Void
    {
        if (maxWidth == 0)
        {
            this._context.strokeText(text, x, y);
        }
        else
        {
            this._context.strokeText(text, x, y, maxWidth);
        }
    }
    public function measureText(text:String):Float
    {
        return this._context.measureText(text).width;
    }
}
#end
